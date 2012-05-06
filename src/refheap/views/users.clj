(ns refheap.views.users
  (:require [refheap.models.users :as users]
            [stencil.core :as stencil]
            [refheap.views.paste :as paste]
            [noir.session :as session]
            [noir.core :refer [defpage]]
            [refheap.views.common :refer [layout avatar page-buttons]]
            [refheap.dates :refer [date-string]]
            [noir.response :refer [redirect]]
            [refheap.models.paste :refer [count-pages proper-page]]))

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (let [you? (= user (:username (session/get :user)))
          others (when-not you? {:private false})
          total (users/count-user-pastes user others)]
      (layout
        (stencil/render-file
          "refheap/views/templates/users"
          {:user (when-not you? {:user user})
           :private (when you? 
                      {:private-count (users/count-user-pastes user {:private true})})
           :public-count (users/count-user-pastes user {:private false})
           :pastes (paste/render-paste-previews 
                     (users/user-pastes user page others)
                     "refheap/views/templates/userheader")
           :paste-buttons (page-buttons (str "/users/" user) total 20 page)
           :gravatar (avatar (:email user-data) 70)})
        {:file "refheap/views/templates/showhead"
         :title (str user "'s pastes")}))))

(defpage "/users/:user" {:keys [user page]}
  (user-page (.toLowerCase user) 
             (proper-page (Long. (or page "1")))))
