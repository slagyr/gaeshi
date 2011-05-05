(ns gaeshi.kuzushi.commands.help-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.spec-helper]
    [gaeshi.kuzushi.commands.help]
    [gaeshi.kuzushi.core :only (run)]))

(describe "Help Comamnd"

  (with-command-help)

  (it "parses no args"
    (should= {} (parse-args)))

  (it "parses help for a command"
    (should= {:command "help"} (parse-args "help")))

  (it "runs the help command"
    (should= 0 (run "--help"))
;    (.println *err* (to-s @output))
    (should-not= -1 (.indexOf (to-s @output) "Usage")))

  (it "runs help on the help command"
    (should= 0 (run "help" "help"))
    (should-not= -1 (.indexOf (to-s @output) "Usage: gaeshi help [command]")))

  (it "knows the right ns regex"
    (should (re-matches ns-regex "some_command.clj"))
    (should (re-matches ns-regex "some_command__init.class"))
    (should= "some_command" (second (first (re-seq ns-regex "some_command.clj"))))
    (should= nil (first (re-seq ns-regex "blah"))))



  )
