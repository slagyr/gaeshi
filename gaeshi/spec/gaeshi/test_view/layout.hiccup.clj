(require `gaeshi.views)
[:html
 [:head
  [:title "Test Layout"]]
 [:body
  (eval (:template-body gaeshi.views/*view-context*))]]