(ns refheap.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css]]))

(defpartial layout [& content]
  [:head
   [:title "The Refusal Heap"]
   (include-css "/css/refheap.css")]
  [:body
   [:div#header
    [:span "The Refusal Heap"]
    [:div#headerlinks
     [:a {:href "/pastes"} "Pastes"]
     [:a {:href "/login"} "Login"]
     [:a {:href "/register"} "Register"]]]
   [:div#content
    content]])
