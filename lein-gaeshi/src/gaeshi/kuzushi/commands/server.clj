(ns gaeshi.kuzushi.commands.server
  (:require [clojure.string]
            [leiningen.core.classpath :refer [get-classpath]]
            [joodo.cmd :refer [java]]
            [joodo.kuzushi.common :refer [symbolize with-lein-project *project*]])
  (:import [mmargs Arguments]))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addValueOption "p" "port" "PORT" "Change the port (default: 8080)")
  (.addValueOption "a" "address" "ADDRESS" "Change the address (default: 127.0.0.1)")
  (.addValueOption "e" "environment" "ENVIRONMENT" "Change the environment (default: development)")
  (.addValueOption "d" "directory" "DIRECTORY" "Change the directory (default: .)")
  (.addValueOption "j" "jvm-opts" "JVM OPTIONS" "Add JVM options"))

(def default-options {
                       :port 8080
                       :address "127.0.0.1"
                       :environment "development"
                       :directory "."})

(defn parse-args [& args]
  (let [options (symbolize (.parse arg-spec (into-array String args)))
        options (if (contains? options :port ) (assoc options :port (Integer/parseInt (:port options))) options)]
    (merge default-options options)))

(defn execute
  "Starts the app in on a local web server"
  [options]
  (with-lein-project
    (let [classpath (clojure.string/join java.io.File/pathSeparatorChar (get-classpath *project*))
          jvm-args (filter identity [(:jvm-opts options) "-cp" classpath])
          args ["-p" (:port options) "-a" (:address options) "-e" (:environment options) "-d" (:directory options)]]
      (java jvm-args "gaeshi.tsukuri.GaeshiDevServer" (map str args)))))

