(ns gaeshi.kake.test-core)

(defn gaeshi-handler [request]
  (assoc request :processed true))
