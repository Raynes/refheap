(ns refheap.views.common
  (:require [noir.session :as session]
            [refheap.models.paste :as paste]
            [stencil.core :as stencil]
            [clavatar.core :refer [gravatar]]
            [me.raynes.laser :refer [defdocument defragment] :as laser]
            [clojure.java.io :refer [resource]]))

(defn avatar [email size]
  (gravatar email :size size))

(defn logged-in [username]
  (let [user (or username
                 (and (bound? #'session/*noir-session*)
                      (:username (session/get :user))))]
    (if user
      (laser/fragment (laser/parse-fragment (resource "refheap/views/templates/loggedin.html"))
                      (laser/id= "userbutton") (comp (laser/attr :href (str "/users/" user))
                                                     (laser/content user)))
      (laser/parse-fragment* (resource "refheap/views/templates/loggedout.html")))))

(defragment head (resource "refheap/views/templates/head.html")
  [extra-head]
  (laser/id= "last-include")
  (fn [node] (into [node] (when-let [items (or (:head-nodes extra-head) (:file extra-head))]
                            (laser/nodes
                             (if (string? items)
                               (stencil/render-file items extra-head)
                               extra-head)))))
  (laser/element= :title) (laser/content (or (:title extra-head) "Refheap")))

(defragment body (resource "refheap/views/templates/commonbody.html")
  [contents]
  (laser/id= "useri") (laser/html-content (logged-in nil))
  (laser/id= "container") (laser/html-content contents))

(defdocument layout (resource "refheap/views/templates/common.html")
  [content & [extra-head]]
  (laser/element= :head) (laser/html-content (head extra-head)) 
  (laser/element= :body) (laser/html-content (body content)))

(defragment page-buttons (resource "refheap/views/templates/pagination.html")
  [base n per page]
  (laser/id= "newer") #(when-not (= 1 page)
                         (assoc-in % [:attrs :href] (str base "?page=" (dec page))))
  (laser/id= "older") #(when-not (or (zero? n) (= page (paste/count-pages n per)))
                         (assoc-in % [:attrs :href] (str base "?page=" (inc page)))))