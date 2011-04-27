(ns gaeshi.middleware.development
  (:use
    [gaeshi.middleware.refresh :only (wrap-refresh)]
    [gaeshi.middleware.verbose :only (wrap-verbose)]))

(defn wrap-development [handler]
  (->
    handler
    wrap-verbose
    wrap-refresh))
