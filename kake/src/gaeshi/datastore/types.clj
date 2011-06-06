(ns gaeshi.datastore.types
  (:import
    [com.google.appengine.api.datastore ShortBlob Blob Category Email GeoPt Link IMHandle IMHandle$Scheme PostalAddress Rating PhoneNumber Text]
    [com.google.appengine.api.users User]
    [com.google.appengine.api.blobstore BlobKey]
    [java.net URL]))

(defmulti pack (fn [packer value] packer))

(defmethod pack :default [packer value] value)

(defmethod pack ShortBlob [_ value]
  (cond
    (nil? value) nil
    (= ShortBlob (class value)) value
    :else (ShortBlob. value)))

(defmethod pack Blob [_ value]
  (cond
    (nil? value) nil
    (= Blob (class value)) value
    :else (Blob. value)))

(defmethod pack Category [_ value]
  (cond
    (nil? value) nil
    (= Category (class value)) value
    :else (Category. value)))

(defmethod pack Email [_ value]
  (cond
    (nil? value) nil
    (= Email (class value)) value
    :else (Email. value)))

(defmethod pack GeoPt [_ value]
  (cond
    (nil? value) nil
    (= GeoPt (class value)) value
    :else (GeoPt. (:latitude value) (:longitude value))))

(defmethod pack User [_ value]
  (cond
    (nil? value) nil
    (= User (class value)) value
    :else
    (cond
      (:federated-identity value) (User. (:email value) (:auth-domain value) (:user-id value) (:federated-identity value))
      (:user-id value) (User. (:email value) (:auth-domain value) (:user-id value))
      :else (User. (:email value) (:auth-domain value)))))

(defmethod pack BlobKey [_ value]
  (cond
    (nil? value) nil
    (= BlobKey (class value)) value
    :else (BlobKey. value)))

(defmethod pack Link [_ value]
  (cond
    (nil? value) nil
    (= Link (class value)) value
    :else (Link. value)))

(defn parse-im-protocol [protocol]
  (try
    (IMHandle$Scheme/valueOf (name protocol))
    (catch Exception e
      (try
        (URL. protocol)
        (catch Exception e
          IMHandle$Scheme/unknown)))))

(defmethod pack IMHandle [_ value]
  (cond
    (nil? value) nil
    (= IMHandle (class value)) value
    :else (IMHandle. (parse-im-protocol (:protocol value)) (:address value))))

(defmethod pack PostalAddress [_ value]
  (cond
    (nil? value) nil
    (= PostalAddress (class value)) value
    :else (PostalAddress. value)))

(defmethod pack Rating [_ value]
  (cond
    (nil? value) nil
    (= Rating (class value)) value
    :else (Rating. value)))

(defmethod pack PhoneNumber [_ value]
  (cond
    (nil? value) nil
    (= PhoneNumber (class value)) value
    :else (PhoneNumber. value)))

(defmethod pack Text [_ value]
  (cond
    (nil? value) nil
    (= Text (class value)) value
    :else (Text. value)))

(defprotocol Packable
  (unpack [this]))

(extend-type nil
  Packable
  (unpack [this] nil))

(extend-type ShortBlob
  Packable
  (unpack [this] (.getBytes this)))

(extend-type Blob
  Packable
  (unpack [this] (.getBytes this)))

(extend-type Category
  Packable
  (unpack [this] (.getCategory this)))

(extend-type Email
  Packable
  (unpack [this] (.getEmail this)))

(extend-type GeoPt
  Packable
  (unpack [this] {:latitude (.getLatitude this) :longitude (.getLongitude this)}))

(extend-type User
  Packable
  (unpack [this] {:email (.getEmail this)
                  :auth-domain (.getAuthDomain this)
                  :user-id (.getUserId this)
                  :federated-identity (.getFederatedIdentity this)}))

(extend-type BlobKey
  Packable
  (unpack [this] (.getKeyString this)))

(extend-type Link
  Packable
  (unpack [this] (.getValue this)))

(extend-type IMHandle
  Packable
  (unpack [this] {:protocol (.getProtocol this) :address (.getAddress this)}))

(extend-type PostalAddress
  Packable
  (unpack [this] (.getAddress this)))

(extend-type Rating
  Packable
  (unpack [this] (.getRating this)))

(extend-type PhoneNumber
  Packable
  (unpack [this] (.getNumber this)))

(extend-type Text
  Packable
  (unpack [this] (.getValue this)))





