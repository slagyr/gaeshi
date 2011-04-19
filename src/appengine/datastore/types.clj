(ns #^{:author "Roman Scherer"}
  appengine.datastore.types
  (:import (com.google.appengine.api.datastore
            Blob Category Email Entity GeoPt IMHandle IMHandle$Scheme Link PhoneNumber
            PostalAddress Rating ShortBlob Text)
           (java.net MalformedURLException URL ))
  (:use [clojure.contrib.string :only (lower-case trim)]
        [clojure.contrib.seq :only (includes?)]
        [appengine.datastore.protocols :only (SerializationProtocol deserialize)]))

(extend-type Blob
  SerializationProtocol
  (deserialize [blob] (seq (.getBytes blob))))

(extend-type Category
  SerializationProtocol
  (deserialize [category] (.getCategory category)))

(extend-type Email
  SerializationProtocol
  (deserialize [email] (.getEmail email)))

(extend-type GeoPt
  SerializationProtocol
  (deserialize [location] {:latitude (.getLatitude location) :longitude (.getLongitude location)}))

(extend-type IMHandle
  SerializationProtocol
  (deserialize [handle] {:protocol (.getProtocol handle) :address (.getAddress handle)}))

(extend-type Link
  SerializationProtocol
  (deserialize [link] (.getValue link)))

(extend-type PhoneNumber
  SerializationProtocol
  (deserialize [phone-number] (.getNumber phone-number)))

(extend-type PostalAddress
  SerializationProtocol
  (deserialize [postal-address] (.getAddress postal-address)))

(extend-type Rating
  SerializationProtocol
  (deserialize [rating] (.getRating rating)))

(extend-type ShortBlob
  SerializationProtocol
  (deserialize [short-blob] (seq (.getBytes short-blob))))

(extend-type Text
  SerializationProtocol
  (deserialize [text] (.getValue text)))

(extend-type Object
  SerializationProtocol
  (deserialize [object] object))

(defmulti serialize
  "Serialize the value into the type.

Examples:

  (serialize Email \"info@example.com\")
  ; => #<Email com.google.appengine.api.datastore.Email@2d3de6b>

  (serialize GeoPt {:latitude 1 :longitude 2})
  ; => #<GeoPt 1.000000,2.000000>
"
  (fn [type value] type))

(defmethod serialize Blob [_ bytes]
  (Blob. (byte-array bytes)))

(defmethod serialize Email [_ address]
  (Email. address))

(defmethod serialize Category [_ category]
  (Category. category))

(defmethod serialize GeoPt [_ location]
  (GeoPt. (:latitude location) (:longitude location)))

(defmethod serialize IMHandle [_ {:keys [protocol address]}]
  (IMHandle.
   (try
     (URL. protocol)
     (catch MalformedURLException _
       (cond
        (= protocol "sip") IMHandle$Scheme/sip
        (= protocol "xmpp") IMHandle$Scheme/xmpp
        :else IMHandle$Scheme/unknown)))
   address))

(defmethod serialize Link [_ link]
  (Link. link))

(defmethod serialize PhoneNumber [_ phone-number]
  (PhoneNumber. (str phone-number)))

(defmethod serialize PostalAddress [_ postal-address]
  (PostalAddress. postal-address))

(defmethod serialize Rating [_ rating]
  (Rating. rating))

(defmethod serialize ShortBlob [_ bytes]
  (ShortBlob. (byte-array bytes)))

(defmethod serialize String [_ string]
  (str string))

(defmethod serialize Text [_ text]
  (Text. text))

(defmethod serialize :default [type value]
  value)
