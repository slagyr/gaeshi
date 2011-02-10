(ns gaeshi.middleware.verbose)

(def request-count (atom 0))

(def spaces (repeat " "))
(def indents (repeat "    "))

(defn- left-col [value width]
  (let [space-count (- width (count (str value)))]
    (apply str value (take space-count spaces))))

(defn- indentation [indent]
  (let [n (if (< indent 0) 0 indent)]
    (apply str (take n indents))))

(declare say)

(defn- say-map [the-map indent]
  (if (< (count the-map) 2)
    (print the-map)
    (do
      (print "{")
      (let [key-map (reduce #(assoc %1 (str %2) %2) {} (keys the-map))
            keys (sort (keys key-map))
            key-lengths (map count keys)
            max-key-length (apply max key-lengths)
            left-width (+ 2 max-key-length)]
        (doseq [key keys]
          (println)
          (print (indentation indent))
          (print (left-col key left-width))
          (say (get the-map (get key-map key)) (inc indent))))
      (print "}"))))

(defn- say
  ([thing] (say thing 0))
  ([thing indent]
    (cond
      (map? thing) (say-map thing (inc indent))
      (nil? thing) (print "nil")
      :else (print thing))))

(defn wrap-verbose [handler]
  (fn [request]
    (let [request-id (swap! request-count inc)]
      (println "REQUEST " request-id " ========================================================================================")
      (say (dissoc request :servlet-request))
      (println)
      (println)
      (let [response (handler request)]
        (println "RESPONSE " request-id " ========================================================================================")
        (say (assoc response :body (str (count (str (:body response))) " chars of body")))
        (println)
        (println)
        response))))
