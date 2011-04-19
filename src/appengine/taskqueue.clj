(ns #^{:author "Roman Scherer"
       :doc "API for the Google App Engine task queue service." }
  appengine.taskqueue
  (:import (com.google.appengine.api.labs.taskqueue TaskOptions$Builder Queue QueueFactory))
  (:use [clojure.contrib.def :only (defvar)]))

(defn default-queue
  "Returns the default task queue."
  [] (QueueFactory/getDefaultQueue))

(defvar *queue* (default-queue)
  "The task queue binding.")

(defn add
  "Adds the given task path to the queue."
  [path & options]
  (. *queue* add (TaskOptions$Builder/url path)))

