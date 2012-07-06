(ns refheap.views.common
  (:require [noir.session :as session]
            [stencil.core :as stencil]
            [refheap.models.paste :as paste]
            [clavatar.core :refer [gravatar]]))

(defn avatar [email size]
  (gravatar email :size size))

(defn logged-in [username]
  (stencil/render-file
    "refheap/views/templates/loggedin"
    {:user (when-let [user (or username
                               (and (bound? #'session/*noir-session*)
                                    (:username (session/get :user))))]
             {:username user})}))

(defn layout [body & [head]]
  (stencil/render-file
    "refheap/views/templates/common"
    {:user (logged-in nil) 
     :title (or (:title head) "RefHeap")
     :content body
     :head (when-let [head-file (:file head)]
             (stencil/render-file head-file (dissoc head :file :title)))}))

(defn body [contents]
  (stencil/render-file
   "refheap/views/templates/commonbody"
   {:user (logged-in nil)
    :content contents}))

(defn page-buttons [base n per page]
  (stencil/render-file
    "refheap/views/templates/pagination"
    {:newer (when-not (= 1 page) {:base base, :page (dec page)})
     :older (when-not (or (zero? n) (= page (paste/count-pages n per)))
              {:base base, :page (inc page)})}))
