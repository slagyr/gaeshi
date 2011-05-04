(ns leiningen.tryit
  (:use
    [leiningen.jar :only (jar get-default-jar-name)]
    [leiningen.plugin :only (plugin)]
    [gaeshi.cmd :only (exec)]))

(defn tryit [project]
  (jar project)
  (exec ["mvn" "install:install-file" "-DgroupId=gaeshi" "-DartifactId=kuzushi" (str "-Dversion=" (:version project)) "-Dpackaging=jar" (str "-Dfile=" (get-default-jar-name project))])
  (plugin "install" "gaeshi/kuzushi" (:version project)))


