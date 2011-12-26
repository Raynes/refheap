(ns refheap.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css]]))

(defpartial layout [& content]
  [:head
   [:title "The Refusal Heap"]
   (include-css "/css/refheap.css")
   (include-css "/css/pygments.css")]
  [:body
   [:div#header
    [:span "The Refusal Heap"]
    [:div#headerlinks
     [:a {:href "/pastes"} "All Pastes"]
     [:a {:href "/create"} "Create Paste"]
     [:a {:href "/login"} "Login"]
     [:a {:href "/register"} "Register"]]]
   [:div#content
    [:div#container
     content]
    [:div#footer
     [:p "Powered by Noir, MongoDB, and the cries of children the world over."]]]])