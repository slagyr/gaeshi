(ns gaeshi.kuzushi.commands.version
  (:use
    [gaeshi.kuzushi.common :only (exit symbolize)])
  (:require
    [gaeshi.kuzushi.version])
  (:import
    [mmargs Arguments]))

(def arg-spec (Arguments.))

(defn parse-args [& args]
  (symbolize (.parse arg-spec (into-array String args))))

(defn execute [options]
  (println gaeshi.kuzushi.version/summary)
  (exit 0))

