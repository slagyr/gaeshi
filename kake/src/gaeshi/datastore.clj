(ns gaeshi.datastore
  (:use
    [gaeshi.string :only (gsub)]
    [gaeshi.datastore.types :only (pack unpack)]
    [gaeshi.datetime :only (now)])
  (:require
    [clojure.string :as str])
  (:import
    [com.google.appengine.api.datastore Entity Query DatastoreServiceFactory Query$FilterOperator
     Query$SortDirection FetchOptions$Builder EntityNotFoundException KeyFactory Key]))

(defn spear-case [value]
  (str/lower-case
    (gsub
      (str/replace (name value) "_" "-")
      #"([a-z])([A-Z])" (fn [[_ lower upper]] (str lower "-" upper)))))

(def datastore-service-instance (atom nil))

(defn datastore-service []
  (when (nil? @datastore-service-instance)
    (reset! datastore-service-instance (DatastoreServiceFactory/getDatastoreService)))
  @datastore-service-instance)

(defprotocol AfterCreate
  (after-create [this]))

(defprotocol BeforeSave
  (before-save [this]))

(defprotocol AfterLoad
  (after-load [this]))

(extend-type Object
  AfterCreate
  (after-create [this] this)
  BeforeSave
  (before-save [this] this)
  AfterLoad
  (after-load [this] this))

(extend-type nil
  AfterCreate
  (after-create [_] nil)
  BeforeSave
  (before-save [_] nil)
  AfterLoad
  (after-load [_] nil))

(defn- with-created-at [record]
  (if (and (contains? record :created-at) (= nil (:created-at record)))
    (assoc record :created-at (now))
    record))

(defn- with-updated-at [record]
  (if (contains? record :updated-at)
    (assoc record :updated-at (now))
    record))

(defn with-updated-timestamps [record]
  (with-updated-at (with-created-at record)))

(defn kind [thing]
  (cond
    (isa? (class thing) Entity) (.getKind thing)
    (map? thing) (:kind thing)
    :else nil))

(defmulti entity->record kind)

(defmethod entity->record nil [entity]
  nil)

(defmethod entity->record :default [entity]
  (after-load
    (reduce
      (fn [record entry] (assoc record (keyword (key entry)) (val entry)))
      {:kind (.getKind entity) :key (.getKey entity)}
      (.getProperties entity))))

(defmulti record->entity :kind)

(defmethod record->entity :default [record]
  (let [entity (if (:key record) (Entity. (:key record)) (Entity. (:kind record)))]
    (doseq [[key value] (dissoc record :kind :key)]
      (.setProperty entity (name key) value))
    entity))

(defn pack-field [packer value]
  (cond
    (fn? packer) (packer value)
    :else (pack packer value)))

(defn unpack-field [unpacker value]
  (cond
    (fn? unpacker) (unpacker value)
    unpacker (unpack value)
    :else value))

(defn- map-field-specs [fields]
  (vec
    (map
      (fn [field]
        (let [key (keyword (first field))
              spec (apply hash-map (rest field))
              spec (if-let [t (:type spec)] (assoc (dissoc spec :type) :packer t :unpacker t) spec)]
          [key spec]))
      fields)))

(defn- extract-defaults [field-specs]
  (reduce
    (fn [map [field spec]]
      (if-let [default (:default spec)]
        (assoc map field default)
        map))
    {}
    field-specs))

(defn- define-constructor [class-sym field-specs]
  (let [kind (spear-case (name class-sym))
        ctor-sym (symbol kind)
        defaults (extract-defaults field-specs)
        field-keys (map first field-specs)]
    `(defn ~ctor-sym [& args#]
      (let [~'values (if (map? (first args#)) (merge (first args#) (apply hash-map (rest args#))) (apply hash-map args#))
            ~'values (merge ~defaults ~'values)
            extras# (dissoc ~'values ~@field-keys)]
        (after-create
          (merge
            (new ~class-sym ~kind nil
              ~@(for [[field _] field-specs]
                `(~field ~'values)))
            extras#))))))

(defn- define-from-entity [class-sym field-specs]
  (let [kind (spear-case (name class-sym))
        field-keys (map first field-specs)
        spec-map (apply hash-map (flatten field-specs))]
    `(defmethod entity->record ~kind [entity#]
      (let [~'properties (reduce (fn [m# [key# val#]] (assoc m# (keyword key#) val#)) {} (.getProperties entity#))
            extras# (dissoc ~'properties ~@field-keys)]
        (merge
          (new ~class-sym ~kind (.getKey entity#)
            ~@(for [[field _] field-specs]
              `(unpack-field (:unpacker (~field ~spec-map)) (get ~'properties ~field))))
          extras#)))))

(defn- define-to-entity [class-sym field-specs]
  (let [kind (spear-case (name class-sym))]
    `(defmethod record->entity ~kind [record#]
      (let [entity# (if (:key record#) (Entity. (:key record#)) (Entity. (:kind record#)))
            field-specs# ~field-specs]
        (doseq [[key# spec#] field-specs#]
          (.setProperty entity# (name key#) (pack-field (:packer spec#) (get record# key#))))
        entity#))))

(defmacro defentity [class-sym & fields]
  (let [field-specs (map-field-specs fields)
        field-names (map first fields)]
    `(do
      (defrecord ~class-sym [~'kind ~'key ~@field-names])
      ~(define-constructor class-sym field-specs)
      ~(define-from-entity class-sym field-specs)
      ~(define-to-entity class-sym field-specs))))

(defn save [record & values]
  (let [values (if (map? (first values)) (merge (first values) (apply hash-map (rest values))) (apply hash-map values))
        record (merge record values)
        record (with-updated-timestamps record)
        record (before-save record)
        entity (record->entity record)
        key (.put (datastore-service) entity)]
    (entity->record entity)))

(defn save-many [records]
  (let [records (map with-updated-timestamps records)
        records (map before-save records)
        entities (map record->entity records)
        keys (.put (datastore-service) entities)]
    (map entity->record entities)))

(defn load-entity [entity]
  (after-load
    (entity->record entity)))

(defn find-by-key [^Key key]
  (if (nil? key)
    nil
    (try
      (let [entity (.get (datastore-service) key)]
        (load-entity entity))
      (catch EntityNotFoundException e
        nil))))

(defn find-by-keys [^Iterable keys]
  (let [keys (filter identity keys)
        result-map (.get (datastore-service) keys)
        entities (map #(get result-map %) keys)]
    (map load-entity entities)))

(defn delete [& records]
  (.delete (datastore-service) (map :key records)))

(defn- ->filter-operator [operator]
  (cond
    (= '= operator) `Query$FilterOperator/EQUAL
    (= '< operator) `Query$FilterOperator/LESS_THAN
    (= '<= operator) `Query$FilterOperator/LESS_THAN_OR_EQUAL
    (= '> operator) `Query$FilterOperator/GREATER_THAN
    (= '>= operator) `Query$FilterOperator/GREATER_THAN_OR_EQUAL
    (= 'not operator) `Query$FilterOperator/NOT_EQUAL
    (= 'contains? operator) `Query$FilterOperator/IN
    :else (throw (Exception. (str "Unknown filter: " operator)))))

(defn- parse-filters [filters]
  (map
    (fn [[operator field value]]
      [(->filter-operator operator) (name field) value])
    filters))

(defn- ->sort-direction [direction]
  (cond
    (= :asc direction) `Query$SortDirection/ASCENDING
    (= :desc direction) `Query$SortDirection/DESCENDING
    (= "asc" (name direction)) `Query$SortDirection/ASCENDING
    (= "desc" (name direction)) `Query$SortDirection/DESCENDING
    :else (throw (Exception. (str "Unknown sort direction: " direction)))))

(defn- parse-sorts [sorts]
  (map
    (fn [[field direction]]
      [(name field) (->sort-direction direction)])
    sorts))

(defn build-query [kind options]
  (let [filters (vec (parse-filters (:filters options)))
        sorts (vec (parse-sorts (:sorts options)))]
    `(let [query# (if ~kind (Query. (name ~kind)) (Query.))]
      (doseq [[operator# field# value#] ~filters] (.addFilter query# field# operator# value#))
      (doseq [[field# direction#] ~sorts] (.addSort query# field# direction#))
      (.prepare (datastore-service) query#))))

(defn build-fetch-options [options]
  (let [limit (:limit options)
        offset (:offset options)
        prefetch-size (:prefetch-size options)
        chunk-size (:chunk-size options)
        start-cursor (:start-cursor options)
        end-cursor (:end-cursor options)]
    `(let [fetch-options# (FetchOptions$Builder/withDefaults)]
      (when ~limit (.limit fetch-options# ~limit))
      (when ~offset (.offset fetch-options# ~offset))
      (when ~prefetch-size (.prefetchSize fetch-options# ~prefetch-size))
      (when ~chunk-size (.checkSize fetch-options# ~chunk-size))
      (when ~start-cursor (.startCursor fetch-options# ~start-cursor))
      (when ~end-cursor (.endCursor fetch-options# ~end-cursor))
      fetch-options#)))

(defmacro find-by-kind [kind & optionskv]
  (let [options (apply hash-map optionskv)
        query (build-query kind options)
        fetching (build-fetch-options options)]
    `(let [results# (.asQueryResultIterator ~query ~fetching)]
      (map load-entity (iterator-seq results#)))))

(defmacro find-all-kinds [& optionskv]
  (let [options (apply hash-map optionskv)
        query (build-query nil options)
        fetching (build-fetch-options options)]
    `(let [results# (.asQueryResultIterator ~query ~fetching)]
      (map load-entity (iterator-seq results#)))))

(defmacro count-by-kind [kind & optionskv]
  (let [options (apply hash-map optionskv)]
    `(.countEntities
      ~(build-query kind options)
      ~(build-fetch-options options))))

(defmacro count-all-kinds [& optionskv]
  (let [options (apply hash-map optionskv)]
    `(.countEntities
      ~(build-query nil options)
      ~(build-fetch-options options))))

  (defn create-key [kind id]
    (if (number? id)
      (KeyFactory/createKey kind (long id))
      (KeyFactory/createKey kind (str id))))

  (defn key? [key]
    (isa? (class key) Key))

  (defn key->string [^Key key]
    (try
      (KeyFactory/keyToString key)
      (catch Exception e nil)))

  (defn string->key [value]
    (try
      (KeyFactory/stringToKey value)
      (catch Exception e nil)))
