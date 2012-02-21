(defproject gaeshi/gaeshi "0.7.1"
  :description "Runtime library for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :repositories {"releases" "http://gaeshi-mvn.googlecode.com/svn/trunk/releases/"}
  :dependencies [[joodo "0.7.0"]
;                 [inflections "0.4.3"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.6.2.1"]]
  :dev-dependencies [[speclj "2.1.1"]
                     [gaeshi/gaeshi-dev "0.7.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  )
