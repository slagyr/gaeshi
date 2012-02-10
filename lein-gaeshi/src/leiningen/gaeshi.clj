(ns leiningen.gaeshi
  (:use
    [gaeshi.kuzushi.main :only (run-with-project)]))

(defn gaeshi [project & args]
  (apply run-with-project project args))