(ns refheap.views.home
  (:require [refheap.views.common :refer [layout]]
            [noir.response :refer [redirect]]
            [noir.statuses :refer [set-page!]]
            [noir.core :refer [defpage]]))

(defn not-found []
  (layout "<p class=\"header\">Insert fancy 404 image here.</p>"))

(set-page! 404 (not-found))

(defpage "/" [] (redirect "/paste"))

;; Convenience routes

(defpage "/gh" [] (redirect "https://github.com/Raynes/refheap"))
(defpage "/ghi" [] (redirect "https://github.com/Raynes/refheap/issues"))
(defpage "/wiki" [] (redirect "https://github.com/Raynes/refheap/wiki"))
