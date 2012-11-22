(ns refheap.views.legal
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]] 
            [refheap.views.common :refer [layout]]))

(defroutes legal-routes
  (GET "/legal/tos" []
    (layout (slurp (io/resource "refheap/views/templates/tos.mustache"))))
  (GET "/legal/privacy" []
    (layout (slurp (io/resource "refheap/views/templates/privacy.mustache")))))
