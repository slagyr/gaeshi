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

  (it "runs the help command"
    (should= 0 (run "--help"))
    (should-not= -1 (.indexOf (to-s @output) "Usage")))

  )
