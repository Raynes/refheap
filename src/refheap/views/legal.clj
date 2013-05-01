(ns refheap.views.legal
  (:require [compojure.core :refer [defroutes GET]]
            [refheap.views.common :refer [static layout]]))

(defroutes legal-routes
  (GET "/legal/tos" [] (layout (static "refheap/views/templates/tos.html")))
  (GET "/legal/privacy" [] (layout (static "refheap/views/templates/privacy.html"))))
