(ns gaeshi.users
  (:import
    [com.google.appengine.api.users UserServiceFactory User]))

(def user-service-instance (atom nil))

(defn user-service []
  (when (nil? @user-service-instance)
    (reset! user-service-instance (UserServiceFactory/getUserService)))
  @user-service-instance)

(defn map->user [values]
  (cond
    (contains? values :federated-identity) (User. (:email values) (:auth-domain values) (:user-id values) (:federated-identity values))
    (contains? values :user-id) (User. (:email values) (:auth-domain values) (:user-id values))
    :else (User. (:email values) (:auth-domain values))))

(defn user->map [user]
  (if user
    {:email (.getEmail user)
     :nickname (.getNickname user)
     :auth-domain (.getAuthDomain user)
     :user-id (.getUserId user)
     :federated-identity (.getFederatedIdentity user)}
    nil))

(defn login-url
  ([destination-url]
    (.createLoginURL (user-service) destination-url))
  ([destination-url auth-domain]
    (.createLoginURL (user-service) destination-url auth-domain))
  ([destination-url auth-domain federated-identity attributes-request]
    (.createLoginURL (user-service) destination-url auth-domain federated-identity attributes-request)))

(defn logout-url
  ([destination-url]
    (.createLogoutURL (user-service) destination-url))
  ([destination-url auth-domain]
    (.createLogoutURL (user-service) destination-url auth-domain)))

(defn current-user []
  (user->map (.getCurrentUser (user-service))))

(defn current-user-admin? []
  (.isUserAdmin (user-service)))

(defn current-user-logged-in? []
  (.isUserLoggedIn (user-service)))