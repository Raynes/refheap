(ns refheap.views.users
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]]
        [clavatar.core :only [gravatar]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers :as ph]
            [noir.session :as session]))

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (let [paste-count (users/count-user-pastes user)
          you? (= user (:username (session/get :user)))]
      (layout
       [:div#user
        (-> user-data :email (gravatar :size 70) ph/image)
        [:p
         (if you?
           "You have "
           (str user " has "))
         (users/count-user-pastes user {:private false})
         " public "
         (if you?
           (str "and " (users/count-user-pastes user {:private true}) " private paste(s).")
           "paste(s).")]
        (for [{:keys [paste-id summary date private]} (users/user-pastes
                                                       user page
                                                       (when-not you? {:private false}))]
          (list
           [:span.header
            (when private
              (ph/image "/img/lock.png"))
            (ph/link-to (str "/paste/" paste-id) paste-id)
            (date-string date)]
           [:div.syntax summary]
           [:br]))
        [:div.centered
         (when-not (= 1 page)
           [:a#newer.pagebutton {:href (str "/users/" user "?page=" (dec page))} "newer"])
         (when-not (= page (users/count-pages paste-count))
           [:a.pagebutton {:href (str "/users/" user "?page=" (inc page))} "older"])]]))))

(defpage "/users/:user" {:keys [user page]}
  (user-page user (Long. (or page "1"))))