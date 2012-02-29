(ns gaeshi.datastore
  (:use
    [joodo.string :only (gsub)]
    [joodo.datetime :only (now)]
    [joodo.core :only (->options)]
    [gaeshi.datastore.types :only (pack unpack)])
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

(def *entities* (ref {}))

(def datastore-service-instance (atom nil))

(defn datastore-service []
  (when (nil? @datastore-service-instance)
    (reset! datastore-service-instance (DatastoreServiceFactory/getDatastoreService)))
  @datastore-service-instance)

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

(defn ->key [value]
  (cond
    (key? value) value
    (string? value) (string->key value)
    (nil? value) nil
    :else (string->key (:key value))))

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

(defn- with-created-at [record spec]
  (if (and (or (contains? spec :created-at) (contains? record :created-at)) (= nil (:created-at record)))
    (assoc record :created-at (now))
    record))

(defn- with-updated-at [record spec]
  (if (or (contains? spec :updated-at) (contains? record :updated-at))
    (assoc record :updated-at (now))
    record))

(defn with-updated-timestamps [record]
  (let [spec (get @*entities* (:kind record))]
    (with-updated-at (with-created-at record spec) spec)))

(defn kind [thing]
  (cond
    (isa? (class thing) Entity) (.getKind thing)
    (map? thing) (:kind thing)
    :else nil))

(defn pack-field [packer value]
  (cond
    (sequential? value) (map #(pack-field packer %) value)
    (fn? packer) (packer value)
    :else (pack packer value)))

(defn unpack-field [unpacker value]
  (cond
    (isa? (class value) java.util.List) (map #(unpack-field unpacker %) value)
    (fn? unpacker) (unpacker value)
    unpacker (unpack value)
    :else value))

(defmulti entity->record kind)

(defmethod entity->record nil [entity]
  nil)

(defn known-entity->record
  ([entity]
    (let [kind (.getKind entity)
          spec (get @*entities* kind)]
      (known-entity->record entity kind spec)))
  ([entity kind spec]
    (let [key (key->string (.getKey entity))
          record ((:*ctor* spec) key)]
      (after-load
        (reduce
          (fn [record [field value]]
            (let [field (keyword field)]
              (assoc record field (unpack-field (:unpacker (field spec)) value))))
          record
          (.getProperties entity))))))

(defn- unknown-entity->record [entity kind]
  (after-load
    (reduce
      (fn [record entry] (assoc record (keyword (key entry)) (val entry)))
      {:kind kind :key (key->string (.getKey entity))}
      (.getProperties entity))))

(defmethod entity->record :default [entity]
  (let [kind (.getKind entity)
        spec (get @*entities* kind)]
    (if spec
      (known-entity->record entity kind spec)
      (unknown-entity->record entity kind))))

(defprotocol EntityRecord
  (->entity [this]))

(defn- unknown-record->entity [record kind]
  (let [key (string->key (:key record))
        entity (if key (Entity. key) (Entity. kind))]
    (doseq [[field value] (dissoc record :kind :key)]
      (.setProperty entity (name field) value))
    entity))

(defn known-record->entity
  ([record]
    (let [kind (:kind record)
          spec (get @*entities* kind)]
      (known-record->entity record kind spec)))
  ([record kind spec]
    (let [key (string->key (:key record))
          entity (if key (Entity. key) (Entity. kind))]
      (doseq [[field attrs] (dissoc spec :*ctor*)]
        (.setProperty entity (name field) (pack-field (:packer (field spec)) (field record))))
      entity)))

(extend-type clojure.lang.APersistentMap
  EntityRecord
  (->entity [record]
    (let [kind (:kind record)
          spec (get @*entities* kind)]
      (if spec
        (known-record->entity record kind spec)
        (unknown-record->entity record kind)))))

(defn- map-fields [fields]
  (reduce
    (fn [spec [key & args]]
      (let [attrs (apply hash-map args)
            attrs (if-let [t (:type attrs)] (assoc (dissoc attrs :type) :packer t :unpacker t) attrs)]
        (assoc spec (keyword key) attrs)))
    {}
    fields))

(defn- extract-defaults [field-specs]
  (reduce
    (fn [map [field spec]]
      (if-let [default (:default spec)]
        (assoc map field default)
        map))
    {}
    field-specs))

(defn construct-entity-record [kind & args]
  (let [spec (get @*entities* kind)
        args (->options args)
        extras (apply dissoc args (keys spec))
        record ((:*ctor* spec) nil)]
    (after-create
      (merge
        (reduce
          (fn [record [key attrs]] (assoc record key (or (key args) (:default attrs))))
          record
          (dissoc spec :*ctor*))
        extras))))

(defmacro defentity [class-sym & fields]
  (let [field-map (map-fields fields)
        kind (spear-case class-sym)]
    `(do
       (defrecord ~class-sym [~'kind ~'key])
       (dosync (alter *entities* assoc ~kind (assoc ~field-map :*ctor* (fn [key#] (new ~class-sym ~kind key#)))))
       (defn ~(symbol kind) [& args#] (apply construct-entity-record ~kind args#))
       (extend-type ~class-sym EntityRecord (~'->entity [this#] (known-record->entity this#)))
       (defmethod entity->record ~kind [entity#] (known-entity->record entity#)))))

(defn save [record & values]
  (let [values (->options values)
        record (merge record values)
        record (with-updated-timestamps record)
        record (before-save record)
        entity (->entity record)
        key (.put (datastore-service) entity)]
    (entity->record entity)))

(defn save-many [records]
  (let [records (map with-updated-timestamps records)
        records (map before-save records)
        entities (map ->entity records)
        keys (.put (datastore-service) entities)]
    (map entity->record entities)))

(defn load-entity [entity]
  (entity->record entity))

(defn find-by-key [value]
  (when-let [key (->key value)]
    (try
      (let [entity (.get (datastore-service) key)]
        (load-entity entity))
      (catch EntityNotFoundException e
        nil))))

(defn find-by-keys [^Iterable keys]
  (let [keys (filter identity (map ->key keys))
        result-map (.get (datastore-service) keys)
        entities (map #(get result-map %) keys)]
    (map load-entity entities)))

(defn reload [record]
  (cond
    (key? record) (find-by-key record)
    (string? record) (find-by-key (string->key record))
    (map? record) (find-by-key (:key record))))

(defn delete [& records]
  (.delete (datastore-service) (map #(->key (:key %)) records)))

(defn- ->filter-operator [operator]
  (or
    (case (name operator)
      ("=" "eq") Query$FilterOperator/EQUAL
      ("<" "lt") Query$FilterOperator/LESS_THAN
      ("<=" "lte") Query$FilterOperator/LESS_THAN_OR_EQUAL
      (">" "gt") Query$FilterOperator/GREATER_THAN
      (">=" "gte") Query$FilterOperator/GREATER_THAN_OR_EQUAL
      ("!=" "not") Query$FilterOperator/NOT_EQUAL
      ("contains?" "in") Query$FilterOperator/IN)
    (throw (Exception. (str "Unknown filter: " operator)))))

(defn- parse-filters [filters]
  (when filters
    (let [filters (if (vector? (first filters)) filters (vector filters))]
      (map
        (fn [[operator field value]]
          [(->filter-operator operator) (name field) value])
        filters))))

(defn- ->sort-direction [direction]
  (or
    (case (name direction)
      "asc" Query$SortDirection/ASCENDING
      "desc" Query$SortDirection/DESCENDING)
    (throw (Exception. (str "Unknown sort direction: " direction)))))

(defn- parse-sorts [sorts]
  (when sorts
    (let [sorts (if (vector? (first sorts)) sorts (vector sorts))]
      (map
        (fn [[field direction]]
          [(name field) (->sort-direction direction)])
        sorts))))

(defn build-query [kind options]
  (let [spec (get @*entities* kind)
        filters (vec (parse-filters (:filters options)))
        sorts (vec (parse-sorts (:sorts options)))]
    (let [query (if kind (Query. (name kind)) (Query.))]
      (doseq [[operator field value] filters] (.addFilter query field operator (pack-field (:packer (get spec (keyword field))) value)))
      (doseq [[field direction] sorts] (.addSort query field direction))
      (.prepare (datastore-service) query))))

(defn build-fetch-options [options]
  (let [limit (:limit options)
        offset (:offset options)
        prefetch-size (:prefetch-size options)
        chunk-size (:chunk-size options)
        start-cursor (:start-cursor options)
        end-cursor (:end-cursor options)]
    (let [fetch-options (FetchOptions$Builder/withDefaults)]
      (when limit (.limit fetch-options limit))
      (when offset (.offset fetch-options offset))
      (when prefetch-size (.prefetchSize fetch-options prefetch-size))
      (when chunk-size (.checkSize fetch-options chunk-size))
      (when start-cursor (.startCursor fetch-options start-cursor))
      (when end-cursor (.endCursor fetch-options end-cursor))
      fetch-options)))

(defn find-by-kind [kind & optionskv]
  (let [options (->options optionskv)
        query (build-query kind options)
        fetching (build-fetch-options options)]
    (let [results (.asQueryResultIterator query fetching)]
      (map load-entity (iterator-seq results)))))

(defn find-all-kinds [& optionskv]
  (let [options (->options optionskv)
        query (build-query nil options)
        fetching (build-fetch-options options)]
    (let [results (.asQueryResultIterator query fetching)]
      (map load-entity (iterator-seq results)))))

(defn count-by-kind [kind & optionskv]
  (let [options (->options optionskv)]
    (.countEntities
      (build-query kind options)
      (build-fetch-options options))))

(defn count-all-kinds [& optionskv]
  (let [options (->options optionskv)]
    (.countEntities
      (build-query nil options)
      (build-fetch-options options))))
