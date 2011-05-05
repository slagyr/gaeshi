(ns !-APP_NAME-!.core
  (:use
    [compojure.core :only (defroutes GET)]
    [compojure.route :only (not-found)]
    [gaeshi.middleware.view-context :only (wrap-view-context)]
    [gaeshi.views :only (render-template render-html)]
    [gaeshi.controllers :only (controller-router)]))

(defroutes !-APP_NAME-!-routes
  (GET "/" [] (render-template "index"))
  (controller-router 'cleancoders.controller)
  (not-found (render-template "not_found" :template-root "!-APP_NAME-!/view" :ns `!-APP_NAME-!.view.view-helpers)))

(def app-handler
  (->
    !-APP_NAME-!-routes
    (wrap-view-context :template-root "!-APP_NAME-!/view" :ns `!-APP_NAME-!.view.view-helpers)))

