(defproject gaeshi "0.2.0"
  :description "FIXME: write"
  :repositories {"releases" "http://appengine-magic-mvn.googlecode.com/svn/releases/"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [ring/ring-servlet "0.3.5"]
                 [compojure "0.6.2"]
                 [appengine "0.4.3-SNAPSHOT"]
                 [speclj "1.3.1"]
                 [fresh "1.0.1"]
                 [hiccup "0.3.1"]
                 [com.google.appengine/appengine-api-labs "1.4.2"]
                 [com.google.appengine/appengine-api-stubs "1.4.2"]
                 [com.google.appengine/appengine-local-runtime "1.4.2"]
                 [com.google.appengine/appengine-local-runtime-shared "1.4.2"]
                 [com.google.appengine/appengine-testing "1.4.2"]]
  :dev-dependencies [[speclj "1.3.1"]
                     [junit "4.8.2"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  )
