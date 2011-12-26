(ns refheap.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css link-to]]))

(defpartial layout [& content]
  [:head
   [:title "The Refusal Heap"]
   (include-css "/css/refheap.css")
   (include-css "/css/pygments.css")]
  [:body
   [:div#header
    [:a#site {:href "/paste"} "The Refusal Heap"]
    [:div#headerlinks
     [:a {:href "/pastes"} "All Pastes"]
     [:a {:href "/login"} "Login"]
     [:a {:href "/register"} "Register"]]]
   [:div#content
    [:div#container
     content]
    [:div#footer
     [:p
      "Powered by " (link-to "http://clojure.org" "Clojure") ", "
      (link-to "http://webnoir.org" "Noir") ", "
      (link-to "http://mongodb.org" "MongoDB")
      " and the cries of children the world over."]]]])