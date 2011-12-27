(ns refheap.views.home
  (:use [refheap.views.common :only [layout]]
        [noir.response :only [redirect]]
        [noir.core :only [defpartial defpage]]))

(defpage "/" []
  (redirect "/paste"))