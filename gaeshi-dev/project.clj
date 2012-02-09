(defproject gaeshi/gaeshi-dev "0.7.0"
  :description "Development library for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :repositories {"releases" "http://gaeshi-mvn.googlecode.com/svn/trunk/releases/"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [speclj "2.1.1"]
                 [mmargs "1.2.0"]
                 [fresh "1.0.2"]
                 [com.google.appengine/appengine-api-labs "1.6.2.1"]
                 [com.google.appengine/appengine-api-stubs "1.6.2.1"]
                 [com.google.appengine/appengine-local-runtime "1.6.2.1"]
                 [com.google.appengine/appengine-local-runtime-shared "1.6.2.1"]
                 [com.google.appengine/appengine-testing "1.6.2.1"]
                 [tomcat/jasper-runtime "5.0.28"]
                 [jstl "1.1.2"]
                 [taglibs/standard "1.1.2"]
                 [commons-el "1.0"]
                 [org.apache.geronimo.specs/geronimo-jsp_2.1_spec "1.0.1"]
                 [filecabinet "1.0.1"]]
  :dev-dependencies [[speclj "2.1.1"]
                     [com.google.appengine/appengine-api-1.0-sdk "1.6.2.1"]
                     [ring/ring-servlet "1.0.2"]]
  :test-path "spec/"
  :java-source-path "src/")
