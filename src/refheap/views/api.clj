(ns refheap.views.api
  (:use [noir.core :only [defpage]]
        [refheap.views.common :only [layout]])
  (:require [noir.response :as response]
            [noir.session :as session]
            [refheap.models.api :as api]
            [refheap.models.paste :as paste]
            [refheap.models.users :as users]))

(defn api-page []
  (layout
   [:div.written
    [:p
     "RefHeap has a simple API for accessing and creating tweets. You can use this API without an "
     "account with RefHeap. However, if you want pastes that you create to be created under your "
     "account, you'll need to supply your username and an API token with the request. "
     "You want to keep this API token a secret. However, if you accidentally push it to Github "
     "or it is otherwise compromised, you can generate a new one at anytime. When you generate a new "
     "token, the old one no longer works."]
    (if-let [id (:id (session/get :user))]
      [:div#token
       [:code#tokentext (api/get-token id)]
       [:button#gentoken {:type "button"} "Generate New Token"]]
      [:p "Login to see your API token!"])]))

(defpage "/api" []
  (api-page))

(defpage "/token/generate" []
  (when-let [id (:id (session/get :user))]
    (api/new-token id)))

(defpage [:post "/api/v1/paste/create"] {:keys [private contents language username token]
                                         :or {private "false"}}
  (response/json
   (let [user (api/validate-user username token)]
     (if (string? user)
       {:error user}
       (let [paste (paste/paste language contents (api/string->bool private) user)]
         (if (string? paste)
           {:error paste}
           (api/process-paste paste)))))))