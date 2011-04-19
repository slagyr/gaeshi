(ns appengine.datastore.utils
  (:use [appengine.datastore.protocols :only (find-entity)]))

(defn assert-new [entity]
  (if-let [found (first (find-entity entity))]
    (throw (Exception. (str "Can't create entity." " Already existing: " entity)))
    entity))
