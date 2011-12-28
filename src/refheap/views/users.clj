(ns refheap.views.users
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]]
        [clavatar.core :only [gravatar]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers :as ph]))

(defn user-page [user]
  (layout
   [:div#user
    (-> user users/get-user :email (gravatar :size 70) ph/image)
    [:p "User has " (users/count-user-pastes user) " pastes."]
    (for [{:keys [paste-id summary]} (users/user-pastes user 1)]
      (list
       (ph/link-to (str "/paste/" paste-id) paste-id)
       [:div.syntax summary]
       [:br]))]))

(defpage "/users/:user" {:keys [user]}
  (user-page user))