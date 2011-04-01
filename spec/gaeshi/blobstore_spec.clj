(ns gaeshi.blobstore-spec
  (:use
    [speclj.core]
    [gaeshi.spec-helpers.blobstore]
    [gaeshi.blobstore]))

(describe "Blobstore"

  (with-local-blobstore)

  (it "provides the blobstore service"
    (should-not= nil (blobstore-service))
    (should-be-same (blobstore-service) (blobstore-service)))

  (it "generates an upload URL"
    (let [url (blobstore-upload-url "/landing")]
      (should-not= nil url)))
  )
