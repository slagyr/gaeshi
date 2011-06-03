(ns gaeshi.datastore-spec
  (:use
    [speclj.core]
    [gaeshi.datastore]
    [gaeshi.spec-helpers.datastore]
    ;    [appengine.datastore]
    ))

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

    )

  )


(run-specs)