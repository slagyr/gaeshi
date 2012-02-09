(ns leiningen.server
  "Startup the app in the development environment."
  (:use
    [leiningen.clean :only (clean)]
    [leiningen.classpath :only [get-classpath-string]]
    [gaeshi.cmd :only (java)]))

(defn server [project & args]
  (let [classpath (get-classpath-string project)
        jvm-args ["-cp" classpath]]
    (clean project)
    (java jvm-args "gaeshi.tsukuri.GaeshiDevServer" args)))