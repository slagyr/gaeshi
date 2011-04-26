(ns #^{:author "E. Fukamachi"
       :doc "API for the Google App Engine URL Fetch service." }
  appengine.urlfetch
  (:use [clojure.contrib.def :only [defnk]])
  (:import [com.google.appengine.api.urlfetch
            HTTPRequest
            HTTPMethod
            HTTPHeader
            FetchOptions$Builder
            URLFetchServiceFactory
            URLFetchService]))

(defn urlfetch []
  "Create a URLFetchService"
  (URLFetchServiceFactory/getURLFetchService))

(defn- headers->map [headers]
  (loop [res {}, h headers]
    (if (empty? h) res
        (recur
         (assoc res
           (keyword (.getName (first h)))
           (.getValue (first h)))
         (rest h)))))

(defn- response->map [response]
  {:status-code (.getResponseCode response)
   :content (String. (.getContent response))
   :headers (headers->map (seq (.getHeaders response)))})

(defn- get-options [allow-truncate follow-redirects]
  (let [opt (if allow-truncate
              (FetchOptions$Builder/allowTruncate)
              (FetchOptions$Builder/disallowTruncate))]
    (if follow-redirects
      (.followRedirects opt)
      (.doNotFollowRedirects opt))
    opt))

(defn- get-request
  [url {:keys [method payload headers allow-truncate follow-redirects] :or {method "GET", follow-redirects true}}]
     (let [request (HTTPRequest. (java.net.URL. url)
                         (HTTPMethod/valueOf method)
                         (get-options allow-truncate follow-redirects))]
       (doseq [h headers]
         (.setHeader request (HTTPHeader. (str (first h)) (second h))))
       (.setPayload request payload)
       request))

(defn fetch [url & options]
  (let [request (get-request url (first options))
        response (.fetch (urlfetch) request)]
    (response->map response)))
