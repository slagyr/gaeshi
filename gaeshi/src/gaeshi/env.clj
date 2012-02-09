(ns gaeshi.env)

(def env (or (System/getProperty "gaeshi.env") "development"))

(defn development-env? []
  (= "development" env))

(defn production-env? []
  (= "production" env))
