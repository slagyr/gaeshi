(ns gaeshi.tsukuri.environment
  (:import
    [gaeshi.tsukuri GaeshiDevServerEnvironment GaeshiApiProxyEnvironment]))

(defn setup-environment [app]
  (.install (GaeshiDevServerEnvironment.))
  (.install (GaeshiApiProxyEnvironment. app)))
