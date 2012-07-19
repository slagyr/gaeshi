(ns gaeshi.kuzushi.version
  (:require
    [clojure.string :as str]))

(def major 0)
(def minor 10)
(def tiny 0)
(def snapshot false)
(def string
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))
(def summary (str "gaeshi/lein-gaeshi " string))

(def gaeshi-version "0.10.0")
(def gaeshi-dev-version "0.9.0")
