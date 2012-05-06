(ns refheap.views.about
  (:require [clojure.java.io :as io]
            [refheap.views.common :refer [layout]]
            [noir.core :refer [defpage]]))

(defpage "/about" []
  (layout (slurp (io/resource "refheap/views/templates/about.mustache"))))
