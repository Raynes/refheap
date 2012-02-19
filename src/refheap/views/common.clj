(ns refheap.views.common
  (:use [clavatar.core :only [gravatar]])
  (:require [noir.session :as session]
            [stencil.core :as stencil]
            [hiccup.core :as hiccup]
            [hiccup.page-helpers :as ph]
            [refheap.models.paste :as paste]))

(defn avatar [email size]
  (gravatar email :size size))

(defn logged-in [username]
  (stencil/render-file
    "refheap/views/templates/loggedin"
    {:user (when-let [user (or username
                               (and (bound? #'session/*noir-session*)
                                    (:username (session/get :user))))]
             {:username user})}))

(defn layout [body]
  (stencil/render-file
    "refheap/views/templates/common"
    {:user (logged-in nil)
     :content (hiccup/html body)}))

(def header nil)

(defn page-buttons [base n per page]
  (stencil/render-file
    "refheap/views/templates/pagination"
    {:newer (when-not (= 1 page) {:base base, :page (dec page)})
     :older (when-not (or (zero? n) (= page (paste/count-pages n per)))
              {:base base, :page (inc page)})}))
