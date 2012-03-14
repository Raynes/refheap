(ns refheap.views.api
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]])
  (:require [noir.session :as session]
            [stencil.core :as stencil]
            [refheap.models.api :as api]
            [refheap.models.paste :as paste]
            [refheap.models.users :as users]))

(defn api-page []
  (stencil/render-file
    "refheap/views/templates/api"
    {:logged (when-let [id (:id (session/get :user))]
               {:token (api/get-token id)})}))

(defpage "/api" []
  (layout (api-page)
          {:file "refheap/views/templates/apihead"}))

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

(defpage [:post "/api/paste/:id/fork"] {:keys [id username token]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user) (api/response :unprocessable user)
       (= (:id user) (:user paste)) (api/response :unprocessable "You can't fork your own pastes.")
       :else (api/response :created
                           (api/process-paste
                            (paste/paste (:language paste)
                                         (:raw-contents paste)
                                         (:private paste)
                                         user
                                         (:id paste))))))
    (api/response :not-found "Paste doesn't exist.")))

(defpage "/api/paste/:id" {:keys [id]}
  (if-let [paste (paste/get-paste id)]
    (api/response :ok (api/process-paste paste))
    (api/response :not-found "Paste does not exist.")))
