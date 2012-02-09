(ns gaeshi.kuzushi.commands.deploy
  (:require
    [clojure.string :as str])
  (:use
    [gaeshi.kuzushi.commands.prepare :only (prepare)]
    [joodo.kuzushi.common :only (symbolize with-lein-project *project*)]
    [joodo.cmd :only (exec)])
  (:import
    [mmargs Arguments]
    [java.io PushbackReader FileReader]))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addOptionalParameter "environment" "Specifies which environment to deploy. (default: development)"))

(def default-options {:environment "development"})

(defn parse-args [& args]
  (let [options (symbolize (.parse arg-spec (into-array String args)))]
    (merge default-options options)))

(defn- abort []
  (println "Deployment aborted! ***********************")
  (System/exit -1))

(defn- config-path [project]
  (let [config-file-path (str/join (System/getProperty "file.separator") [(System/getProperty "user.home") ".gaeshi" (:name project)])]
    config-file-path))

(defn- config-help [project]
  (println "The deployment config file (" (config-path project) ") must exist and define a map with the following data:")
  (println "{")
  (println "\t:appengine-sdk-dir \"/path/to/appengine-java-sdk-1.4.3\"")
  (println "\t:appengine-email \"jon.doe@gmail.com\"")
  (println "\t:appengine-password \"jonspassword\"")
  (println "}")
  (abort))

(defn read-deploy-config [project]
  (try
    (with-open [reader (PushbackReader. (FileReader. (config-path project)))]
      (read reader))
    (catch Exception e
      (println e)
      (config-help project))))

(defn- check-required-config [config]
  (when (some #(nil? (% config)) [:appengine-sdk-dir :appengine-email :appengine-password])
    (config-help)))

(defn deploy [project options]
  (println "Gaeshi:" (:environment options) "deployment requested...")
  (let [config (read-deploy-config project)
        _ (check-required-config config)
        windows? (re-find #"Windows" (System/getProperty "os.name"))
        deploy-exe (str/join (System/getProperty "file.separator") [(:appengine-sdk-dir config) "bin" (if windows? "appcfg.cmd" "appcfg.sh")])]
    (prepare project options)
    (println "Gaeshi: Invoking deploy command")
    (exec [deploy-exe (str "--email=" (:appengine-email config)) "--passin" "update" "war"] (:appengine-password config))))

(defn execute
  "Deploy the project to Google AppEngine"
  [options]
  (with-lein-project
    (let [options (assoc options :environment (or (:environment options) "development"))]
      (deploy *project* options))))
