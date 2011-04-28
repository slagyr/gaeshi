(ns leiningen.gaeshi-init)

(defn gaeshi-init [project]
  (println "(System/getProperty \"class.path\"): " (System/getProperty "java.class.path"))
  (let [template-root (.getResource (clojure.lang.RT/baseLoader) "gaeshi/tsukuri/templates/marker.txt")]
    (println "template-root: " template-root)))
