(ns #^{:author "Roman Scherer"}
  appengine.environment
  (:import (com.google.apphosting.api ApiProxy ApiProxy$Environment)
           (com.google.appengine.tools.development ApiProxyLocalFactory LocalServerEnvironment)
           java.util.HashMap
           java.io.File)
  (:use clojure.contrib.zip-filter.xml))

(def *application* "local")
(def *version* "1")

(defn- proxy-attributes
  "Return a map of attributes for ApiProxy."
  [& options]
  (let [options (apply hash-map options)]
    (doto (HashMap.)
      (.put "com.google.appengine.server_url_key"
            (str "http://localhost:" (or (:port options) 8080))))))

(defn local-proxy
  "Returns a local api proxy environment."
  [& options]
  (let [options (apply hash-map options)]
    (proxy [ApiProxy$Environment] []
      (getAppId [] *application*)
      (getAttributes [] (java.util.HashMap.))
      (getAuthDomain [] "")
      (getDefaultNamespace [] "")
      (getEmail [] (or (:email options) ""))
      (getRequestNamespace [] "")
      (getVersionId [] *version*)
      (isAdmin [] (or (:admin options) true))
      (isLoggedIn [] (not (nil? (:email options)))))))

(defn local-server-environment
  "Returns a local server environment."  
  [ & [directory]]
  (let [directory (or directory (System/getProperty "java.io.tmpdir"))]
    (proxy [LocalServerEnvironment] []
      (getAppDir [] (java.io.File. directory)))))

(defn login-aware-proxy
  "Returns a local api proxy environment."
  [request]
  (let [email (:email (:session request))]
    (local-proxy :email email)))

(defmacro with-appengine
  "Macro to set the environment for the current thread."
  [proxy body]
  `(last
    (doall [(ApiProxy/setEnvironmentForCurrentThread ~proxy) ~body])))

(defn environment-decorator
  "Decorates the given application with local api proxy environment."
  [application]
  (fn [request]
    (with-appengine (login-aware-proxy request)
      (application request))))

(defn init-appengine
  "Initialize the App Engine services."  
  [& [directory]]     
  (ApiProxy/setDelegate
   (.create
    (ApiProxyLocalFactory.)
    (local-server-environment directory))))

(defn init-repl
  "Initialize the App Engine services."  
  [& [directory]]
  (ApiProxy/setEnvironmentForCurrentThread (local-proxy))
  (ApiProxy/setDelegate
   (.create
    (ApiProxyLocalFactory.)
    (local-server-environment directory))))

(defn- feed-to-zip [filename]
  (clojure.zip/xml-zip (clojure.xml/parse filename)))

(defn parse-configuration
  "Read the appengine-web.xml file, extract and save all system
  properties defined in this file."
  [filename]    
  (let [zipper (feed-to-zip filename)]
    {:application (first (xml-> zipper :application text))
     :version (first (xml-> zipper :version text))
     :properties (reduce #(assoc %1 (attr %2 :name) (attr %2 :value))
                         {} (xml-> zipper :system-properties :property))}))

(defn set-system-properties
  "Set the system properties."
  [properties] (doall (map #(System/setProperty (first %) (last %)) properties)))

(defmacro with-configuration [filename & body]
  `(let [configuration# (parse-configuration ~filename)]
     (binding [*application* (:application configuration#)
               *version* (:version configuration#)]
       (set-system-properties (:properties configuration#))
       ~@body)))
