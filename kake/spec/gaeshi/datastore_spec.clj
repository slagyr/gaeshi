(ns gaeshi.datastore-spec
  (:use
    [speclj.core]
    [gaeshi.datastore]
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

;(prn (macroexpand `(defentity ManyDefaultedFields
;  [field1]
;  [field2]
;  [field3 :default ".141592"]
;  [field42 :default "value42"])))
;
;(do
;  (defrecord ManyDefaultedFields [])
;  (defn gaeshi.datastore-spec.many-defaulted-fields [& args__13636]
;    (let [instance__13637 (new ManyDefaultedFields)]
;      (reduce
;        (fn [inst__13638 [kw__13639 spec__13640]]
;          (if-let [default__13641 (:default spec__13640)]
;            (assoc inst__13638 kw__13639 default__13641) inst__13638))
;        instance__13637
;        {:field42 {:default "value42"}, :field3 {:default ".141592"}, :field2 {}, :field1 {}})
;      (if (seq args__13636) (apply assoc instance__13637 args__13636) instance__13637))))


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
  )

(run-specs)



;(defrecord Foo
;  [key kind] EntityProtocol
;  (create-entity
;    [foo]
;    (create-entity
;      (serialize foo)))
;
;  (delete-entity [foo]
;    (delete-entity
;      (:key foo)))
;
;  (save-entity [foo]
;    (save-entity
;      (serialize foo)))
;
;  (find-entity [foo]
;    (find-entity
;      (serialize foo)))
;
;  (update-entity [foo key-vals]
;    (save-entity
;      (merge foo key-vals)))
;
;  SerializationProtocol
;
;  (deserialize [foo] foo)
;
;  (serialize [foo]
;    (serialize-entity foo)))
;
;(defprotocol FooProtocol
;  (foo? [foo] "Returns true if arg is a datastore spec/foo, false otherwise.")
;  (foo-key-name [foo] "Extract the datastore spec/foo key name.")
;  (foo-key [foo] "Make a datastore spec/foo key.")
;  (foo [foo] "Make a datastore spec/foo."))
;
;(defmethod deserialize-entity "foo" [entity]
;  (new Foo (.getKey entity) (.getKind entity)))
;
;(defmethod serialize-entity "foo" [map]
;  (doto (com.google.appengine.api.datastore.Entity.
;    (or (:key map) (:kind map)))))
;
;(do
;  (defn find-foos "Find all foos." [& options]
;    (appengine.datastore.query/select "foo")))
;
;(extend-type com.google.appengine.api.datastore.Entity FooProtocol
;  (foo? [foo]
;    (= (.getKind foo) "foo")))
;
;(extend-type com.google.appengine.api.datastore.Key FooProtocol)
;
;(extend-type nil FooProtocol (foo? [foo] false))
;(extend-type java.lang.Object FooProtocol (foo? [foo] false))
;nil
;(extend-type clojure.lang.IPersistentMap FooProtocol
;  (foo? [foo]
;    (= (:kind foo) "foo"))
;  (foo-key-name [foo]
;    (extract-key foo []))
;  (foo-key [foo]
;    (if-let [key (foo-key-name foo)]
;      (appengine.datastore.keys/make-key nil "foo" key)))
;  (foo [foo]
;    (new Foo
;      (foo-key foo) "foo")))