(ns gaeshi.spec_helpers.users
  (:use
    [speclj.core :only (before)]
    [gaeshi.users])
  (:import
    [com.google.appengine.api.users UserService]))

(deftype FakeUserService [user admin? logged-in?]
  UserService
  (createLoginURL [this destination-url]
    (format "http://log.in?destination=%s" destination-url))
  (createLoginURL [this destination-url auth-domain]
    (format "http://log.in?destination=%s&authDomain=%s" destination-url auth-domain))
  (createLoginURL [this destination-url auth-domain federated-identity attributes-request]
    (format "http://log.in?destination=%s&authDomain=%s&federatedIdentity=%s&attributes=%s" destination-url auth-domain federated-identity attributes-request))
  (createLogoutURL [this destination-url]
    (format "http://log.out?destination=%s" destination-url))
  (createLogoutURL [this destination-url auth-domain]
    (format "http://log.out?destination=%s&authDomain=%s" destination-url auth-domain))
  (getCurrentUser [this] @user)
  (isUserAdmin [this] @admin?)
  (isUserLoggedIn [this] @logged-in?))

(def fake-user-service (atom nil))

(defn setup-fake-user [& args]
  (let [values (apply hash-map args)
        service @fake-user-service]
    (when (contains? values :user) (reset! (.user service) (map->user (:user values))))
    (when (contains? values :admin?) (reset! (.admin? service) (:admin? values)))
    (when (contains? values :logged-in?) (reset! (.logged-in? service) (:logged-in? values)))))

(defn with-fake-users [& args]
  (before
    (reset! fake-user-service (FakeUserService. (atom nil) (atom false) (atom false)))
    (apply setup-fake-user args)
    (reset! user-service-instance @fake-user-service)))
