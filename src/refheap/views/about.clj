(ns refheap.views.about
  (:require [refheap.views.common :refer [layout static]]
            [compojure.core :refer [defroutes GET]]))

(defroutes about-routes
  (GET "/about" [] (layout (static "refheap/views/templates/about.html"))))