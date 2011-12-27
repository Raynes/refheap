(ns refheap.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js link-to]]))

(defpartial layout [& content]
  [:head
   [:title "The Refusal Heap"]
   (include-css "/css/refheap.css")
   (include-css "/css/native.css")
   (include-js  "/js/jquery-1.7.1.min.js")
   (include-js  "/js/refheap.js")]
  [:body
   [:div#header
    [:a#site {:href "/paste"} "The Refusal Heap"]
    [:div#headerlinks
     (link-to "/pastes" "All Pastes")
     (link-to "/about" "About")
     [:img#signin {:src "/img/browserid.png"}]]]
   [:div#content
    [:div#container
     content]
    [:div#footer
     [:p
      "Powered by " (link-to "http://clojure.org" "Clojure") ", "
      (link-to "http://webnoir.org" "Noir") ", "
      (link-to "http://mongodb.org" "MongoDB") ", "
      (link-to "http://pygments.org/" "Pygments")
      " and the cries of children the world over."]]]])