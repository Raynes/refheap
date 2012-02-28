(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons header]]
        [noir.response :only [redirect content-type]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [noir.session :as session]
            [stencil.core :as stencil]))

(defn create-paste-page [lang & [old]]
  (let [lang (or (:language old) "Clojure")]
    (layout
      (stencil/render-file
        "refheap/views/templates/paste"
        {:url (if old
                (str "/paste/" (:paste-id old) "/edit")
                "/paste/create")
         :languages (for [lang (sort #(.compareToIgnoreCase % %2)
                                     (keys (dissoc paste/lexers lang)))]
                      {:language lang})
         :selected lang
         :checked (:private old)
         :old (:raw-contents old)
         :button (if old "Save!" "Paste!")})
      (if old 
        (str "Editing paste " (:paste-id old))
        "RefHeap"))))

(defn fullscreen-paste [id]
  (when-let [contents (:contents (paste/get-paste id))]
    (stencil/render-file
      "refheap/views/templates/fullscreen"
      {:contents contents})))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date fork]
              :as all}
             (paste/get-paste id)]
    (let [user-id (:id (session/get :user))
          paste-user (if user
                       (:username (users/get-user-by-id user))
                       "anonymous")]
      (layout
        (stencil/render-file
          "refheap/views/templates/pasted"
          {:language language
           :private private
           :lines lines
           :id id
           :username (if user
                       (str "<a href=\"/users/" paste-user "\">" paste-user "</a>")
                       paste-user) 
           :date (date-string date)
           :forked (when fork {:from (if-let [paste (:paste-id (paste/get-paste-by-id fork))]
                                       (str "<a href=\"/paste/" paste "\">" paste "</a>")
                                       "[deleted]")})
           :owner (when (and user-id (= user user-id)) {:id id})
           :fork (when (and user-id (not= user user-id)) {:id id})
           :contents contents})
        (str paste-user "'s paste: " id)))))

(defpage "/paste/:id/fullscreen" {:keys [id]}
  (fullscreen-paste id))

(defpage "/paste/:id/framed" {:keys [id]}
  (fullscreen-paste id))

(defn render-paste-previews [pastes header-template]
  (stencil/render-file
    "refheap/views/templates/preview"
    {:pastes (for [{:keys [paste-id lines summary date user private]} pastes]
               {:header (stencil/render-file
                          header-template
                          {:user (when user (users/get-user-by-id user))
                           :date (date-string date)
                           :private private})
                :id paste-id
                :summary summary
                :more (> lines 5)})}))

(defn render-embed-page [paste]
  (let [id (:paste-id paste)]
    (layout
      (stencil/render-file
        "refheap/views/templates/embed"
        {:id (:paste-id paste)})
      (str "Embedding paste " id))))

(defn all-pastes-page [page]
  (let [paste-count (paste/count-pastes false)]
    (if (> page (paste/count-pages paste-count 20))
      (redirect "/paste")
      (layout
        (stencil/render-file
          "refheap/views/templates/all"
          {:pastes (render-paste-previews
                     (paste/get-pastes page)
                     "refheap/views/templates/allheader")
           :paste-buttons (page-buttons "/pastes" paste-count 20 page)})
        (str "All pastes")))))

(defn fail [error]
  (layout
    (stencil/render-file
      "refheap/views/templates/fail"
      {:message error})
    "You broke it"))

(defpage "/paste" {:keys [lang]}
  (create-paste-page lang))

(defpage "/paste/:id/edit" {:keys [id]}
  (when-let [user (:id (session/get :user))]
    (let [paste (paste/get-paste id)]
      (when (= (:user paste) user)
        (create-paste-page nil paste)))))

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
    (content-type "text/plain; charset=utf-8" content)))

(defpage "/paste/:id/embed" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (render-embed-page paste)))

(defpage [:post "/paste/:id/edit"] {:keys [id paste language private]}
  (let [paste (paste/update-paste
               (paste/get-paste id)
               language
               paste
               private
               (session/get :user))]
    (if (map? paste)
      (redirect (str "/paste/" (:paste-id paste)))
      (fail paste))))

(defpage [:post "/paste/create"] {:keys [paste language private]}
  (let [paste (paste/paste language paste private (session/get :user))]
    (if (map? paste)
      (redirect (str "/paste/" (:paste-id paste)))
      (fail paste))))

(defpage "/paste/:id" {:keys [id]}
  (show-paste-page id))

(defpage "/pastes" {:keys [page]}
  (all-pastes-page (paste/proper-page (Long. (or page "1")))))
