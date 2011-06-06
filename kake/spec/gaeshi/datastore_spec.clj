(ns gaeshi.datastore-spec
  (:use
    [speclj.core]
    [gaeshi.datastore]
    [gaeshi.spec-helpers.datastore]
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
  )

(run-specs)
