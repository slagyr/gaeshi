(ns gaeshi.kuzushi.version
  (:require
    [clojure.string :as str]))

(def major 0)
(def minor 6)
(def tiny 0)
(def snapshot true)
(def string
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))
(def summary (str "gaeshi/kuzushi " string))

(def kake-version "0.6.0-SNAPSHOT")
(def tsukuri-version "0.6.0-SNAPSHOT")
