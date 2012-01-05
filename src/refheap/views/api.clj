(ns refheap.views.api
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]]
        [hiccup.page-helpers :only [link-to]])
  (:require [noir.session :as session]
            [refheap.models.api :as api]
            [refheap.models.paste :as paste]
            [refheap.models.users :as users]))

(defn api-page []
  (layout
   [:div.written
    [:p
     "RefHeap has a simple API for accessing and creating pastes. You can use this API without an "
     "account with RefHeap. However, if you want pastes that you create to be created under your "
     "account, you'll need to supply your username and an API token with the request. "
     "You want to keep this API token a secret. However, if you accidentally push it to Github "
     "or it is otherwise compromised, you can generate a new one at anytime. When you generate a new "
     "token, the old one no longer works."]
    [:p
     "Check out the API documentation on "
     (link-to "https://github.com/Raynes/refheap/wiki/Documentation:-API" "Github")
     "!"]
    (if-let [id (:id (session/get :user))]
      [:div#token
       [:code#tokentext (api/get-token id)]
       [:button#gentoken {:type "button"} "Generate New Token"]]
      [:p "Login to see your API token."])]))

(defpage "/api" []
  (api-page))

(defpage "/token/generate" []
  (when-let [id (:id (session/get :user))]
    (api/new-token id)))

(defpage [:post "/api/paste"] {:keys [private contents language username token]
                               :or {private "false"}}
  (let [user (api/validate-user username token)]
    (if (string? user)
      (api/response :unprocessable user)
      (let [paste (paste/paste language contents (api/string->bool private) user)]
        (if (string? paste)
          (api/response :bad paste)
          (api/response :created (api/process-paste paste)))))))

(defpage [:post "/api/paste/:id"] {:keys [id private contents language username token]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user) (api/response :unprocessable user)
       (nil? (:user paste)) (api/response :unprocessable "You can't edit anonymous pastes.")
       (nil? user) (api/response :unprocessable "You must be authenticated to edit pastes.")
       (not= (:id user) (:user paste)) (api/response :unprocessable "You can only edit pastes that you own.")
       :else (let [paste (paste/update-paste paste
                                             (or language (:language paste))
                                             (or contents (:raw-contents paste))
                                             (if (nil? private)
                                               (:private paste)
                                               (api/string->bool private))
                                             user)]
               (if (string? paste)
                 (api/response :bad paste)
                 (api/response :ok (api/process-paste paste))))))
    (api/response :not-found "Paste does not exist.")))

(defpage [:delete "/api/paste/:id"] {:keys [id username token]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user) (api/response :unprocessable user)
       (nil? (:user paste)) (api/response :unprocessable "You can't delete anonymous pastes.")
       (nil? user) (api/response :unprocessable "You must be authenticated to delete pastes.")
       (not= (:id user) (:user paste))
       (api/response :unprocessable "You can only delete pastes that you own.")
       :else (api/response :no-content (paste/delete-paste id))))
    (api/response :not-found "Paste doesn't exist.")))

(defpage "/api/paste/:id" {:keys [id username token]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user) (api/response :unprocessable user)
       (and (:private paste) (not= (:id user) (:user paste)))
       (api/response :unprocessable "You can't see private pastes that you don't own.")
       :else (api/response :ok (api/process-paste paste))))
    (api/response :not-found "Paste does not exist.")))