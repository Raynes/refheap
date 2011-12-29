(ns refheap.views.users
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]]
        [clavatar.core :only [gravatar]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers :as ph]))

(defn user-page [user page]
  (let [paste-count (users/count-user-pastes user)]
    (layout
     [:div#user
      (-> user users/get-user :email (gravatar :size 70) ph/image)
      [:p "User has " paste-count " paste(s)."]
      (for [{:keys [paste-id summary date]} (users/user-pastes user page)]
        (list
         [:span.header
          (ph/link-to (str "/paste/" paste-id) paste-id)
          (date-string date)]
         [:div.syntax summary]
         [:br]))
      [:div.centered
       (when-not (= 1 page)
         [:a#newer.pagebutton {:href (str "/users/" user "?page=" (dec page))} "newer"])
       (when-not (= page (users/count-pages paste-count))
         [:a.pagebutton {:href (str "/users/" user "?page=" (inc page))} "older"])]])))

(defpage "/users/:user" {:keys [user page]}
  (user-page user (Long. (or page "1"))))