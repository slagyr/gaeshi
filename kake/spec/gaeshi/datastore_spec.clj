(ns gaeshi.datastore-spec
  (:use
    [speclj.core]
    [gaeshi.datastore]
    [gaeshi.spec-helpers.datastore]
    [clojure.string :only (upper-case)])
  (:import
    [com.google.appengine.api.datastore ShortBlob Blob Category Email GeoPt Link IMHandle PostalAddress Rating PhoneNumber Text]
    [com.google.appengine.api.users User]
    [com.google.appengine.api.blobstore BlobKey]))

(defentity Hollow)

(defentity OneField
  [field])

(defentity ManyFields
  [field1]
  [field2]
  [field42])

(defentity ManyDefaultedFields
  [field1]
  [field2]
  [field3 :default ".141592"]
  [field42 :default "value42"])

(defentity VarietyShow
  [short-blob :type ShortBlob]
  [blob :type Blob]
  [category :type Category]
  [email :type Email]
  [geo-pt :type GeoPt]
  [user :type User]
  [blob-key :type BlobKey]
  [link :type Link]
  [imhandle :type IMHandle]
  [address :type PostalAddress]
  [rating :type Rating]
  [phone :type PhoneNumber]
  [text :type Text])

(defentity CustomPacking
  [bauble :packer #(apply str (reverse %)) :unpacker upper-case])


(describe "Datastore"
  (it "uses dashes in spear-case"
    (should= "normal-name" (spear-case "NormalName"))
    (should= "one" (spear-case "One"))
    (should= "one-two" (spear-case "OneTwo"))
    (should= "one-two-three" (spear-case "OneTwo-Three"))
    (should= "four" (spear-case "FOUR"))
    (should= "fou-r" (spear-case "FOuR"))
    (should= "fi-ve" (spear-case "FI_VE")))

  (it "defines simple entities"
    (let [instance (hollow)]
      (should= `Hollow (symbol (.getName (class instance))))))

  (it "defines entity with one field"
    (let [instance (one-field :field "value")]
      (should= `OneField (symbol (.getName (class instance))))
      (should= "value" (:field instance))))

  (it "defines an entity with multiple field"
    (let [instance (many-fields :field1 "value" :field2 "value2" :field42 "value42")]
      (should= `ManyFields (symbol (.getName (class instance))))
      (should= "value" (:field1 instance))
      (should= "value2" (:field2 instance))
      (should= "value42" (:field42 instance))))

  (it "defines an entity with some fields with default values"
    (let [instance (many-defaulted-fields :field1 "value" :field2 "value2")]
      (should= "value" (:field1 instance))
      (should= "value2" (:field2 instance))
      (should= ".141592" (:field3 instance))
      (should= "value42" (:field42 instance))))

  (it "extra fields in constructor are included"
    (should= "foo" (:foo (many-fields :foo "foo")))
    (should= "foo" (:foo (many-defaulted-fields :foo "foo"))))

  (context "with service"

    (with-local-datastore)

    (it "provides the datastore service"
      (should-not= nil (datastore-service))
      (should-be-same (datastore-service) (datastore-service)))

    (it "saves and loads a hollow entity"
      (let [unsaved (hollow)
            saved (save unsaved)]
        (should-not= nil (:key saved))
        (let [loaded (find-by-key (:key saved))]
          (should= (:key saved) (:key loaded))
          (should-not-be-same saved loaded))))

    (it "can set values while saving"
      (let [unsaved (one-field)
            saved (save unsaved :field "kia")]
        (should= "kia" (:field saved))
        (should= "kia" (:field (find-by-key (:key saved))))))

    (it "can update existing enitity"
      (let [saved (save (one-field :field "giyup"))
            updated (save saved :field "kia")]
        (should= (:key saved) (:key updated))
        (should= "kia" (:field updated))
        (should= "kia" (:field (find-by-key (:key saved))))))

    (context "searching"

      (it "finds by kind"
        (should= [] (find-by-kind "hollow"))
        (let [saved (save (hollow))]
          (should= [saved] (find-by-kind "hollow"))
          (let [saved2 (save (hollow))]
            (should= [saved saved2] (find-by-kind "hollow")))))

      (it "finds by kind with multiple kinds"
        (let [saved-hollow (save (hollow))
              saved-one (save (one-field))
              saved-many (save (many-fields))]
          (should= [saved-hollow] (find-by-kind "hollow"))
          (should= [saved-one] (find-by-kind 'one-field))
          (should= [saved-many] (find-by-kind :many-fields))))

      (it "handles filters to find-by-kind"
        (let [one (save (one-field :field 1))
              five (save (one-field :field 5))
              ten (save (one-field :field 10))]
          (should= [one five ten] (find-by-kind :one-field))
          (should= [one] (find-by-kind :one-field :filters [(= :field 1)]))
          (should= [one] (find-by-kind :one-field :filters [(< :field 5)]))
          (should= [one five] (find-by-kind :one-field :filters [(<= :field 5)]))
          (should= [ten] (find-by-kind :one-field :filters [(> :field 5)]))
          (should= [five ten] (find-by-kind :one-field :filters [(>= :field 5)]))
          (should= [one ten] (find-by-kind :one-field :filters [(not :field 5)]))
          (should= [five] (find-by-kind :one-field :filters [(contains? :field [4 5 6])]))
          (should= [five] (find-by-kind :one-field :filters [(> :field 1) (< :field 10)]))
          (should= [] (find-by-kind :one-field :filters [(> :field 1) (< :field 10) (not :field 5)]))))

      (it "handles sort order to find-by-kind"
        (let [three (save (many-fields :field1 3 :field2 "odd"))
              one (save (many-fields :field1 1 :field2 "odd"))
              four (save (many-fields :field1 4 :field2 "even"))
              five (save (many-fields :field1 5 :field2 "odd"))
              nine (save (many-fields :field1 9 :field2 "odd"))
              two (save (many-fields :field1 2 :field2 "even"))]
          (should= [one two three four five nine] (find-by-kind "many-fields" :sorts [(:field1 :asc)]))
          (should= [nine five four three two one] (find-by-kind "many-fields" :sorts [(:field1 :desc)]))
          (should= [three one five nine four two] (find-by-kind "many-fields" :sorts [(:field2 "desc")]))
          (should= [four two three one five nine] (find-by-kind "many-fields" :sorts [(:field2 "asc")]))
          (should= [two four one three five nine] (find-by-kind "many-fields" :sorts [(:field2 "asc") (:field1 :asc)]))))
      )

    (context "handles data types:"
      (it "ShortBlob"
        (let [saved (save (variety-show :short-blob (.getBytes "Short" "UTF-8")))
              raw (.get (datastore-service) (:key saved))]
          (should= ShortBlob (class (.getProperty raw "short-blob")))
          (should= "Short" (String. (.getBytes (.getProperty raw "short-blob"))))
          (should= "Short" (String. (:short-blob (find-by-key (:key saved)))))))

      (it "Blob"
        (let [saved (save (variety-show :blob (.getBytes "Blob" "UTF-8")))
              raw (.get (datastore-service) (:key saved))]
          (should= Blob (class (.getProperty raw "blob")))
          (should= "Blob" (String. (.getBytes (.getProperty raw "blob"))))
          (should= "Blob" (String. (:blob (find-by-key (:key saved)))))))

      (it "Category"
        (let [saved (save (variety-show :category "red"))
              raw (.get (datastore-service) (:key saved))]
          (should= Category (class (.getProperty raw "category")))
          (should= "red" (.getCategory (.getProperty raw "category")))
          (should= "red" (:category (find-by-key (:key saved))))))

      (it "Email"
        (let [saved (save (variety-show :email "joe@blow.com"))
              raw (.get (datastore-service) (:key saved))]
          (should= Email (class (.getProperty raw "email")))
          (should= "joe@blow.com" (.getEmail (.getProperty raw "email")))
          (should= "joe@blow.com" (:email (find-by-key (:key saved))))))

      (it "GeoPt"
        (let [saved (save (variety-show :geo-pt {:latitude 12.34 :longitude 56.78}))
              raw (.get (datastore-service) (:key saved))
              loaded (find-by-key (:key saved))]
          (should= GeoPt (class (.getProperty raw "geo-pt")))
          (should= 12.34 (.getLatitude (.getProperty raw "geo-pt")) 0.01)
          (should= 56.78 (.getLongitude (.getProperty raw "geo-pt")) 0.01)
          (should= 12.34 (:latitude (:geo-pt loaded)) 0.01)
          (should= 56.78 (:longitude (:geo-pt loaded)) 0.01)))

      (it "User"
        (let [saved (save (variety-show :user {:email "joe@blow.com" :auth-domain "gmail.com" :user-id "1234567890"}))
              raw (.get (datastore-service) (:key saved))
              loaded (find-by-key (:key saved))]
          (should= User (class (.getProperty raw "user")))
          (should= "joe@blow.com" (.getEmail (.getProperty raw "user")))
          (should= "joe@blow.com" (:email (:user loaded)))))

      (it "BlobKey"
        (let [saved (save (variety-show :blob-key "4321"))
              raw (.get (datastore-service) (:key saved))]
          (should= BlobKey (class (.getProperty raw "blob-key")))
          (should= "4321" (.getKeyString (.getProperty raw "blob-key")))
          (should= "4321" (:blob-key (find-by-key (:key saved))))))

      (it "Link"
        (let [saved (save (variety-show :link "http://gaeshi.org"))
              raw (.get (datastore-service) (:key saved))]
          (should= Link (class (.getProperty raw "link")))
          (should= "http://gaeshi.org" (.getValue (.getProperty raw "link")))
          (should= "http://gaeshi.org" (:link (find-by-key (:key saved))))))

      (it "IMHandle"
        (let [saved (save (variety-show :imhandle {:protocol "sip" :address "somewhere"}))
              raw (.get (datastore-service) (:key saved))]
          (should= IMHandle (class (.getProperty raw "imhandle")))
          (should= "somewhere" (.getAddress (.getProperty raw "imhandle")))
          (should= {:protocol "sip" :address "somewhere"} (:imhandle (find-by-key (:key saved))))))

      (it "PostalAddress"
        (let [saved (save (variety-show :address "123 Elm"))
              raw (.get (datastore-service) (:key saved))]
          (should= PostalAddress (class (.getProperty raw "address")))
          (should= "123 Elm" (.getAddress (.getProperty raw "address")))
          (should= "123 Elm" (:address (find-by-key (:key saved))))))

      (it "Rating"
        (let [saved (save (variety-show :rating 42))
              raw (.get (datastore-service) (:key saved))]
          (should= Rating (class (.getProperty raw "rating")))
          (should= 42 (.getRating (.getProperty raw "rating")))
          (should= 42 (:rating (find-by-key (:key saved))))))

      (it "PhoneNumber"
        (let [saved (save (variety-show :phone "555-867-5309"))
              raw (.get (datastore-service) (:key saved))]
          (should= PhoneNumber (class (.getProperty raw "phone")))
          (should= "555-867-5309" (.getNumber (.getProperty raw "phone")))
          (should= "555-867-5309" (:phone (find-by-key (:key saved))))))

      (it "Text"
        (let [saved (save (variety-show :text "some text"))
              raw (.get (datastore-service) (:key saved))]
          (should= Text (class (.getProperty raw "text")))
          (should= "some text" (.getValue (.getProperty raw "text")))
          (should= "some text" (:text (find-by-key (:key saved))))))
      )

    (it "allows custom packing"
      (let [unsaved (custom-packing :bauble "hello")
            saved (save unsaved)
            raw (.get (datastore-service) (:key saved))
            loaded (find-by-key (:key saved))]
        (should= "hello" (:bauble unsaved))
        (should= "olleh" (.getProperty raw "bauble"))
        (should= "OLLEH" (:bauble loaded))))

    (it "can store multiple values in one field"
      (let [unsaved (one-field :field [1 2 3 4 5])
            saved (save unsaved)
            raw (.get (datastore-service) (:key saved))
            loaded (find-by-key (:key saved))]
        (should= [1 2 3 4 5] (:field loaded))
        (should= java.util.ArrayList (class (.getProperty raw "field")))
        (should= java.util.ArrayList (class (:field loaded)))))

    )
  )

(run-specs)
