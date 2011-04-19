(ns #^{:author "John D. Hume"
       :doc "API for the Google App Engine user service." }
   appengine.users
  (:import (com.google.appengine.api.users User UserService UserServiceFactory)))

(defn user-service
  "Returns the user service."
  [] (UserServiceFactory/getUserService))

(defn user->map
  "Converts a user object into a map."
  [user]
  (if (isa? (class user) User)
    {:auth-domain (.getAuthDomain user)
     :email (.getEmail user)
     :federated-identity (.getFederatedIdentity user)
     :nickname (.getNickname user)
     :user-id (.getUserId user)}))

(defn current-user
  "If the user is logged in, this method will return a User that
  contains information about them."
  [] (user->map (.getCurrentUser (user-service))))

(defn login-url
  "Returns an URL that can be used to display a login page to the user."
  [destination-url & [auth-domain federated-identity attributes-request]]
  (.createLoginURL (user-service) destination-url auth-domain
                   federated-identity attributes-request))

(defn logout-url
  " Returns an URL that can be used to log the current user out."
  [destination-url & [auth-domain]]
  (.createLogoutURL (user-service) destination-url auth-domain))

(defn user-info
  "With no arguments, returns a UserService and User for the current request in a map keyed by :user-service and :user respectively.
  If the user is not logged in, :user will be nil.
  With a single map argument, a Ring request, returns the user-info map associated with the request by wrap-with-user-info."
  ([] {:user (.getCurrentUser (user-service)) :user-service (user-service)})
  ([request]
   (:appengine/user-info request)))

(defn wrap-with-user-info
  "Ring middleware method that wraps an application so that every request will have
  a user-info map assoc'd to the request under the key :appengine/user-info."
  [application]
  (fn [request]
    (application (assoc request :appengine/user-info (user-info)))))

(defn wrap-requiring-login
  ([application] (wrap-requiring-login application nil))
  ([application destination-uri]
    (let [uri-fn (if destination-uri
                   (fn [_] destination-uri)
                   (fn [request] (:uri request)))]
      (fn [request]
        (let [{:keys [user-service]} (user-info request)]
          (if (.isUserLoggedIn user-service)
            (application request)
            {:status 302 :headers {"Location" (.createLoginURL user-service (uri-fn request))}}))))))

(defn wrap-requiring-admin [application]
  (fn [request]
    (let [{:keys [user-service]} (user-info request)]
      (if (.isUserAdmin user-service)
        (application request)
        {:status 403 :body "Access denied. You must be logged in as admin user!"}))))
