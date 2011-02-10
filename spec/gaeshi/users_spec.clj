(ns gaeshi.users-spec
  (:use
    [speclj.core]
    [gaeshi.users]
    [gaeshi.spec_helpers.users]))

(describe "Users"

  (with-fake-users)

  (it "creates user from a full map"
    (let [user (map->user {:auth-domain "yahoo.com"
                           :email "joe@yahoo.com"
                           :federated-identity "http://yahoo.com/joe"
                           :nickname "joe"
                           :id "1234567890"})]
      (should= "yahoo.com" (.getAuthDomain user))
      (should= "joe@yahoo.com" (.getEmail user))
      (should= "http://yahoo.com/joe" (.getFederatedIdentity user))
      (should= "joe" (.getNickname user))
      (should= "1234567890" (.getUserId user))))

  (it "creates user from a minimal map"
    (let [user (map->user {:auth-domain "yahoo.com"
                           :email "joe@yahoo.com"})]
      (should= "yahoo.com" (.getAuthDomain user))
      (should= "joe" (.getNickname user))
      (should= "joe@yahoo.com" (.getEmail user))))

  (it "creates user from a medium map"
    (let [user (map->user {:auth-domain "yahoo.com"
                           :email "joe@yahoo.com"
                           :id "1234567890"})]
      (should= "yahoo.com" (.getAuthDomain user))
      (should= "joe@yahoo.com" (.getEmail user))
      (should= "joe" (.getNickname user))
      (should= "1234567890" (.getUserId user))))

  (it "creates map from a user"
    (let [values {:auth-domain "yahoo.com"
                  :email "joe@yahoo.com"
                  :federated-identity "http://yahoo.com/joe"
                  :nickname "joe"
                  :id "1234567890"}
          user (map->user values)
          result (user->map user)]
      (should= values result)))

  (it "generates login/logout urls"
    (should= "http://log.in?destination=foo" (login-url "foo"))
    (should= "http://log.in?destination=foo&authDomain=authdomain" (login-url "foo" "authdomain"))
    (should= "http://log.in?destination=foo&authDomain=authdomain&federatedIdentity=yahoo.com&attributes=#{:a}"
      (login-url "foo" "authdomain" "yahoo.com" #{:a}))
    (should= "http://log.out?destination=foo" (logout-url "foo"))
    (should= "http://log.out?destination=foo&authDomain=authdomain" (logout-url "foo" "authdomain")))

  (it "handles service calls"
    (setup-fake-user
      :user {:email "joe@yahoo.com" :auth-domain "yahoo.com"}
      :admin? true
      :logged-in? true)
    (should= "joe@yahoo.com" (:email (current-user)))
    (should= true (current-user-admin?))
    (should= true (current-user-logged-in?)))

  (it "handles service calls with other values"
    (should= nil (current-user))
    (should= false (current-user-admin?))
    (should= false (current-user-logged-in?)))
  )
