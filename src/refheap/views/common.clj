(ns refheap.views.common
  (:require [noir.session :as session]
            [refheap.models.paste :as paste]
            [clavatar.core :refer [gravatar]]
            [me.raynes.laser :refer [defdocument defragment] :as l]
            [clojure.java.io :refer [resource]]))

(defn avatar [email size]
  (gravatar email :size size))

(defn static [file]
  (-> file resource slurp l/unescaped))

(let [html (l/parse-fragment (resource "refheap/views/templates/loggedin.html"))
      logged-out (static "refheap/views/templates/loggedout.html")]
  (defn logged-in [username]
    (let [user (or username
                   (and (bound? #'session/*noir-session*)
                        (:username (session/get :user))))]
      (if user
        (l/fragment html
                    (l/id= "userbutton") (comp (l/attr :href (str "/users/" user))
                                               (l/content user)))
        logged-out))))

(defragment head (resource "refheap/views/templates/head.html")
  [title heads]
  (when heads
    [(l/id= :last-include) (l/insert :right heads)])
  (l/element= :title) (l/content (or title "Refheap - The pastebin for your, you know, pastes")))

(defragment body (resource "refheap/views/templates/commonbody.html")
  [contents]
  (l/id= :useri) (l/content (logged-in nil))
  (l/id= :container) (l/content contents))

(let [html (l/parse (resource "refheap/views/templates/common.html"))]
  (defn layout
    ([content] (layout content nil nil))
    ([content title] (layout content title nil))
    ([content title heads]
       (l/document
        html
        (l/element= :head) (l/content (head title heads))
        (l/element= :body) (l/content (body content))))))

(defragment page-buttons (resource "refheap/views/templates/pagination.html")
  [base n per page]
  (if-not (= 1 page)
    [(l/id= :newer) (l/attr :href (str base "?page=" (dec page)))]
    [(l/id= :newer) (l/remove)])
  (if-not (or (zero? n) (= page (paste/count-pages n per)))
    [(l/id= :older) (l/attr :href (str base "?page=" (inc page)))]
    [(l/id= :older) (l/remove)]))