(ns leiningen.gaeshi
  (:use
    [gaeshi.kuzushi.main :only (run-with-args)]))

(defn ^:no-project-needed gaeshi [project & args]
  (apply gaeshi.kuzushi.main/run-with-args args))