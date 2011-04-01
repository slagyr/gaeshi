(ns gaeshi.spec-helpers.datastore
  (:use
    [speclj.core])
  (:import
    [com.google.appengine.tools.development.testing
     LocalDatastoreServiceTestConfig
     LocalServiceTestHelper]
    [com.google.apphosting.api ApiProxy]))

(defn tear-down []
  (ApiProxy/clearEnvironmentForCurrentThread)
  (.stop (ApiProxy/getDelegate)))

(defn with-local-datastore []
  (around [it]
    (try
      (.setUp (LocalServiceTestHelper. (into-array [(LocalDatastoreServiceTestConfig.)])))
      (it)
      (finally (tear-down)))))

