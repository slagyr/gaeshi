(ns gaeshi.kuzushi.commands.help
  (:use
    [gaeshi.kuzushi.common :only (exit symbolize)])
  (:import
    [mmargs Arguments]))

(def arg-spec (Arguments.))

(defn parse-args [& args]
  (symbolize (.parse arg-spec (into-array String args))))

(defn usage [errors]
  (if (seq errors)
    (do
      (println "ERROR!!!")
      (doseq [error (seq errors)]
        (println error))))
  (println)
  (println "Usage: gaeshi" (.argString arg-spec))
  (println)
  (println (.parametersString arg-spec))
  (println (.optionsString arg-spec))
  (if (seq errors)
    (exit -1)
    (exit 0)))

(defn execute [options]
  (usage nil))