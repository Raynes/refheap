(ns refheap.views.common
  (:use [clavatar.core :only [gravatar]])
  (:require [noir.session :as session]
            [hiccup.page-helpers :as ph]
            [refheap.models.paste :as paste]))

(defn avatar [email size]
  (ph/image (gravatar email :size size)))

(defn analytics []
  (ph/javascript-tag
   "var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-28074244-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();"))

(defn layout [& content]
  (ph/html5
   [:head
    [:title "RefHeap"]
    [:link {:rel "shortcut icon" :href "/img/favicon.ico"}]
    (ph/include-css "http://fonts.googleapis.com/css?family=Open+Sans")
    (ph/include-css "/css/refheap.css")
    (ph/include-css "/css/native.css")
    (ph/include-js  "/js/jquery-1.7.1.min.js")
    (ph/include-js  "https://browserid.org/include.js")
    (ph/include-js  "/js/refheap.js")
    (analytics)]
   [:body
    [:div#site-container
     [:div#header
      [:a#site {:href "/paste"} "The Reference Heap"]
      [:div.headerlinks
       (ph/link-to "/pastes" "All Pastes")
       (ph/link-to "/about" "About")
       (ph/link-to "/api" "API")
       [:div#useri
        (if-let [user (and (bound? #'session/*noir-session*)
                           (:username (session/get :user)))]
          [:div
           [:b (ph/link-to (str "/users/" user) user)]
           (ph/link-to "/users/logout" "logout")]
          [:img#signin.imgbutton {:src "/img/browserid.png"}])]]]
     [:div#content
      [:div#container content]
      [:div#footer
       [:p.centered
        (ph/link-to "https://github.com/Raynes/refheap" "Refheap")
        " is powered by " (ph/link-to "http://clojure.org" "Clojure") ", "
        (ph/link-to "http://webnoir.org" "Noir") ", "
        (ph/link-to "http://mongodb.org" "MongoDB") ", "
        (ph/link-to "http://pygments.org/" "Pygments")
        ", and "
        (ph/link-to "http://photos.geni.com/p13/45/9a/44/22/5344483904b52482/img_7798_large.jpg" "Iguana") ". Please read: the "
        (ph/link-to "/legal/tos" "terms of service") ", and "
        (ph/link-to "/legal/privacy" "privacy") " policy."]]]]]))

(defn page-buttons [base n per page]
  [:div.centered
   (when-not (= 1 page)
     [:a#newer.pagebutton {:href (str base "?page=" (dec page))} "newer"])
   (when-not (or (zero? n) (= page (paste/count-pages n per)))
     [:a.pagebutton {:href (str base "?page=" (inc page))} "older"])])
