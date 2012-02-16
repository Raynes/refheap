(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons header]]
        [noir.response :only [redirect content-type]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [noir.session :as session]
            [stencil.core :as stencil]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

(defn create-paste-page [lang & [old]]
  (stencil/render-file
    "refheap/views/templates/paste"
    {:url (if old
            (str "/paste/" (:paste-id old) "/edit")
            "/paste/create")
     :languages (for [lang (sort #(.compareToIgnoreCase % %2)
                                 (keys (dissoc paste/lexers "Clojure")))]
                  {:language lang})
     :old (:raw-contents old)}))

(defn fullscreen-paste [id]
  (when-let [contents (:contents (paste/get-paste id))]
    (stencil/render-file
      "refheap/views/templates/fullscreen"
      {:contents contents})))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date fork]
              :as all}
             (paste/get-paste id)]
    (stencil/render-file
      "refheap/views/templates/pasted"
      {:language language
       :lines lines
       :id id
       :username (if user
                   (let [user (:username (users/get-user-by-id user))]
                     (str "<a href=\"/users/" user "\">" user "</a>"))
                   "anonymous")
       :date (date-string date)
       :forked (when fork {:from (if-let [paste (:paste-id (paste/get-paste-by-id fork))]
                                   (str "<a href=\"/paste/" paste "\">" paste "</a>")
                                   "[deleted]")})
       :owner (when (= user (:id (session/get :user)))
                {:id id})
       :contents contents})))

(defpage "/paste/:id/fullscreen" {:keys [id]}
  (fullscreen-paste id))

(defpage "/paste/:id/framed" {:keys [id]}
  (fullscreen-paste id))

(defn render-paste-previews [pastes]
  (stencil/render-file
    "refheap/views/templates/preview"
    {:pastes (for [{:keys [paste-id lines summary date user]} pastes]
               {:user (when user (users/get-user-by-id user))
                :date (date-string date)
                :id paste-id
                :summary summary
                :more (> lines 5)})}))

(defn render-embed-page [paste]
  (stencil/render-file
    "refheap/views/templates/embed"
    {:id (:paste-id paste)}))

(defn all-pastes-page [page]
  (let [paste-count (paste/count-pastes false)]
    (if (> page (paste/count-pages paste-count 20))
      (redirect "/paste")
      (stencil/render-file
        "refheap/views/templates/all"
        {:pastes (render-paste-previews (paste/get-pastes page))
         :paste-buttons (page-buttons "/pastes" paste-count 20 page)}))))

(defn fail [error]
  (stencil/render-file
    "refheap/views/templates/fail"
    {:message error}))

(defpage "/paste" {:keys [lang]}
  (layout (create-paste-page lang)))

(defpage "/paste/:id/edit" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (when (= (:user paste) (:id (session/get :user)))
      (layout (create-paste-page nil paste)))))

(defpage "/paste/:id/fork" {:keys [id]}
  (let [user (:id (session/get :user))
        paste (paste/get-paste id)]
    (when (and user paste (not= (:user paste) user))
      (let [forked (paste/paste (:language paste)
                                (:raw-contents paste)
                                (:private paste)
                                (session/get :user)
                                (:id paste))]
        (redirect (str "/paste/" (:paste-id forked)))))))

(defpage "/paste/:id/delete" {:keys [id]}
  (when-let [user (:user (paste/get-paste id))]
    (when (= user (:id (session/get :user)))
      (paste/delete-paste id)
      (redirect "/paste"))))

(defpage "/paste/:id/raw" {:keys [id]}
  (when-let [content (:raw-contents (paste/get-paste id))]
    (content-type "text/plain" content)))

(defpage "/paste/:id/embed" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (layout (render-embed-page paste))))

(defpage [:post "/paste/:id/edit"] {:keys [id paste language private]}
  (let [paste (paste/update-paste
               (paste/get-paste id)
               language
               paste
               private
               (session/get :user))]
    (if (map? paste)
      (redirect (str "/paste/" (:paste-id paste)))
      (layout (fail paste)))))

(defpage [:post "/paste/create"] {:keys [paste language private]}
  (let [paste (paste/paste language paste private (session/get :user))]
    (if (map? paste)
      (redirect (str "/paste/" (:paste-id paste)))
      (layout (fail paste)))))

(defpage "/paste/:id" {:keys [id]}
  (layout (show-paste-page id)))

(defpage "/pastes" {:keys [page]}
  (layout (all-pastes-page (paste/proper-page (Long. (or page "1"))))))
