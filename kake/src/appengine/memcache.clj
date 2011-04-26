(ns #^{:author "E. Fukamachi"
       :doc "Clojure API for the Google App Engine memcache service." }
  appengine.memcache
  (:import [com.google.appengine.api.memcache
            Expiration
            MemcacheServiceFactory
            MemcacheService
            MemcacheService$SetPolicy]))

(defn memcache []
  "Create a MemcacheService"
  (MemcacheServiceFactory/getMemcacheService))

(defn clear-all []
  (.clearAll (memcache)))

(defn get-stats []
  (let [stats (.getStatistics (memcache))]
    (if stats
      {:hits (.getHitCount stats)
       :misses (.getMissCount stats)
       :byte-hits (.getBytesReturnedForHits stats)
       :items (.getItemCount stats)
       :bytes (.getTotalItemBytes stats)
       :oldest-item-age (/ (.getMaxTimeWithoutAccess stats) 1000.0)})))

(defn get-value [key]
  (.get (memcache) key))

(defn get-native-policy [policy-keyword]
  (condp = policy-keyword
    :add MemcacheService$SetPolicy/ADD_ONLY_IF_NOT_PRESENT
    :replace MemcacheService$SetPolicy/REPLACE_ONLY_IF_PRESENT
    :set MemcacheService$SetPolicy/SET_ALWAYS
    MemcacheService$SetPolicy/SET_ALWAYS))

(defn get-expiration [amount]
  (cond
   (or (nil? amount) (= 0 amount)) nil
   (= (class amount) com.google.appengine.api.memcache.Expiration) amount
   (= (class amount) java.util.Date) (Expiration/onDate amount)
   (> amount (* 86400 30)) (Expiration/onDate (java.util.Date. (+ (System/currentTimeMillis) (* amount 1000))))
   :else (Expiration/byDeltaMillis (* amount 1000))))

(defn put-value
  "Save function for general purpose. set always when not specified policy"
  ([key value expiration policy] (.put (memcache) key value (get-expiration expiration) (get-native-policy policy)))
  ([key value expiration] (put-value key value expiration :set))
  ([key value] (put-value key value nil :set)))

(defn set-value
  "Same as put-value"
  [key value & expiration] (put-value key value (first expiration) :set))

(defn add-value
  "Saves value only if not present"
  [key value & expiration] (put-value key value (first expiration) :add))

(defn replace-value
  "Replace value only if present"
  [key value & expiration] (put-value key value (first expiration) :replace))

(defn delete-value [#^Key key ms]
  (.delete (memcache) key ms))

(defn inc-value [#^Key key & delta]
  (.increment (memcache) key (or delta 1)))

(defn dec-value [#^Key key & delta]
  (.increment (memcache) key (- (or delta 1))))
