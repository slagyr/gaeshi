(ns gaeshi.blobstore
  (:import
    [com.google.appengine.api.blobstore BlobstoreServiceFactory]
    [com.google.appengine.api.files FileServiceFactory]))

(def blobstore-service-instance (atom nil))
(def file-service-instance (atom nil))

(defn blobstore-service []
  (when (nil? @blobstore-service-instance)
    (reset! blobstore-service-instance (BlobstoreServiceFactory/getBlobstoreService)))
  @blobstore-service-instance)

(defn file-service []
  (when (nil? @file-service-instance)
    (reset! file-service-instance (FileServiceFactory/getFileService)))
  @file-service-instance)

(defn blobstore-upload-url [success-path]
  (.createUploadUrl (blobstore-service) success-path))

