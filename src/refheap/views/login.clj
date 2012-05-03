(ns refheap.views.login
  (:use [refheap.views.common :only [layout logged-in]]
        [noir.core :only [defpage]]
        [noir.response :only [redirect json]])
  (:require [refheap.models.login :as login]
            [stencil.core :as stencil]
            [noir.session :as session]))

(defn create-user-page [email]
  (session/flash-put! :email email)
  (stencil/render-file
    "refheap/views/templates/createuser"
    {:error (when-let [error (session/flash-get :error)]
              {:message error})}))

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

(defpage "/user/logout" []
  (session/remove! :user)
  (redirect "/paste"))

(defpage [:post "/user/verify"] {:keys [assertion]}
  (when-let [{:keys [email]} (login/verify-assertion assertion)]
    (if-let [username (login/user-exists email)]
     (json {:login-html (logged-in username)})
      (do
        (session/flash-put! :email email)
        ;; TODO: This results in css and stuff being added twice.
        ;; It has no effect on the layout but is still dirty. Fix.
        (json {:chooselogin-html (layout (create-user-page email))})))))
