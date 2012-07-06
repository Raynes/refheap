(ns refheap.views.login
  (:require [refheap.models.login :as login]
            [stencil.core :as stencil]
            [noir.session :as session]
            [refheap.views.common :refer [body logged-in]]
            [noir.core :refer [defpage]]
            [noir.response :refer [redirect json]]))

(defn create-user-page [email]
  (session/flash-put! :email email)
  (stencil/render-file
    "refheap/views/templates/createuser"
    {:error (when-let [error (session/flash-get :error)]
              {:message error})}))

(defpage [:post "/user/create"] {:keys [name]}
  (if-let [email (session/flash-get :email)]
    (if (login/create-user email name)
      (redirect "/paste")
      (create-user-page email))))

(defpage "/user/logout" []
  (session/remove! :user)
  (redirect "/paste"))

(defpage [:post "/user/verify"] {:keys [assertion]}
  (when-let [{:keys [email]} (login/verify-assertion assertion)]
    (if-let [username (login/user-exists email)]
     (json {:login-html (logged-in username)})
      (do
        (session/flash-put! :email email)
        (json {:chooselogin-html (body (create-user-page email))})))))
