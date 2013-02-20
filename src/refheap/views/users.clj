(ns refheap.views.users
  (:require [refheap.models.users :as users]
            [stencil.core :as stencil]
            [refheap.views.paste :as paste]
            [noir.session :as session]
            [compojure.core :refer [defroutes GET]]
            [refheap.views.common :refer [layout avatar page-buttons]]
            [refheap.dates :refer [date-string]]
            [noir.response :refer [redirect]]
            [refheap.models.paste :refer [count-pages proper-page get-pastes]]
            [me.raynes.laser :refer [defragment] :as l]
            [clojure.java.io :refer [resource]]))

(defragment paste-header (resource "refheap/views/templates/userheader.html")
  [paste]
  [{:keys [paste-id date private]} paste]
  (l/element= :a) (comp (l/attr :href (str "/paste/" paste-id))
                        (l/content (str "Paste " paste-id)))
  (l/element= :span) #(when private %)
  (l/element= :div) #(update-in % [:content] conj (date-string date)))

(defragment user-page-fragment (resource "refheap/views/templates/users.html")
  [user page user-data]
  [you? (= user (:username (session/get :user)))
   others (when-not you? {:private false})
   total (users/count-user-pastes user others)]
  (l/element= :img) (l/attr :src (avatar (:email user-data) 70))
  (l/id= "header-data") (l/content [(if you?
                                      (str "You have ")
                                      (str user " has "))
                                    (str (users/count-user-pastes user {:private false}))
                                    " public "
                                    (when you?
                                      (str "and " (users/count-user-pastes user {:private true}) " private "))
                                    "pastes."])
  (l/id= "pastes") (l/content
                    (concat (paste/render-paste-previews (users/user-pastes user page others) paste-header)
                            (page-buttons (str "/users/" user) total 10 page))))

(defn user-page [user page]
  (when-let [user-data (users/get-user user)]
    (layout
     (user-page-fragment user page user-data)
     (str user "'s pastes")
     paste/show-head)))

(defroutes user-routes
  (GET "/users/:user" {{:keys [user page]} :params}
    (user-page (.toLowerCase user) 
               (proper-page (Long. (or page "1"))))))
