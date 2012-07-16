(def config (load-file "../config.clj"))

(defproject gaeshi/gaeshi (:version config)
  :description "Runtime library for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :repositories {"releases" "http://gaeshi-mvn.googlecode.com/svn/trunk/releases/"}
  :dependencies [[joodo ~(:joodo-version config)]
                 [com.google.appengine/appengine-api-1.0-sdk ~(:gae-version config)]]
  :dev-dependencies [[speclj ~(:speclj-version config)]
                     [gaeshi/gaeshi-dev ~(:version config)]]
  :test-path "spec/"
  :java-source-path "src/"
  )
