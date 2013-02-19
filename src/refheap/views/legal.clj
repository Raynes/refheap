(ns refheap.views.legal
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]] 
            [refheap.views.common :refer [layout]]))

(def tos-page (layout (slurp (io/resource "refheap/views/templates/tos.html"))))
(def privacy-page (layout (slurp (io/resource "refheap/views/templates/privacy.html"))))

(defroutes legal-routes
  (GET "/legal/tos" [] tos-page)
  (GET "/legal/privacy" [] privacy-page))
