(ns gaeshi.middleware.view-context
  (:use
    [gaeshi.views :only (*view-context*)]))

(defn wrap-view-context [handler & kwargs]
  (let [view-context (merge *view-context* (apply hash-map kwargs))]
    (fn [request]
      (binding [*view-context* view-context]
        (handler request)))))


