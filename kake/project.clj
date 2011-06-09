(defproject gaeshi/kake "0.6.0-SNAPSHOT"
  :description "Runtime library for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011 Micah Martin All Rights Reserved."}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [ring/ring-servlet "0.3.7"]
                 [compojure "0.6.2"]
                 [hiccup "0.3.1"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.5.0"]
                 [inflections "0.4.3"]]
  :dev-dependencies [[speclj "1.4.0"]
                     [gaeshi/tsukuri "0.6.0-SNAPSHOT"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  )
