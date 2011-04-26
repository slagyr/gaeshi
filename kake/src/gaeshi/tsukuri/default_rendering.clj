(ns gaeshi.support.default-rendering
  "This namespace is used by default when rendering.  It contains commonly used view helper functions."
  (:use
    [gaeshi.views :only (render-partial *view-context*)]
    [hiccup.page-helpers]
    [hiccup.form-helpers]))
