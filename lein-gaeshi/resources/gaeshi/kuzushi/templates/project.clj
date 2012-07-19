(defproject !-APP_NAME-! "0.0.1"
  :description "A website deployable to AppEngine"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [gaeshi "!-GAESHI_VERSION-!"]]

  :gaeshi-core-namespace !-APP_NAME-!.core

  ; leiningen 2
  :profiles {:dev {:dependencies [[gaeshi/gaeshi-dev "!-GAESHI_DEV_VERSION-!"]
                                  [speclj "2.2.0"]]}}
  :test-paths ["spec/"]
  :java-source-paths ["src/"]
  :repl-options {:init (do (use 'gaeshi.tsukuri.environment) (setup-environment "!-APP_NAME-!-development"))}
  :plugins [[speclj "2.2.0"]]


  ; leiningen 1
  :dev-dependencies [[gaeshi/gaeshi-dev "!-GAESHI_DEV_VERSION-!"]
                     [speclj "2.2.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  :repl-init-script "config/development/repl_init.clj"

  )