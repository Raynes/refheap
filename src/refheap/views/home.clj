(ns refheap.views.home
  (:require [noir.response :refer [redirect]]
            [compojure.core :refer [defroutes GET]]))

(defroutes home-routes
  (GET "/gh"   [] (redirect "https://github.com/Raynes/refheap"))
  (GET "/ghi"  [] (redirect "https://github.com/Raynes/refheap/issues"))
  (GET "/wiki" [] (redirect "https://github.com/Raynes/refheap/wiki")))
