(ns #^{:author "Roman Scherer"
       :doc "The datastore protocols."}
  appengine.datastore.protocols
  (:use appengine.utils))

(defprotocol LifecycleProtocol
  (after-create [record]
    "Callback fns to evaluate after creating the record.")
  (after-delete [record]
    "Callback fns to evaluate after deleting the record.")
  (after-save [record]
    "Callback fns to evaluate after saving the record.")
  (after-update [record]
    "Callback fns to evaluate after updating the record.")
  (after-validation [record]
    "Callback fns to evaluate after validation.")
  (after-validation-on-create [record]
    "Callback fns to evaluate after validating a new record.")
  (after-validation-on-update [record]
    "Callback fns to evaluate after validating an already saved record.")
  (before-create [record]
    "Callback fns to evaluate before creating the record.")
  (before-delete [record]
    "Callback fns to evaluate before deleting the record.")
  (before-save [record]
    "Callback fns to evaluate before saving the record.")
  (before-update [record]
    "Callback fns to evaluate before updating the record.")
  (before-validation [record]
    "Callback fns to evaluate before validating the record.")
  (before-validation-on-create [record]
    "Callback fns to evaluate before validating a new record.")
  (before-validation-on-update [record]
    "Callback fns to evaluate before validating an already saved record."))

(defprotocol EntityProtocol
  (create-entity [entity]
    "Create new entity in the datastore. If the entity already exists,
    the functions throws an exception.")
  (delete-entity [entity]
    "Delete the entity from the datastore.")
  (find-entity [entity]
    "Find the entity from the datastore.")
  (save-entity [entity]
    "Save the entity in the datastore.")
  (update-entity [entity key-vals]
    "Update the entity with the key-vals and save it in the
    datastore."))

(defprotocol QueryProtocol
  (execute [query] "Execute the query against the datastore.")
  (prepare [query] "Prepare a query for execution."))

(defprotocol SerializationProtocol
  (deserialize [object]
    "Deserialize an object into a clojure data structure.")
  (serialize [entity]
    "Serialize the entity into an entity."))

(defprotocol ValidationProtocol
  (validate-entity [entity]
    "Validate the entity."))

(extend-type clojure.lang.Seqable
  EntityProtocol
  (create-entity [entities] (map create-entity entities))
  (delete-entity [entities] (map delete-entity entities))
  (find-entity [entities] (map find-entity entities))
  (save-entity   [entities] (map save-entity entities))
  (update-entity [entities key-vals] (map #(update-entity % key-vals) entities)))

