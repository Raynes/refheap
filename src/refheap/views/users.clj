(ns refheap.views.users
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout avatar]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers :as ph]
            [noir.session :as session]))

(defn pastes [ps]
  (for [{:keys [paste-id summary date private]} ps]
    (list
     [:span.header
      (ph/link-to (str "/paste/" paste-id) paste-id)
      (date-string date)
      (when private
        (ph/image "/img/lock.png"))]
     [:div.syntax summary]
     [:br])))

(defn buttons [user page]
  [:div.centered
   (when-not (= 1 page)
     [:a#newer.pagebutton {:href (str "/users/" user "?page=" (dec page))} "newer"])
   (when-not (= page (users/count-pages paste-count))
     [:a.pagebutton {:href (str "/users/" user "?page=" (inc page))} "older"])])

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (let [paste-count (users/count-user-pastes user)
          you? (= user (:username (session/get :user)))]
      (layout
       [:div#user
        (-> user-data :email (avatar 70))
        [:p
         (if you?
           "You have "
           (str user " has "))
         (users/count-user-pastes user {:private false})
         " public "
         (if you?
           (str "and " (users/count-user-pastes user {:private true}) " private paste(s).")
           "paste(s).")]
        (pastes (users/user-pastes user page (when-not you? {:private false})))
        (buttons user page)]))))

(defpage "/users/:user" {:keys [user page]}
  (user-page user (Long. (or page "1"))))