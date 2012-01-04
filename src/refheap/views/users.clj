(ns refheap.views.users
  (:use [noir.core            :only [defpage]]
        [refheap.views.common :only [layout avatar page-buttons]]
        [refheap.dates        :only [date-string]]
        [noir.response        :only [redirect]]
        [refheap.models.paste :only [count-pages proper-page]])
  (:require [refheap.models.users :as users]
            [hiccup.page-helpers  :as ph]
            [noir.session         :as session]))

(defn pastes [ps]
  (for [{:keys [paste-id lines summary date private]} ps]
    (list
     [:div.preview-header
      (date-string date)
      (when private
          (list " (" [:span.private "Private"] ")"))
      [:div.right
       "[" (ph/link-to (str "/paste/" paste-id) "Link") "]"]]
     [:div.syntax summary
      (if (> lines 5) [:div.more (ph/link-to (str "/paste/" paste-id) "more...")])]
     [:br])))

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (let [you? (= user (:username (session/get :user)))
          others (when-not you? {:private false})
          paste-count (users/count-user-pastes user others)]
      (if (and (> page (count-pages paste-count 10))
               (not (and (zero? paste-count) (= 1 page))))
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
  (user-page (.toLowerCase user) (proper-page (Long. (or page "1")))))
