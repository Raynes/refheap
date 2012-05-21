(ns refheap.views.api
  (:require [noir.session :as session]
            [stencil.core :as stencil]
            [refheap.models.api :as api]
            [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [noir.core :refer [defpage]]
            [refheap.views.common :refer [layout]]))

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

(defpage [:post "/api/paste"] {:keys [private contents language username token return]
                               :or {private "false"}}
  (let [user (api/validate-user username token)]
    (if (string? user)
      (api/response :unprocessable user return)
      (let [paste (paste/paste language contents (api/string->bool private) user)]
        (if (string? paste)
          (api/response :bad paste return)
          (api/response :created (api/process-paste paste) return))))))

(defpage [:post "/api/paste/:id"] {:keys [id private contents language username token return]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user)
       (api/response :unprocessable user return)
       (nil? (:user paste))
       (api/response :unprocessable "You can't edit anonymous pastes." return)
       (nil? user)
       (api/response :unprocessable "You must be authenticated to edit pastes." return)
       (not= (:id user) (:user paste))
       (api/response :unprocessable "You can only edit pastes that you own." return)
       :else (let [paste (paste/update-paste paste
                                             (or language (:language paste))
                                             (or contents (:raw-contents paste))
                                             (if (nil? private)
                                               (:private paste)
                                               (api/string->bool private))
                                             user)]
               (if (string? paste)
                 (api/response :bad paste return)
                 (api/response :ok (api/process-paste paste) return)))))
    (api/response :not-found "Paste does not exist." return)))

(defpage [:delete "/api/paste/:id"] {:keys [id username token return]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user)
       (api/response :unprocessable user return)
       (nil? (:user paste))
       (api/response :unprocessable "You can't delete anonymous pastes." return)
       (nil? user)
       (api/response :unprocessable "You must be authenticated to delete pastes." return)
       (not= (:id user) (:user paste))
       (api/response :unprocessable "You can only delete pastes that you own." return)
       :else (api/response :no-content (paste/delete-paste id) return)))
    (api/response :not-found "Paste doesn't exist." return)))

(defpage [:post "/api/paste/:id/fork"] {:keys [id username token return]}
  (if-let [paste (paste/get-paste id)]
    (let [user (api/validate-user username token)]
      (cond
       (string? user)
       (api/response :unprocessable user return)
       (= (:id user)
          (:user paste)) (api/response :unprocessable "You can't fork your own pastes." return)
       :else (api/response :created
                           (api/process-paste
                            (paste/paste (:language paste)
                                         (:raw-contents paste)
                                         (:private paste)
                                         user
                                         (:id paste)))
                           return)))
    (api/response :not-found "Paste doesn't exist." return)))

(defpage "/api/paste/:id" {:keys [id return]}
  (if-let [paste (paste/get-paste id)]
    (api/response :ok (api/process-paste paste) return)
    (api/response :not-found "Paste does not exist." return)))

(defpage "/api/paste/:id/highlight" {:keys [id return]}
  (if-let [paste (paste/get-paste id)]
    (api/response :ok {:content (:contents paste)} return)
    (api/response :not-found "Paste does not exist." return)))
