(ns gaeshi.kuzushi.version
  (:require
    [clojure.string :as str]))

(def major 0)
(def minor 5)
(def tiny 0)
(def snapshot false)
(def string
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))
(def summary (str "gaeshi/kuzushi " string))
