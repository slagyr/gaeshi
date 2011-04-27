(ns gaeshi.tsukuri.environment
  (:import
    [com.google.apphosting.api ApiProxy ApiProxy$Environment]
    [com.google.appengine.tools.development ApiProxyLocalFactory LocalServerEnvironment]))

(deftype Environment [app]
  ApiProxy$Environment
  (getAppId [this] app)
  (getAttributes [this] (java.util.HashMap.))
  (getAuthDomain [this] "")
  (getEmail [this] "")
  (getRequestNamespace [this] "")
  (getVersionId [this] 1)
  (isAdmin [this] true)
  (isLoggedIn [this] false)

  LocalServerEnvironment
  (getAppDir [this] (java.io.File. "war")))

(defn setup-environment [app]
  (let [environment (Environment. app)]
    (ApiProxy/setEnvironmentForCurrentThread environment)
    (ApiProxy/setDelegate
      (.create (ApiProxyLocalFactory.) environment))))
