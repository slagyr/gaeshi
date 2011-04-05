(ns gaeshi.blobstore-spec
  (:use
    [speclj.core]
    [gaeshi.spec-helpers.datastore]
    [gaeshi.blobstore])
  (:import
    [com.google.appengine.api.files AppEngineFile]
    [java.util Date]))

(describe "Blobstore"

  (with-local-datastore)

  (it "provides the blobstore service"
    (should-not= nil (blobstore-service))
    (should-be-same (blobstore-service) (blobstore-service)))

  (it "provides the file service"
    (should-not= nil (file-service))
    (should-be-same (file-service) (file-service)))

  (it "generates an upload URL"
    (let [url (blobstore-upload-url "/landing")]
      (should-not= nil url)))

  (it "gets from blobs from an empty blobstore"
    (should= 0 (count (blob-infos))))
  
  (it "writes a blob"
    (let [blob (create-blob "text/plain" "test.txt" "Foobar")]
      (should-not= nil blob)))

  (it "add a blob to the blobstore"
    (create-blob "text/plain" "test.txt" "Foobar")
    (should= 1 (count (blob-infos)))
    (let [blob (first (blob-infos))]
      (should= "test.txt" (:filename blob))
      (should= 6 (:size blob))
      (should= "text/plain" (:content-type blob))
      (should-not= nil (:created-at blob))
      (should= Date (class (:created-at blob)))
      (should-not= nil (:key blob))
      (should= String (class (:key blob)))))

  (it "deleted a blob"
    (create-blob "text/plain" "test.txt" "Foobar")
    (let [blob (first (blob-infos))]
      (delete-blob (:key blob)))
    (should= 0 (count (blob-infos))))
  )
