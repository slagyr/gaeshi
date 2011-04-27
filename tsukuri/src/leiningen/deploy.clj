(ns leiningen.deploy
  (:require
    [clojure.string :as str])
  (:use
    [leiningen.prepare :only (prepare)]
    [gaeshi.cmd :only (exec)])
  (:import
    [java.io PushbackReader FileReader]))

(defn- abort []
  (println "Deployment aborted! ***********************")
  (System/exit -1))

(defn- config-path [project]
  (let [config-file-name (str "." (:name project) ".gaeshi")
        config-file-path (str/join (System/getProperty "file.separator") [(System/getProperty "user.home") config-file-name])]
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

(defn deploy [project & args]
  (apply println "Gaeshi: Deployment requested..." args)
  (let [config (read-deploy-config project)
        _ (check-required-config config)
        windows? (re-find #"Windows" (System/getProperty "os.name"))
        deploy-exe (str/join (System/getProperty "file.separator") [(:appengine-sdk-dir config) "bin" (if windows? "appcfg.cmd" "appcfg.sh")])]
    (apply prepare project args)
    (println "Gaeshi: Invoking deploy command")
    (exec [deploy-exe (str "--email=" (:appengine-email config)) "--passin" "update" "war"] (:appengine-password config))))
