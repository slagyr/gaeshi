(defproject gaeshi "0.1.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [ring/ring-servlet "0.3.5"]
                 [appengine "0.4.3-SNAPSHOT"]
                 [fresh "1.0.1"]]
  :dev-dependencies [[speclj "1.2.0"]
                     [junit "4.8.2"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  :repositories {"local" "file://m2"}
  )
