(ns refheap.views.legal
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]] 
            [refheap.views.common :refer [layout]]))

(defroutes legal-routes
  (GET "/legal/tos" []
    (-> "refheap/views/templates/tos.html" resource slurp layout))
  (GET "/legal/privacy" []
    (-> "refheap/views/templates/privacy.html" resource slurp layout)))
