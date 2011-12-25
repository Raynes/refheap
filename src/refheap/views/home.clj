(ns refheap.views.home
  (:use [refheap.views.common :only [layout]]
        [noir.core :only [defpartial defpage]]))

(defpartial welcome-page []
  (layout "I wish I actually did things."))

(defpage "/" []
  (welcome-page))