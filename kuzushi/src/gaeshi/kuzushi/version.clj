(ns gaeshi.kuzushi.version
  (:require
    [clojure.string :as str]))

(def major 999)
(def minor 5)
(def tiny 1)
(def snapshot false)
(def string
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))
(def summary (str "gaeshi/kuzushi " string))

(def kake-version "0.5.1")
(def tsukuri-version "0.5.1")
