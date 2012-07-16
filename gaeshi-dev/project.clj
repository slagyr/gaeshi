(def config (load-file "../config.clj"))

(defproject gaeshi/gaeshi-dev (:version config)
  :description "Development library for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :repositories {"releases" "http://gaeshi-mvn.googlecode.com/svn/trunk/releases/"}
  :dependencies [[org.clojure/clojure ~(:clojure-version config)]
                 [speclj ~(:speclj-version config)]
                 [mmargs "1.2.0"]
                 [fresh "1.0.2"]
                 [com.google.appengine/appengine-api-labs ~(:gae-version config)]
                 [com.google.appengine/appengine-api-stubs ~(:gae-version config)]
                 [com.google.appengine/appengine-local-runtime ~(:gae-version config)]
                 [com.google.appengine/appengine-local-runtime-shared ~(:gae-version config)]
                 [com.google.appengine/appengine-testing ~(:gae-version config)]
                 [tomcat/jasper-runtime "5.0.28"]
                 [jstl "1.1.2"]
                 [taglibs/standard "1.1.2"]
                 [commons-el "1.0"]
                 [org.apache.geronimo.specs/geronimo-jsp_2.1_spec "1.0.1"]
                 [filecabinet "1.0.4"]]
  :dev-dependencies [[speclj ~(:speclj-version config)]
                     [com.google.appengine/appengine-api-1.0-sdk ~(:gae-version config)]
                     [ring/ring-servlet "1.0.2"]]
  :test-path "spec/"
  :java-source-path "src/")
