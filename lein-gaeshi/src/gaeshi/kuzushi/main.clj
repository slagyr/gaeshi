(ns gaeshi.kuzushi.main
  (:use
    [joodo.kuzushi.core :only (run)]
    [joodo.kuzushi.common :only (*project* *command-root* *main-name*)])
  (:require
    [gaeshi.kuzushi.version]))


(defn- run-with-bindings [args]
  (binding [*command-root* "gaeshi.kuzushi.commands"
            *version-summary* gaeshi.kuzushi.version/summary
            *main-name* "gaeshi"]
    (apply run args)))

(defn run-with-project [project & args]
  (binding [*project* project]
    (run-with-bindings args)))

(defn -main [& args]
  (run-with-bindings args))