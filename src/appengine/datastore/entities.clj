(ns #^{:author "Roman Scherer"
       :doc
       "Entity is the fundamental unit of data storage. It has an
immutable identifier (contained in the Key) object, a reference to an
optional parent Entity, a kind (represented as an arbitrary string),
and a set of zero or more typed properties." }
  appengine.datastore.entities
  (:import (com.google.appengine.api.datastore Entity EntityNotFoundException Key)
           (clojure.lang IPersistentMap))
  (:require [appengine.datastore.service :as datastore]
            [appengine.datastore.types :as types])
  (:use [clojure.contrib.string :only (blank? join lower-case replace-str replace-re)]
        [clojure.contrib.seq :only (includes?)]
        [appengine.datastore.utils :only (assert-new)]
        appengine.datastore.query
        appengine.datastore.protocols
        appengine.datastore.keys
        appengine.utils
        inflections))

(defn- extract-values
  "Extract the attributes of the record that are used to build the
entity key. The key-fns argument must be a sequence of key/fn
pairs. The key is used to extract the value, and fn will be applied to
the value if it is not nil. If fn is true, the value is returned
without transformation.

Examples:

  (extract-values
    {:iso-3166-alpha-2 \"eu\" :name \"Europe\"}
   [[:iso-3166-alpha-2 true] [:name lower-case]])
  ; => (\"eu\" \"europe\")

  (extract-values {} [[:iso-3166-alpha-2 true] [:name lower-case]])
  ; => (nil nil)
"
  [record key-fns]
  (map (fn [[key key-fn]]
         (if-let [value (get record key)]
           (if (fn? key-fn)
             (key-fn value) value)))
       key-fns))

(defn extract-key
  "Extract the entity key from record. The key-fns argument must be a
sequence of key/fn pairs. The key is used to extract the value, and fn
will be applied to the value if it is not nil. If fn is true, the
value is returned without transformation. The values will be joined by
\"-\" or the separator specified. The fn returns the entity key only
if all key values are present in the record.

Examples:

  (extract-key {:iso-3166-alpha-2 \"eu\" :name \"Europe\"}
               [[:iso-3166-alpha-2 true] [:name lower-case]])
  ; => \"eu-europe\"

  (extract-key {:iso-3166-alpha-2 \"eu\"}
               [[:iso-3166-alpha-2 true] [:name lower-case]])
  ; => nil
"
  [record key-fns & {:keys [separator]}]
  (let [values (extract-values record key-fns)]
    (if (and (not (empty? values)) (every? (comp not nil?) values))
      (join (or separator "-") values))))

(defn- extract-properties
  "Extract the properties from the property specification."
  [property-specs]
  (reduce
   #(assoc %1 (keyword (first %2)) (apply hash-map (rest %2)))
   (array-map) (reverse property-specs)))

(defn- extract-option
  "Extract the option from the property specification."
  [property-specs option]
  (let [properties (extract-properties property-specs)]
    (reduce
     #(if-let [value (option (%2 properties))] (assoc %1 %2 value) %1)
     (array-map) (reverse (keys properties)))))

(defn- extract-key-fns
  "Extract the key fns from the property specification."
  [property-specs]
  (apply vector (seq (extract-option property-specs :key))))

(defn- extract-serializer
  "Extract the property serialization fns from the property
  specification."
  [property-specs]
  (apply hash-map (flat-seq (extract-option property-specs :serialize))))

(defn- extract-deserializer
  "Extract the property deserialization fns from the property
  specification."
  [property-specs]
  (apply hash-map (flat-seq (merge (extract-option property-specs :serialize)
                                   (extract-option property-specs :deserialize)))))

(defn- humanize [entity]
  (lower-case (replace-str "-" " " (hyphenize (demodulize entity)))))

(defn- entity-fn-doc [entity]
  (str "Make a " (humanize entity) "."))

(defn- entity-fn-sym [entity]
  (symbol (hyphenize (demodulize entity))))

(defn- entity-p-fn-doc [entity]
  (str "Returns true if arg is a " (humanize entity) ", false otherwise."))

(defn- entity-p-fn-sym [entity]
  (symbol (str (hyphenize (demodulize entity)) "?")))

(defn- find-entities-fn-doc [entity]
  (str "Find all " (lower-case (pluralize (stringify entity))) "."))

(defn- find-entities-fn-sym [entity]
  (symbol (str "find-" (hyphenize (pluralize (demodulize entity))))))

(defn- find-entities-by-property-fn-doc [entity property]
  (str "Find all " (lower-case (pluralize (stringify entity))) " by " (stringify property) "."))

(defn- find-entities-by-property-fn-sym [entity property]
  (symbol (str "find-" (hyphenize (pluralize (demodulize entity))) "-by-" (hyphenize (stringify property)))))

(defn- key-fn-doc [entity]
  (str "Make a " (humanize entity) " key."))

(defn- key-fn-sym [entity]
  (symbol (str (entity-fn-sym entity) "-key")))

(defn- key-name-fn-doc [entity]
  (str "Extract the " (humanize entity) " key name."))

(defn- key-name-fn-sym [entity]
  (symbol (str (entity-fn-sym entity) "-key-name")))

(defn entity?
  "Returns true if arg is an Entity, false otherwise."
  [arg] (isa? (class arg) Entity))

(defn entity-kind-name
  "Returns the kind of the entity as a string.

Examples:

  (entity-kind-name 'Continent)
  ; => \"continent\"

  (entity-kind-name 'CountryFlag)
  ; => \"country-flag\"
"
  [record] (if record (hyphenize (demodulize record))))

(defn entity-protocol-name
  "Returns the protocol name for an entity. Entity can be a class,
a symbol or a string. If the entity is blank the fn returns nil.

Examples:

  (entity-protocol-name nil)
  ; => nil

  (entity-protocol-name \"\")
  ; => nil

  (entity-protocol-name Continent)
  ; => \"ContinentProtocol\"

  (entity-protocol-name 'Continent)
  ; => \"ContinentProtocol\"

  (entity-protocol-name \"Continent\")
  ; => \"ContinentProtocol\"
"
  [entity] (if-not (blank? (str entity)) (str (demodulize entity) "Protocol")))

;; (entity-protocol-name nil)
;; (entity-protocol-name 'Continent)

(defn make-blank-entity
  "Returns a blank Entity. If called with one parameter, key-or-kind
  must be either a Key or a String, that specifies the kind of the
  entity. If called with multiple parameters, parent must be the Key
  of the parent entity, kind a String that specifies the kind of the
  entity and key-name a String that identifies the entity.

Examples:

  (make-blank-entity \"continent\")
  ; => #<Entity [continent(no-id-yet)]>

  (make-blank-entity (make-key \"continent\" \"eu\"))
  ; => #<Entity [continent(\"eu\")]>
"
  ([key-or-kind]
     (Entity. key-or-kind))
  ([#^Key parent #^String kind key-or-id]
     (Entity. (make-key parent kind key-or-id))))

(defn deserialize-property
  "Deserialize the property value with the deserializer."
  [value deserializer]
  (cond (nil? value) nil
        (fn? deserializer) (deserializer value)
        (class? deserializer) (deserialize value)
        :else value))

(defn   serialize-property
  "Serialize the property value with the serializer."
  [value serializer]
  (cond (nil? value) nil
        (fn? serializer) (serializer value)
        (class? serializer) (types/serialize serializer value)
        :else value))

(defmulti deserialize-entity
  "Convert a Entity into a persistent map. The property values are
stored under their property names converted to a keywords, the
entity's key and kind under the :key and :kind keys.

Examples:

  (deserialize-entity (Entity. \"continent\"))
  ; => {:kind \"continent\", :key #<Key continent(no-id-yet)>}

  (deserialize-entity (doto (Entity. \"continent\")
                     (.setProperty \"name\" \"Europe\")))
  ; => {:name \"Europe\", :kind \"continent\", :key #<Key continent(no-id-yet)>}
"
  (fn [entity] (.getKind entity)))

(defmethod deserialize-entity :default [entity]
           (reduce #(assoc %1 (keyword (key %2)) (val %2))
                   (merge {:kind (.getKind entity) :key (.getKey entity)})
                   (.entrySet (.getProperties entity))))

(defmulti serialize-entity
  "Converts a map into an entity. The kind of the entity is determined
by one of the :key or the :kind keys, which must be in the map.

Examples:

  (serialize-entity {:kind \"person\" :name \"Bob\"})
  ; => #<Entity <Entity [person(no-id-yet)]:
  ;        name = Bob

  (serialize-entity {:key (make-key \"continent\" \"eu\") :name \"Europe\"})
  ; => #<Entity <Entity [continent(\"eu\")]:
  ;        name = Europe
"
  (fn [map]
    (if-let [key (:key map)]
      (.getKind key)
      (:kind map))))

(defmethod serialize-entity :default [map]
  (reduce #(do (.setProperty %1 (stringify (first %2)) (serialize-property (second %2) nil)) %1)
          (Entity. (or (:key map) (:kind map)))
          (dissoc map :key :kind)))

(defn- define-finder [entity property-specs]
  (let [kind# (entity-kind-name entity)
        properties# (map (comp keyword first) property-specs)
        serializers# (extract-serializer property-specs)]
    `(do
       (defn ~(find-entities-fn-sym entity) ~(find-entities-fn-doc entity) [& ~'options]
         (select ~kind#))
       ~@(for [property# properties#]
           `(defn ~(find-entities-by-property-fn-sym entity property#)
              ~(find-entities-by-property-fn-doc entity property#)
              [~'value & ~'options]
              (select
               ~kind#
               ~'where (= ~property# (types/serialize ~(property# serializers#) ~'value))))))))

(defn- define-deserialization [entity property-specs]
  (let [deserializers# (extract-deserializer property-specs)
        kind# (entity-kind-name entity)
        entity# (symbol (entity-kind-name entity))
        properties# (map (comp keyword first) property-specs)]
    `(defmethod ~'appengine.datastore.entities/deserialize-entity ~kind# [~'entity]
                (new ~entity
                     (.getKey ~'entity)
                     (.getKind ~'entity)
                     ~@(for [property# properties#]
                         `(deserialize-property
                           (.getProperty ~'entity ~(stringify property#))
                           ~(property# deserializers#)))))))

(defn- define-serialization [entity property-specs]
  (let [kind# (entity-kind-name entity)        
        properties# (map (comp keyword first) property-specs)
        serializers# (extract-serializer property-specs)]
    `(defmethod ~'appengine.datastore.entities/serialize-entity ~kind# [~'map]
                (doto (Entity. (or (:key ~'map) (:kind ~'map)))
                  ~@(for [property# properties#]
                      `(.setProperty
                        ~(stringify property#)
                        (serialize-property (~property# ~'map) ~(property# serializers#))))))))

(defn- define-protocol [entity parent]
  (let [entity-sym (symbol (entity-kind-name entity))
        parent-sym (if parent (symbol (entity-kind-name parent)))
        arglist (if parent `(~parent-sym ~entity-sym) `(~entity-sym))]
    `(defprotocol ~(symbol (entity-protocol-name entity))
       (~(entity-p-fn-sym entity) [~entity-sym] ~(entity-p-fn-doc entity))
       (~(key-name-fn-sym entity) [~entity-sym] ~(key-name-fn-doc entity))
       (~(key-fn-sym entity)      [~@arglist] ~(key-fn-doc entity))
       (~(entity-fn-sym entity)   [~@arglist] ~(entity-fn-doc entity)))))

(defn- define-record [entity properties options]
  (let [entity-sym (symbol (entity-kind-name entity))
        properties (map #(symbol (replace-re #"^:*" "" (str %))) properties)]
    `(defrecord ~entity [~'key ~'kind ~@properties]
       EntityProtocol
       (create-entity [~entity-sym] (create-entity (serialize ~entity-sym)))
       (delete-entity [~entity-sym] (delete-entity (:key ~entity-sym)))
       (save-entity   [~entity-sym] (save-entity (serialize ~entity-sym)))
       (find-entity   [~entity-sym] (find-entity (serialize ~entity-sym)))
       (update-entity [~entity-sym ~'key-vals] (save-entity (merge ~entity-sym ~'key-vals)))
       SerializationProtocol
       (deserialize [~entity-sym] ~entity-sym)
       (serialize   [~entity-sym] (serialize-entity ~entity-sym)))))

(defn- extend-entity [entity]
  (let [kind# (entity-kind-name entity) entity-sym (symbol kind#)]    
    `(extend-type Entity
       ~(symbol (entity-protocol-name entity))
       (~(entity-p-fn-sym entity) [~entity-sym]
        (= (.getKind ~entity-sym) ~kind#)))))

(defn- extend-key [entity parent properties]
  (let [kind# (entity-kind-name entity)
        entity-sym (symbol kind#)
        parent-sym (if parent (symbol (entity-kind-name parent)))]
    `(extend-type Key
       ~(symbol (entity-protocol-name entity))
       ~@(if parent
           `((~(key-fn-sym entity) [~parent-sym ~entity-sym]          
              (if-let [~'key (~(key-name-fn-sym entity) ~entity-sym)]
                (make-key ~parent-sym ~kind# ~'key)))
             (~(entity-fn-sym entity) [~parent-sym ~entity-sym]          
              (new ~entity (~(key-fn-sym entity) ~parent-sym ~entity-sym) ~kind#
                   ~@(map (fn [key#] `(~key# ~entity-sym)) properties))))))))

(defn- extend-nil [entity]
  (let [kind# (entity-kind-name entity) entity-sym (symbol kind#)]    
    `(extend-type nil
       ~(symbol (entity-protocol-name entity))
       (~(entity-p-fn-sym entity) [~entity-sym]
        false))))

(defn- extend-object [entity]
  (let [kind# (entity-kind-name entity) entity-sym (symbol kind#)]    
    `(extend-type Object
       ~(symbol (entity-protocol-name entity))
       (~(entity-p-fn-sym entity) [~entity-sym]
        false))))

(defn- extend-parent [entity parent properties]
  (let [kind# (entity-kind-name entity)
        entity-sym (symbol kind#)
        parent-sym (if parent (symbol (entity-kind-name parent)))]
    (if parent
      `(extend-type ~parent
         ~(symbol (entity-protocol-name entity))
         (~(key-fn-sym entity) [~parent-sym ~entity-sym]          
          (if-let [~'key (~(key-name-fn-sym entity) ~entity-sym)]
            (make-key ~parent-sym ~kind# ~'key)))
         (~(entity-fn-sym entity) [~parent-sym ~entity-sym]          
          (new ~entity (~(key-fn-sym entity) ~parent-sym ~entity-sym) ~kind#
               ~@(map (fn [key#] `(~key# ~entity-sym)) properties)))))))

(defn- extend-persistent-map [entity parent properties key-fns]
  (let [kind# (entity-kind-name entity)
        entity-sym (symbol kind#)
        parent-sym (if parent (symbol (entity-kind-name parent)))]
    `(extend-type IPersistentMap
       ~(symbol (entity-protocol-name entity))
       (~(entity-p-fn-sym entity) [~entity-sym]
        (= (:kind ~entity-sym) ~kind#))
       (~(key-name-fn-sym entity) [~entity-sym]          
        (extract-key ~entity-sym ~key-fns))
       ~@(if-not parent
           `((~(key-fn-sym entity) [~entity-sym]          
              (if-let [~'key (~(key-name-fn-sym entity) ~entity-sym)]
                (make-key nil ~kind# ~'key)))
             (~(entity-fn-sym entity) [~entity-sym]          
              (new ~entity (~(key-fn-sym entity) ~entity-sym) ~kind#
                   ~@(map (fn [key#] `(~key# ~entity-sym)) properties))))))))

(defmacro defentity
  "A macro to define entitiy records.

Examples:

  (defentity Continent ()
    ((iso-3166-alpha-2 :key lower-case :serialize lower-case)
     (location :serialize GeoPt)
     (name)))
  ; => (user.Continent)

  (continent {:name \"Europe\" :iso-3166-alpha-2 \"eu\"})
  ; => #:user.Continent{:key #<Key user.Continent(\"eu\")>, :kind \"user.Continent\",
                        :iso-3166-alpha-2 \"eu\", :location nil, :name \"Europe\"}

  (defentity Country (Continent)
    ((iso-3166-alpha-2 :key lower-case :serialize lower-case)
     (location :serialize GeoPt)
     (name)))

  (country (continent {:iso-3166-alpha-2 \"eu\" :name \"Europe\"})
           {:iso-3166-alpha-2 \"de\" :name \"Germany\"})
  ; => #:user.Country{:key #<Key continent(\"eu\")/country(\"de\")>, :kind country,
                      :iso-3166-alpha-2 de, :location nil, :name Germany}
"
  [entity [parent] property-specs & options]
  (let [key-fns# (extract-key-fns property-specs)
        properties# (map (comp keyword first) property-specs)
        options (apply hash-map options)]
    `(do
       ~(define-record entity properties# options)
       ~(define-protocol entity parent)
       ~(define-deserialization entity property-specs)
       ~(define-serialization entity property-specs)
       ~(define-finder entity property-specs)
       ~(extend-entity entity)
       ~(extend-key entity parent properties#)
       ~(extend-nil entity)
       ~(extend-object entity)
       ~(extend-parent entity parent properties#)
       ~(extend-persistent-map entity parent properties# key-fns#))))

(extend-type Entity
  EntityProtocol
  (create-entity [entity] (save-entity (assert-new entity)))
  (delete-entity [entity] (delete-entity (.getKey entity)))
  (find-entity   [entity] (find-entity (.getKey entity)))
  (save-entity   [entity] (deserialize (datastore/put entity)))
  (update-entity [entity key-vals] (update-entity (deserialize entity) key-vals))
  SerializationProtocol
  (deserialize [entity] (deserialize-entity entity))
  (serialize   [entity] entity))

(extend-type IPersistentMap
  EntityProtocol
  (create-entity [map] (create-entity (serialize map)))
  (delete-entity [map] (delete-entity (serialize map)))
  (save-entity   [map] (save-entity (serialize map)))
  (find-entity   [map] (find-entity (serialize map)))
  (update-entity [map key-vals] (save-entity (merge map key-vals)))
  SerializationProtocol
  (deserialize [map] map)
  (serialize   [map] (serialize-entity map)))
