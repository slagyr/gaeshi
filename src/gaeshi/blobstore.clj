(ns gaeshi.blobstore
  (:use
    [clojure.java.io :only (copy)]
    [appengine.datastore.service :only (datastore)])
  (:import
    [com.google.appengine.api.blobstore BlobstoreServiceFactory BlobInfoFactory]
    [com.google.appengine.api.files FileServiceFactory]
    [java.nio.channels Channels]))

(def blobstore-service-instance (atom nil))
(def file-service-instance (atom nil))
(def blob-info-factory-instance (atom nil))

(defn blobstore-service []
  (when (nil? @blobstore-service-instance)
    (reset! blobstore-service-instance (BlobstoreServiceFactory/getBlobstoreService)))
  @blobstore-service-instance)

(defn file-service []
  (when (nil? @file-service-instance)
    (reset! file-service-instance (FileServiceFactory/getFileService)))
  @file-service-instance)

(defn blob-info-factory []
  (when (nil? @blob-info-factory-instance)
    (reset! blob-info-factory-instance (BlobInfoFactory. (datastore))))
  @blob-info-factory-instance)

(defn blobstore-upload-url [success-path]
  (.createUploadUrl (blobstore-service) success-path))

(defn blob-info->map [blob-info]
  (if (not blob-info)
    nil
    {:filename (.getFilename blob-info)
     :size (.getSize blob-info)
     :content-type (.getContentType blob-info)
     :created-at (.getCreation blob-info)
     :key (.getBlobKey blob-info)}))

(defn blob-infos []
  (let [iterator (.queryBlobInfos (blob-info-factory))]
    (map blob-info->map (iterator-seq iterator))))

(defn write-blob [content-type filename source]
  (let [file (.createNewBlobFile (file-service) content-type filename)
        channel (.openWriteChannel (file-service) file true)
        out (Channels/newOutputStream channel)]
    (with-open [out out]
      (copy source out))
    (.closeFinally channel)
    file))

(defn serve-blob [key reseponse]
  (let [blob-key (BlobKey. key)]
    (.serve (blobstore-service) blob-key response)))

