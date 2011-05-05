(ns gaeshi.kuzushi.common)

(def endl (System/getProperty "line.separator"))

(defn exit [code]
  (System/exit code))

(defn symbolize [java-map]
  (reduce (fn [result entry] (assoc result (keyword (.getKey entry)) (.getValue entry))) {} java-map))

(defn load-var [ns-sym var-sym]
  (try
    (require ns-sym)
    (let [ns (the-ns ns-sym)]
      (ns-resolve ns var-sym))
    (catch Exception e
      (.printStackTrace e)
      nil)))
