(ns refheap.views.common
  (:use [hiccup.page-helpers :only [html5 include-css include-js link-to image]]
        [clavatar.core :only [gravatar]])
  (:require [noir.session :as session]
            [refheap.models.paste :as paste]))

(defn avatar [email size]
  (image (gravatar email :size size)))

(defn layout [& content]
  (html5
   [:head
    [:title "The Refuse Heap"]
    (include-css "http://fonts.googleapis.com/css?family=Open+Sans")
    (include-css "/css/refheap.css")
    (include-css "/css/native.css")
    (include-js  "/js/jquery-1.7.1.min.js")
    (include-js  "https://browserid.org/include.js")
    (include-js  "/js/refheap.js")]
   [:body
    [:div#header
     [:a#site {:href "/paste"} "The Refuse Heap"]
     [:div.headerlinks
      (link-to "/pastes" "All Pastes")
      (link-to "/about" "About")]
     [:div#useri.headerlinks
      (if-let [user (and (bound? #'session/*noir-session*)
                         (:username (session/get :user)))]
        [:div
         (link-to (str "/users/" user) user)
         (link-to "/users/logout" "logout")]
        [:img#signin.imgbutton {:src "/img/browserid.png"}])]]
    [:div#content
     [:div#container content]
     [:div#footer
      [:p.centered
       (link-to "https://github.com/Raynes/refheap" "Refheap")
       " is powered by " (link-to "http://clojure.org" "Clojure") ", "
       (link-to "http://webnoir.org" "Noir") ", "
       (link-to "http://mongodb.org" "MongoDB") ", "
       (link-to "http://pygments.org/" "Pygments")
       " and the cries of children the world over."]]]]))

(defn page-buttons [n page]
  [:div.centered
   (when-not (= 1 page)
     [:a#newer.pagebutton {:href (str "/pastes?page=" (dec page))} "newer"])
   (when-not (or (zero? n) (= page (paste/count-pages n)))
     [:a.pagebutton {:href (str "/pastes?page=" (inc page))} "older"])])