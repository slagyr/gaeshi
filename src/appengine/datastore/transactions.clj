(ns #^{:author "Roman Scherer"
       :doc "The transaction API for Google App Engine's datastore service." }
    appengine.datastore.transactions
    (:use [clojure.contrib.def :only (defvar)])
    (:require [appengine.datastore.service :as datastore]))

(defprotocol Transaction
  (active?  [transaction] "Returns true if the transaction is active, false otherwise.")
  (commit   [transaction] "Commits the transaction.")
  (rollback [transaction] "Rolls back the transaction."))

(defn transaction?
  "Returns true id arg is a transaction, false otherwise."
  [arg] (isa? (class arg) com.google.appengine.api.datastore.Transaction))

(defmacro with-commit-transaction
  "Evaluate the forms within a datastore transaction. After the forms
  have been evaluated the transaction will be committed and the result
  of the last form is returned."
  [& body]
  `(let [transaction# (datastore/begin-transaction)]
     (let [result# (do ~@body)]
       (commit transaction#)
       result#)))

(defmacro with-rollback-transaction
  "Evaluate the forms within a datastore transaction. After the forms
  have been evaluated the transaction will be committed and the result
  of the last form is returned."
  [& body]
  `(let [transaction# (datastore/begin-transaction)]
     (try
       (let [result# (do ~@body)]
         (commit transaction#)
         result#)
       (catch Exception exception#
         (do (if (active? transaction#) (rollback transaction#))
             (throw exception#))))))

(extend-type com.google.appengine.api.datastore.Transaction
  Transaction
  (active?  [transaction] (.isActive transaction))
  (commit [transaction] (.commit transaction) transaction)
  (rollback [transaction] (.rollback transaction) transaction))
