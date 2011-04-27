(ns gaeshi.validations
  (:use
    [inflections.core]))

(defn validator [& validations]
  (fn [entity]
    (seq (filter identity (map #(% entity) validations)))))

(defn validate-presence-of
  ([field] (validate-presence-of field (str (name field) " is blank")))
  ([field message]
    (fn [record]
      (let [value (get record field)]
        (if (or (nil? value) (and (string? value) (empty? value)))
          [field message])))))

(defmacro validate-uniqueness-of
  ([field] `(validate-uniqueness-of ~field (str (name ~field) " is not unique")))
  ([field message]
    `(fn [~'record]
      (if-let [value# (~field ~'record)]
        (let [kind# (:kind ~'record)
              finder-name# (symbol (str "find-" (pluralize kind#) "-by-" (name ~field)))
              record-ns# ~*ns*
              finder# (ns-resolve record-ns# finder-name#)
              key# (:key ~'record)
              others# (finder# value#)]
          (if (some #(not (= key# (:key %))) others#)
            [~field ~message]))))))
