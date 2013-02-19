(ns refheap.views.common
  (:require [noir.session :as session]
            [refheap.models.paste :as paste]
            [stencil.core :as stencil]
            [clavatar.core :refer [gravatar]]
            [me.raynes.laser :refer [defdocument defragment] :as l]
            [clojure.java.io :refer [resource]]))

(defn avatar [email size]
  (gravatar email :size size))

(defn logged-in [username]
  (let [user (or username
                 (and (bound? #'session/*noir-session*)
                      (:username (session/get :user))))]
    (if user
      (l/fragment (l/parse-fragment (resource "refheap/views/templates/loggedin.html"))
                      (l/id= "userbutton") (comp (l/attr :href (str "/users/" user))
                                                 (l/content user)))
      (l/nodes (resource "refheap/views/templates/loggedout.html")))))

(defragment head (resource "refheap/views/templates/head.html")
  [title heads]
  (l/id= "last-include") #(if heads [% (l/unescaped heads)] %)
  (l/element= :title) (l/content (or title "Refheap")))

(defragment body (resource "refheap/views/templates/commonbody.html")
  [contents]
  (l/id= "useri") (l/content (logged-in nil))
  (l/id= "container") (l/content contents))

(let [html (l/parse (resource "refheap/views/templates/common.html"))]
  (defn layout
    ([content] (layout content "Refheap" nil))
    ([content title] (layout content title nil))
    ([content title heads]
       (l/document
        html
        (l/element= :head) (l/content (head title heads)) 
        (l/element= :body) (l/content (body content))))))

(defragment page-buttons (resource "refheap/views/templates/pagination.html")
  [base n per page]
  (l/id= "newer") #(when-not (= 1 page)
                     (assoc-in % [:attrs :href] (str base "?page=" (dec page))))
  (l/id= "older") #(when-not (or (zero? n) (= page (paste/count-pages n per)))
                     (assoc-in % [:attrs :href] (str base "?page=" (inc page)))))