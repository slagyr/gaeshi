(ns gaeshi.datastore.types-spec
  (:use
    [speclj.core]
    [gaeshi.datastore.types])
  (:import
    [com.google.appengine.api.datastore ShortBlob Blob Category Email GeoPt Link IMHandle IMHandle$Scheme PostalAddress Rating PhoneNumber Text]
    [com.google.appengine.api.users User]
    [com.google.appengine.api.blobstore BlobKey]
    [java.net URL]))

(describe "Datastore Types"

  (it "handles ShortBlob"
    (let [packed (pack ShortBlob (.getBytes "Hello" "UTF-8"))]
      (should= ShortBlob (class packed))
      (should-be-same packed (pack ShortBlob packed))
      (should= "Hello" (String. (.getBytes packed)))
      (should= "Hello" (String. (unpack packed)))
      (should= nil (pack ShortBlob nil))))

  (it "handles Blob"
    (let [packed (pack Blob (.getBytes "Hello" "UTF-8"))]
      (should= Blob (class packed))
      (should-be-same packed (pack Blob packed))
      (should= "Hello" (String. (.getBytes packed)))
      (should= "Hello" (String. (unpack packed)))
      (should= nil (pack Blob nil))))

  (it "handles Category"
    (let [packed (pack Category "blue")]
      (should= Category (class packed))
      (should-be-same packed (pack Category packed))
      (should= "blue" (.getCategory packed))
      (should= "blue" (unpack packed))
      (should= nil (pack Category nil))))

  (it "handles Email"
    (let [packed (pack Email "joe@blow.com")]
      (should= Email (class packed))
      (should-be-same packed (pack Email packed))
      (should= "joe@blow.com" (.getEmail packed))
      (should= "joe@blow.com" (unpack packed))
      (should= nil (pack Email nil))))

  (it "handles GeoPt"
    (let [packed (pack GeoPt {:latitude 12.34 :longitude 56.78})]
      (should= GeoPt (class packed))
      (should-be-same packed (pack GeoPt packed))
      (should= 12.34 (.getLatitude packed) 0.01)
      (should= 56.78 (.getLongitude packed) 0.01)
      (should= 12.34 (:latitude (unpack packed)) 0.01)
      (should= 56.78 (:longitude (unpack packed)) 0.01)
      (should= nil (pack GeoPt nil))))

  (it "handles User"
    (let [packed (pack User {:email "joe@blow.com" :auth-domain "gmail.com" :user-id "1234" :federated-identity "abcd"})]
      (should= User (class packed))
      (should-be-same packed (pack User packed))
      (should= "joe@blow.com" (.getEmail packed))
      (should= "gmail.com" (.getAuthDomain packed))
      (should= "1234" (.getUserId packed))
      (should= "abcd" (.getFederatedIdentity packed))
      (should= "joe@blow.com" (:email (unpack packed)))
      (should= "gmail.com" (:auth-domain (unpack packed)))
      (should= "1234" (:user-id (unpack packed)))
      (should= "abcd" (:federated-identity (unpack packed)))
      (should= nil (pack User nil))))

  (it "packs users with partial fields"
    (should-not-throw (pack User {:email "foo" :auth-domain "bar"}))
    (should-not-throw (pack User {:email "foo" :auth-domain "bar" :user-id "fizz"}))
    (should-not-throw (pack User {:email "foo" :auth-domain "bar" :user-id "fizz" :federated-identity "bang"})))

  (it "handles BlobKey"
    (let [packed (pack BlobKey "1234")]
      (should= BlobKey (class packed))
      (should-be-same packed (pack BlobKey packed))
      (should= "1234" (.getKeyString packed))
      (should= "1234" (unpack packed))
      (should= nil (pack BlobKey nil))))

  (it "handles BlobKey"
    (let [packed (pack Link "http://gaeshi.org")]
      (should= Link (class packed))
      (should-be-same packed (pack Link packed))
      (should= "http://gaeshi.org" (.getValue packed))
      (should= "http://gaeshi.org" (unpack packed))
      (should= nil (pack Link nil))))

  (it "handles IMHandle"
    (let [packed (pack IMHandle {:protocol "sip" :address "some_address"})]
      (should= IMHandle (class packed))
      (should-be-same packed (pack IMHandle packed))
      (should= "some_address" (.getAddress packed))
      (should= "sip" (.getProtocol packed))
      (should= {:protocol "sip" :address "some_address"} (unpack packed))
      (should= nil (pack IMHandle nil))))

  (it "parses IM protocols"
    (should= IMHandle$Scheme/sip (parse-im-protocol "sip"))
    (should= IMHandle$Scheme/sip (parse-im-protocol :sip))
    (should= IMHandle$Scheme/xmpp (parse-im-protocol :xmpp))
    (should= IMHandle$Scheme/unknown (parse-im-protocol :foo))
    (should= (URL. "http://gaeshi.org") (parse-im-protocol "http://gaeshi.org")))

  (it "handles PostalAddress"
    (let [packed (pack PostalAddress "123 Elm")]
      (should= PostalAddress (class packed))
      (should-be-same packed (pack PostalAddress packed))
      (should= "123 Elm" (.getAddress packed))
      (should= "123 Elm" (unpack packed))
      (should= nil (pack PostalAddress nil))))

  (it "handles Rating"
    (let [packed (pack Rating 42)]
      (should= Rating (class packed))
      (should-be-same packed (pack Rating packed))
      (should= 42 (.getRating packed))
      (should= 42 (unpack packed))
      (should= nil (pack Rating nil))))

  (it "handles PhoneNumber"
    (let [packed (pack PhoneNumber "555-867-5309")]
      (should= PhoneNumber (class packed))
      (should-be-same packed (pack PhoneNumber packed))
      (should= "555-867-5309" (.getNumber packed))
      (should= "555-867-5309" (unpack packed))
      (should= nil (pack PhoneNumber nil))))

  (it "handles Text"
    (let [packed (pack Text "some text")]
      (should= Text (class packed))
      (should-be-same packed (pack Text packed))
      (should= "some text" (.getValue packed))
      (should= "some text" (unpack packed))
      (should= nil (pack Text nil))))

  )
