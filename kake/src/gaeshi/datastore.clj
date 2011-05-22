(ns gaeshi.datastore
  (:use
    [gaeshi.string :only (gsub)])
  (:require
    [clojure.string :as str])
  )

(defn spear-case [value]
  (str/lower-case
    (gsub
      (str/replace (name value) "_" "-")
      #"([a-z])([A-Z])" (fn [[_ lower upper]] (str lower "-" upper)))))

(defn- map-field-specs [fields]
  (reduce
    (fn [map field]
      (assoc map (keyword (first field)) (apply hash-map (rest field))))
    {} fields))

(defn- extract-defaults [field-specs]
  (reduce
    (fn [map [field spec]]
      (if-let [default (:default spec)]
        (assoc map field default)
        map))
    {}
    field-specs))

(defn- define-constructor [class-sym field-specs]
  (let [ctor-sym (symbol (spear-case (name class-sym)))
        defaults (extract-defaults field-specs)]
    `(defn ~ctor-sym [& args#]
      (let [instance# (new ~class-sym)
            instance# (merge instance# ~defaults)]
        (merge instance# (apply hash-map args#))))))

(defmacro defentity [class-sym & fields]
  (let [field-specs (map-field-specs fields)]
    `(do
      (defrecord ~class-sym [])
      ~(define-constructor class-sym field-specs))))
