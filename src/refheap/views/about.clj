(ns refheap.views.about
  (:use [refheap.views.common :only [layout]]
        [noir.core :only [defpage]])
  (:require [clojure.java.io :as io]))

(defpage "/about" []
  (layout (slurp (io/resource "refheap/views/templates/about.mustache"))))
