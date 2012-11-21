(ns refheap.views.about
  (:require [clojure.java.io :as io]
            [refheap.views.common :refer [layout]]
            [compojure.core :refer [defroutes GET]]))

(defroutes about-routes
  (GET "/about" []
    (layout (slurp (io/resource "refheap/views/templates/about.mustache")))))
