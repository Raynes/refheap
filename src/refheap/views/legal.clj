(ns refheap.views.legal
  (:require [clojure.java.io :as io]
            [noir.core :refer [defpage]]
            [refheap.views.common :refer [layout]]))

(defpage "/legal/tos" []
  (layout (slurp (io/resource "refheap/views/templates/tos.mustache"))))

(defpage "/legal/privacy" []
  (layout (slurp (io/resource "refheap/views/templates/privacy.mustache"))))
