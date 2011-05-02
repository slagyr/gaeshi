(ns gaeshi.kake.test-override-core)

(defn gaeshi-handler [request]
  (assoc request :gaeshi-handler true))
