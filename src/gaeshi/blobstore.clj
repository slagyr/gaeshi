(ns gaeshi.blobstore
  (:import
    [com.google.appengine.api.blobstore BlobstoreServiceFactory]))

(def blobstore-service-instance (atom nil))

(defn blobstore-service []
  (when (nil? @blobstore-service-instance)
    (reset! blobstore-service-instance (BlobstoreServiceFactory/getBlobstoreService)))
  @blobstore-service-instance)

(defn blobstore-upload-url [success-path]
  (.createUploadUrl (blobstore-service) success-path))

