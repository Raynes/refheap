(ns refheap.views.users
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout avatar page-buttons]]
        [refheap.dates :only [date-string]]
        [noir.response :only [redirect]]
        [refheap.models.paste :only [count-pages proper-page]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers :as ph]
            [noir.session :as session]))

(defn pastes [ps]
  (for [{:keys [paste-id summary date private]} ps]
    (list
     [:span.header
      (ph/link-to (str "/paste/" paste-id) paste-id)
      " pasted on "
      (date-string date)
      (when private
        (ph/image "/img/lock.png"))]
     [:div.syntax summary]
     [:br])))

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (let [you? (= user (:username (session/get :user)))
          others (when-not you? {:private false})
          paste-count (users/count-user-pastes user others)]
      (if (> page (count-pages paste-count 10))
        (redirect "/paste")
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
          (pastes (users/user-pastes user page others))
          (page-buttons (str "/users/" user) paste-count 10 page)])))))

(defpage "/users/:user" {:keys [user page]}
  (user-page user (proper-page (Long. (or page "1")))))
