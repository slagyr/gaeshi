(ns gaeshi.spec-helpers.datastore
  (:use
    [speclj.core])
  (:import
    [com.google.appengine.tools.development.testing
     LocalServiceTestConfig
     LocalDatastoreServiceTestConfig
     LocalBlobstoreServiceTestConfig
     LocalServiceTestHelper]
    [com.google.apphosting.api ApiProxy]))

(defn tear-down-local-datastore []
  (.stop (ApiProxy/getDelegate))
  (ApiProxy/clearEnvironmentForCurrentThread))

(defn set-up-local-datastore []
  (.setUp (LocalServiceTestHelper.
    (into-array LocalServiceTestConfig
      [(LocalBlobstoreServiceTestConfig.) (LocalDatastoreServiceTestConfig.)]))))

(defn with-local-datastore []
  (around [it]
    (try
      (set-up-local-datastore)
      (it)
      (finally (tear-down-local-datastore)))))
