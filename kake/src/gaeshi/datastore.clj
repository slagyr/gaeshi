(ns gaeshi.datastore
  (:use
    [gaeshi.string :only (gsub)])
  (:require
    [clojure.string :as str])
  )

(defn spear-case [value]
  (str/lower-case
    (gsub
      (str/replace (name value) "_" "-")
      #"([a-z])([A-Z])" (fn [[_ lower upper]] (str lower "-" upper)))))

(defn- map-field-specs [fields]
  (map
    (fn [field] [(keyword (first field)) (apply hash-map (rest field))])
    fields))

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
        defaults (extract-defaults field-specs)]
    `(defn ~ctor-sym [& args#]
      (let [~'values (apply hash-map args#)
            ~'values (merge ~defaults ~'values)]
        (new ~class-sym ~kind nil
          ~@(for [[field _] field-specs]
            `(~field ~'values)))))))

(defmacro defentity [class-sym & fields]
  (let [field-specs (map-field-specs fields)
        field-names (map first fields)]
    `(do
      (defrecord ~class-sym [~'kind ~'key ~@field-names])
      ~(define-constructor class-sym field-specs))))

(def datastore-service-instance (atom nil))

(defn datastore-service []
  (when (nil? @datastore-service-instance)
    (reset! datastore-service-instance (com.google.appengine.api.datastore.DatastoreServiceFactory/getDatastoreService)))
  @datastore-service-instance)

(defmulti entity->record (fn [entity] (.getKind entity)))

(defmethod entity->record :default [entity]
  (reduce
    (fn [record entry] (assoc record (keyword (key entry)) (val entry)))
    {:kind (.getKind entity) :key (.getKey entity)}
    (.getProperties entity)))

(defmulti record->entity :kind)

(defmethod record->entity :default [record]
  (let [entity (com.google.appengine.api.datastore.Entity. (:kind record))]
    (doseq [[key value] (dissoc record :kind :key)]
      (.setProperty entity (name key) value))
    entity))

(defn save [record]
  (let [entity (record->entity record)
        key (.put (datastore-service) entity)]
    (assoc record :key key)))

(defn find-by-key [key]
  (let [entity (.get (datastore-service) key)]
    (entity->record entity)))
