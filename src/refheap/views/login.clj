(ns refheap.views.login
  (:use [hiccup.form-helpers :only [text-field submit-button form-to]]
        [refheap.views.common :only [layout]]
        [noir.core :only [defpage]]
        [noir.response :only [redirect]])
  (:require [refheap.models.login :as login]
            [noir.session :as session]))

(defn create-user-page [email]
  (session/flash-put! :email email)
  (layout
   [:div#login
    (when-let [error (session/flash-get :error)]
      [:p.evil error])
    (form-to
     [:post "/user/create"]
     [:p "You're almost there! Just enter a username and you'll be on your way."]
     (text-field :name)
     (submit-button "submit"))]))

(defpage [:post "/user/create"] {:keys [name]}
  (let [email (session/flash-get :email)]
    (if (login/create-user email name)
      (redirect "/paste")
      (create-user-page email))))

(defpage [:post "/user/login"] {:keys [email]}
  (if-let [username (login/user-exists email)]
    (redirect "/paste")
    (do (session/flash-put! :email email)
        (redirect "/user/create"))))

(defpage "/users/logout" []
  (session/remove! :user)
  (redirect "/paste"))

(defpage [:post "/user/verify"] {:keys [assertion]}
  (when-let [{:keys [email]} (login/verify-assertion assertion)]
    (if (login/user-exists email)
      (redirect "/paste")
      (create-user-page email))))