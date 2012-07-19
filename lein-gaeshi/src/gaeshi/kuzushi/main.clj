(ns gaeshi.kuzushi.main
  (:use [joodo.kuzushi.core :only (run)]
        [joodo.kuzushi.common :only (*project* *lib-name* *summary* load-lein-project)])
  (:require [gaeshi.kuzushi.version]))


(defn- run-with-bindings [args]
  (binding [*summary* (str gaeshi.kuzushi.version/summary ": Command line component for Gaeshi; A Clojure framework for Google App Engine.")
            *lib-name* "gaeshi"]
    (apply run args)))

(defn run-with-project [project & args]
  (binding [*project* project]
    (run-with-bindings args)))

(defn run-with-args [& args]
  (if (= "new" (first args))
    (run-with-bindings args)
    (binding [*project* (load-lein-project)]
      (run-with-bindings args))))

(defn -main [& args]
  (run-with-bindings args))