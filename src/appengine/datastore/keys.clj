(ns #^{:author "Roman Scherer"
       :doc
       "Allows you to create arbitrary Key objects in the root
group (no parent). Also allows you to encode Key objects to and decode
them from strings. Clients should not make any assumptions about this
return value, except that it is a websafe string that does not need to
be quoted when used in HTML or in URLs."}
  appengine.datastore.keys
  (:import (com.google.appengine.api.datastore EntityNotFoundException Key KeyFactory))
  (:use [appengine.datastore.utils :only (assert-new)]
        appengine.datastore.protocols)
  (:require [appengine.datastore.service :as datastore]))

(defn key?
  "Returns true if arg is a Key, else false."
  [arg] (isa? (class arg) Key))

(defn make-key
  "Creates a new Key using the given kind and identifier. If parent-key is
given, the new key will be a child of the parent-key.
  
Examples:

  (make-key \"country\" \"de\")
  ; => #<Key country(\"de\")>
 	
  (make-key (make-key \"continent\" \"eu\") \"country\" \"de\")
  ; => #<Key continent(\"eu\")/country(\"de\")>"
  ([kind identifier]
     (make-key nil kind identifier))
  ([#^Key parent-key kind identifier]
     (KeyFactory/createKey
      (if (key? parent-key) parent-key (:key parent-key))
      kind 
      (if (integer? identifier) (Long/valueOf (str identifier)) 
          (str identifier)))))

(defn key->string
  "Returns a \"websafe\" string from the given Key.

Examples:

  (key->string (make-key \"continent\" \"eu\"))
  ; => \"agR0ZXN0chELEgljb250aW5lbnQiAmV1DA\"

  (key->string (make-key (make-key \"continent\" \"eu\") \"country\" \"de\")
  ; => \"agR0ZXN0ciALEgljb250aW5lbnQiAmV1DAsSB2NvdW50cnkiAmRlDA\""
  [#^Key key] (KeyFactory/keyToString key))

(defn string->key
  "Returns a Key from the given \"websafe\" string.

Examples:

  (string->key \"agR0ZXN0chELEgljb250aW5lbnQiAmV1DA\")
  ; => #<Key country(\"de\")>

  (string->key \"agR0ZXN0ciALEgljb250aW5lbnQiAmV1DAsSB2NvdW50cnkiAmRlDA\")
  ; => #<Key continent(\"eu\")/country(\"de\")>"
  [string] (KeyFactory/stringToKey string))

(extend-type Key
  EntityProtocol
  (create-entity [key]
    (assert-new key)
    (save-entity key))
  (delete-entity [key]
    (datastore/delete-entity key))
  (save-entity [key]
    (deserialize (datastore/put-entity (com.google.appengine.api.datastore.Entity. key) )))
  (find-entity [key]          
          (try (if-let [entity (datastore/get-entity key)]
                 (deserialize entity))
         (catch EntityNotFoundException _ nil)))
  (update-entity [key key-vals]
    (if-let [entity (datastore/get-entity key)] ; TODO: Don't call get-entity
      (update-entity entity key-vals)))) 
