(ns gaeshi.spec-helpers.blobstore
  (:use
    [speclj.core])
  (:import
    [com.google.appengine.tools.development.testing
     LocalServiceTestConfig
     LocalDatastoreServiceTestConfig
     LocalBlobstoreServiceTestConfig
     LocalServiceTestHelper]
    [com.google.apphosting.api ApiProxy]
    [com.google.appengine.api NamespaceManager]))

(defn tear-down []
  (.stop (ApiProxy/getDelegate))
  (ApiProxy/clearEnvironmentForCurrentThread)
  )

(defn with-local-blobstore []
  (around [it]
    (try
      (.setUp (LocalServiceTestHelper. (into-array LocalServiceTestConfig [(LocalBlobstoreServiceTestConfig.) (LocalDatastoreServiceTestConfig.)])))
      (it)
      (finally (tear-down)))))

