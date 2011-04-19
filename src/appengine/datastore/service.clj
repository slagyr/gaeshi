<(ns #^{:author "Roman Scherer"
       :doc "Low-level API for the Google App Engine datastore service."}
  appengine.datastore.service
  (:refer-clojure :exclude (get))
  (:import (com.google.appengine.api.datastore
            DatastoreService DatastoreServiceFactory DatastoreServiceConfig 
            DatastoreServiceConfig$Builder Entity EntityNotFoundException Key Transaction Query))
  (:use [clojure.contrib.def :only (defvar)]))

(defprotocol Datastore
  (delete [entity]
          "Delete the entity, key or sequence of entities and
  keys from the datastore.")

  (get [entity]
       "Get the entity, key or sequence of entities and keys from the
  datastore.

Examples:

  (get (make-key \"continent\" \"eu\"))
  ; => nil

  (put (make-key \"continent\" \"eu\"))
  ; => #<Entity <Entity [continent(\"eu\")]:>>

  (get (make-key \"continent\" \"eu\"))
  ; => #<Entity <Entity [continent(\"eu\")]:>>")

  (put [entity]
       "Put the entity, key or sequence of entities and keys to the
datastore.

Examples:

  (put (Entity. \"person\"))
  ; => #<Entity <Entity [person(1)]:>>

  (put (Entity. (make-key \"continent\" \"eu\")))
  ; => #<Entity <Entity [continent(\"eu\")]:>>"))

(defn ^DatastoreService datastore
  "Returns a DatastoreService with the default or the provided
  configuration.

Examples:

  (datastore)
  ; => #<DatastoreServiceImpl DatastoreServiceImpl@a7b68a>

  (datastore DatastoreServiceConfig$Builder/withDefaults)
  ; => #<DatastoreServiceImpl DatastoreServiceImpl@a7b68a>"
  [ & [configuration]]
  (DatastoreServiceFactory/getDatastoreService
   (or configuration (DatastoreServiceConfig$Builder/withDefaults))))

(defn datastore?
  "Returns true if arg is a datastore, else false."
  [arg] (isa? (class arg) DatastoreService))

(defn active-transactions
  "Returns all transactions started by this thread upon which no
  attempt to commit or rollback has been made."
  [] (.getActiveTransactions (datastore)))

(defn begin-transaction
  "Begin a datastore transaction."
  [] (.beginTransaction (datastore)))

(defn current-transaction
  "Returns the current datastore transaction, or nil if not within a
  transaction."  [] 
  (.getCurrentTransaction (datastore) nil))

(defn commit-transaction
  "Commits the transaction. If no transaction is given"
  [& [#^Transaction transaction]]
  (if-let [transaction (or transaction (current-transaction))]
    (.commit transaction)))

(defn get-entity
  "Get the entity identified by key from the datastore. If an entity
with the given key was found in the datastore the function returns an
Entity instance, otherwise nil.

Examples:

  (get-entity (make-key \"continent\" \"eu\"))
  ; => nil

  (def *europe* (put-entity (Entity. (make-key \"continent\" \"eu\"))))
  ; => #<Entity <Entity [continent(\"eu\")]:>>

  (get-entity (.getKey *europe*))
  ; => #<Entity <Entity [continent(\"eu\")]:>>"
  [#^Key key]
  (if key
    (try
      (if (and key (.isComplete key))
        (.get (datastore) (current-transaction) key))
      (catch EntityNotFoundException _ nil))))

(defn delete-entity
  "Delete the entity identified by key from the datastore. The
function always returns the given key, even if the entity identified
by the key was not found in the datastore.

Examples:

  (delete-entity (make-key \"continent\" \"eu\"))
  ; => #<Key continent(\"eu\")>

  (def *europe* (put-entity (Entity. (make-key \"continent\" \"eu\"))))
  ; => #<Entity <Entity [continent(\"eu\")]:>>

  (delete-entity (.getKey *europe*))
  ; => #<Key continent(\"eu\")>"
  [#^Key key]
  (if key (.delete (datastore) (current-transaction) (into-array [key]))) key)

(defn put-entity
  "Put the entity into the datastore and return the updated entity.

Examples:

  (put-entity (Entity. \"person\"))
  ; => #<Entity <Entity [person(1)]:>>

  (put-entity (Entity. (make-key \"continent\" \"eu\")))
  ; => #<Entity <Entity [continent(\"eu\")]:>>"
  [#^Entity entity]
  (if entity (do (.put (datastore) (current-transaction) entity) entity)))

(defn prepare-query
  "Prepares a query for execution. This function returns a PreparedQuery
which can be used to execute and retrieve results from the datastore
for query.

Example:

  (prepare-query (Query. \"continent\"))
  ; => #<PreparedQueryImpl SELECT * FROM continent>"
  [#^Query query]
  (if query (.prepare (datastore) (current-transaction) query)))

(extend-type Entity
  Datastore
  (delete [entity] (delete-entity (.getKey entity)))
  (get [entity] (get-entity (.getKey entity)))
  (put [entity] (put-entity entity)))

(extend-type Key
  Datastore
  (delete [key] (delete-entity key))
  (get [key] (get-entity key))
  (put [key] (put-entity (Entity. key))))
