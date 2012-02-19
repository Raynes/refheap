(ns refheap.views.legal
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]])
  (:require [clojure.java.io :as io]))

(defpage "/legal/tos" []
  (layout (slurp (io/resource "refheap/views/templates/tos.mustache"))))

(defpage "/legal/privacy" []
  (layout (slurp (io/resource "refheap/views/templates/privacy.mustache"))))
