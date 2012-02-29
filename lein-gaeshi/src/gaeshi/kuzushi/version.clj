(ns gaeshi.kuzushi.version
  (:require
    [clojure.string :as str]))

(def major 0)
(def minor 7)
(def tiny 2)
(def snapshot false)
(def string
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))
(def summary (str "gaeshi/lein-gaeshi " string))

(def gaeshi-version "0.7.2")
(def gaeshi-dev-version "0.7.2")
