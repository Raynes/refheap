(ns refheap.views.api
  (:use [noir.core :only [defpage]])
  (:require [noir.response :as response]
            [refheap.models.api :as api]
            [refheap.models.paste :as paste]))

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