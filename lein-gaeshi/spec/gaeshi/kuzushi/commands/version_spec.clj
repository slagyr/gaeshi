(ns gaeshi.kuzushi.commands.version-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.spec-helper]
    [joodo.kuzushi.core :only (run)]
    [joodo.kuzushi.common :only (endl)]
    [gaeshi.kuzushi.commands.version]))

(describe "Version Comamnd"

  (with-command-help)

  (it "parses no args"
    (should= {} (parse-args)))

  (it "handles the --version command"
    (should= 0 (run "--version"))
    (should= (str "gaeshi/lein-gaeshi " gaeshi.kuzushi.version/string endl) (to-s @output)))

  )
