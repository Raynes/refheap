(ns refheap.views.home
  (:use [refheap.views.common :only [layout]]
        [noir.response :only [redirect]]
        [noir.statuses :only [set-page!]]
        [noir.core :only [defpartial defpage]]))

(defn not-found []
  (layout [:p.centered "Just no."]))

(set-page! 404 (not-found))

(defpage "/" []
  (redirect "/paste"))