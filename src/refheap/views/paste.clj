(ns refheap.views.paste
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [refheap.highlight :refer [lexers]]
            [refheap.utilities :refer [to-booleany escape-string pluralize safe-parse-long]]
            [noir.session :as session]
            [noir.response :refer [redirect content-type]]
            [stencil.core :as stencil]
            [me.raynes.laser :refer [defragment] :as l]
            [clojure.java.io :refer [resource]]
            [compojure.core :refer [defroutes GET POST]]
            [refheap.views.common :refer [layout avatar page-buttons static]]
            [refheap.dates :refer [date-string]]
            [clojure.string :refer [split join]]))

(defn paste-url [paste & [suffix]]
  (if-let [version (:version paste)]
    (str "/" (:paste-id paste) "/history/" version suffix)
    (str "/" (:paste-id paste) suffix)))

(defn paste-username [paste]
  (if-let [user (:user paste)]
    (:username (users/get-user-by-id user))
    "anonymous"))

(defragment paste-page-fragment (resource "refheap/views/templates/paste.html")
  [lang & [old]]
  (l/child-of (l/id= "language")
              (l/negate (l/attr? :selected)))
  (fn [node]
    (for [lang (sort #(.compareToIgnoreCase % %2)
                     (keys (dissoc lexers lang)))]
      (l/on node (l/attr :value lang) (l/content lang))))
  (l/attr? :selected) (let [lang (or lang (:language old) (session/get :last-lang) "Clojure")]
                        (comp (l/attr :value lang)
                              (l/content lang)))
  (l/element= :form) (l/attr :action (if old
                                       (paste-url old "/edit")
                                       "/create"))
  (when (:private old)
    [(l/attr= :name :private) (l/attr :checked "")])
  (when old
    [(l/element= :textarea) (l/content (:raw-contents old))])
  (l/id= :submit-button) (l/attr :value (if old "Save!" "Paste!")))


(let [head (static "refheap/views/templates/createhead.html")]
  (defn paste-page [lang & [old]]
    (layout
     (paste-page-fragment lang old)
     (when old
       (str "Editing paste " (:paste-id old)))
     head)))

(def show-head (static "refheap/views/templates/showhead.html"))

(let [head (static "refheap/views/templates/head.html")
      html (l/parse (resource "refheap/views/templates/fullscreen.html"))
      fullscreen #(l/document html
                              (l/element= :head) (l/content [head show-head])
                              (l/class= :syntax) (l/content (l/unescaped %)))]
  (defn fullscreen-paste [id]
    (when-let [contents (:contents (paste/view-paste id))]
      (fullscreen contents)))
  (defn fullscreen-version [id version]
    (when-let [contents (:contents (paste/get-version id version))]
      (fullscreen contents))))

(defragment show-paste-page-fragment (resource "refheap/views/templates/pasted.html")
  [{:keys [lines private user contents language date fork views] :as paste} paste-user]
  [user-id (:id (session/get :user))
   forks (paste/count-forks paste)
   history (paste/count-history paste)
   current? (not (:version paste))]
  (l/id= :language) (l/content language)
  (l/id= :lines) (l/content (pluralize lines "line"))
  (l/id= :views) (if current? 
                   (l/content (pluralize views "view"))
                   (l/remove))
  (l/id= :forks) (if (and current? (pos? forks))
                   (l/content (l/node :a :attrs {:href (paste-url paste "/forks")}
                                      :content (pluralize forks "fork")))
                   (l/remove))
  (l/id= :edits) (if (pos? history)
                   (l/content (l/node :a :attrs {:href (paste-url paste "/history")}
                                      :content (pluralize history "edit")))
                   (l/remove))
  (when-not private
    [(l/class= :private) (l/remove)])
  (l/id= :last) (l/content [(if fork "Forked by " "Pasted by ")
                            (if user
                              (l/node :a :attrs {:href (str "/users/" paste-user)} :content paste-user)
                              paste-user)
                            (when fork
                              (l/unescaped
                               (str " from "
                                    (if-let [paste (:paste-id (paste/get-paste-by-id fork))]
                                      (str "<a href=\"/" paste "\">" paste "</a>")
                                      "[deleted]"))))
                            " on "
                            (date-string date)])
  (l/id= :embed) (if current?
                   (l/attr :href (paste-url paste "/embed"))
                   (l/remove))
  (l/id= :raw) (l/attr :href (paste-url paste "/raw"))
  (l/id= :fullscreen) (l/attr :href (paste-url paste "/fullscreen"))
  (if (and current? (paste/same-user? (and user-id {:id user-id}) paste))
    [(l/id= :owner) #(l/fragment (l/zip (:content %))
                                 (l/id= "editb") (l/attr :href (paste-url paste "/edit"))
                                 (l/id= "delete") (l/attr :href (paste-url paste "/delete")))]
    [(l/id= :owner) (l/remove)])
  (if (and user-id (not= user user-id))
    [(l/id= :fork) (l/attr :href (paste-url paste "/fork"))]
    [(l/id= :fork) (l/remove)])
  (l/id= :paste) (l/content (l/unescaped contents)))

(defn show-paste-page [id]
  (when-let [paste (paste/view-paste id)]
    (let [paste-user (paste-username paste)]
      (layout
        (show-paste-page-fragment paste paste-user)
        (str paste-user "'s paste: " id)
        show-head))))

(defn show-version-page [id version]
  (when-let [paste (paste/get-version id version)]
    (layout
      (show-paste-page-fragment paste (paste-username paste))
      (str "Version " version " of paste: " id)
      show-head)))

(defn paste-preview [node paste header]
  (let [{:keys [lines summary date user private]} paste]
    (l/at node
          (l/class= :more) (l/insert :left (l/unescaped summary))
          (if (> lines 5)
            [(l/class= :more) (l/attr :href (paste-url paste))]
            [(l/class= :more) (l/remove)])
          (l/class= :syntax) (l/insert :left header))))

(defragment render-paste-previews (resource "refheap/views/templates/preview.html")
  [pastes header-fn]
  (l/class= :preview-header) #(for [paste pastes]
                                 (paste-preview % paste (header-fn paste))))

(defragment embed-page-fragment (resource "refheap/views/templates/embed.html")
  [id host scheme]
  (l/id= :script) (l/content (str "<script src=\"" (name scheme) "://" host "/" id ".js\"></script>")))

(defn embed-page [paste host scheme]
  (let [id (:paste-id paste)]
    (layout (embed-page-fragment id host scheme)
            (str "Embedding paste " id))))

(defn embed-paste [id host scheme lines?]
  (when-let [paste (paste/get-paste id)]
    (content-type
      "text/javascript"
      (stencil/render-file
        "refheap/views/templates/embedjs"
        {:id id
         :content (escape-string (:contents paste))
         :url (str (name scheme) "://" host "/css/embed.css")
         :nolinenos (and lines? (not (to-booleany lines?)) {})}))))

(defragment paste-header (resource "refheap/views/templates/allheader.html")
  [paste]
  [{:keys [paste-id date user]} paste]
  (l/id= :id) (comp (l/attr :href (paste-url paste))
                     (l/content (str "Paste " paste-id)))
  (l/class= :right) (l/content [(if-let [user (and user (:username (users/get-user-by-id user)))]
                                  (l/node :a :attrs {:href (str "/users/" user)} :content user)
                                   "anonymous")
                                 " on "
                                 (date-string date)]))

(defn list-page [title url redirect-url list-count get-fn header-fn page]
  (if (> page (paste/count-pages list-count 20))
    (redirect redirect-url)
    (layout
      (l/node :div :attrs {:class "clearfix"}
              :content (concat (render-paste-previews (get-fn page) header-fn)
                               (page-buttons url list-count 20 page)))
      title
      show-head)))

(defn all-pastes-page [page]
  (list-page "All pastes"
             "/pastes"
             "/paste"
             (paste/count-pastes false)
             paste/get-pastes
             paste-header
             page))

(defn forks-page [id page]
  (when-let [paste (paste/get-paste id)]
    (list-page (str "Forks of paste: " id)
               (paste-url paste "/forks")
               (paste-url paste)
               (paste/count-forks paste)
               (partial paste/get-forks paste)
               paste-header
               page)))

(defragment version-header (resource "refheap/views/templates/allheader.html")
  [paste]
  [{:keys [version date]} paste]
  (l/id= :id) (comp (l/attr :href (paste-url paste))
                    (l/content (str "Version " version)))
  (l/class= :right) (l/content (date-string date)))

(defn history-page [id page]
  (when-let [paste (paste/get-paste id)]
    (list-page (str "History of paste: " id)
               (paste-url paste "/history")
               (paste-url paste)
               (paste/count-history paste)
               (partial paste/get-history paste)
               version-header
               page)))

(defn fail [error]
  (layout (l/node :p :attrs {:class "error"} :content error) "You broke it."))

(defn edit-paste-page [id]
  (let [paste (paste/get-paste id)]
    (when (paste/same-user? (session/get :user) paste)
      (paste-page nil paste))))

(defn fork-paste-page [id & [version]]
  (let [user (:id (session/get :user))
        paste (if version
                (paste/get-version id version)
                (paste/get-paste id))]
    (when (and user paste (not= (:user paste) user))
      (let [forked (paste/paste (:language paste)
                                (:raw-contents paste)
                                (:private paste)
                                (session/get :user)
                                (:id paste))]
        (redirect (paste-url forked))))))

(defn delete-paste-page [id]
  (if-let [user (:user (paste/get-paste id))]
    (when (= user (session/get-in [:user :id]))
      (paste/delete-paste id)
      (redirect (str "/users/" (:username (session/get :user)))))
    (when (some #{id} (session/get :anon-pastes))
      (paste/delete-paste id)
      (redirect "/pastes"))))

(defn edit-paste [{:keys [id paste language private]}]
  (let [paste (paste/update-paste
               (paste/get-paste id)
               language
               paste
               private
               (session/get :user))]
    (if (map? paste)
      (redirect (paste-url paste))
      (fail paste))))

(defn create-paste [{:keys [paste language private]}]
  (let [paste (paste/paste language paste private (session/get :user))]
    (if (map? paste)
      (redirect (paste-url paste))
      (fail paste))))

(defroutes paste-routes
  (GET "/" [lang]
    (paste-page lang))

  (GET "/pastes" [page]
    (all-pastes-page (paste/proper-page (safe-parse-long page 1))))

  (GET "/:id/forks" [id page]
    (forks-page id (paste/proper-page (safe-parse-long page 1))))

  (GET "/:id/history" [id page]
    (history-page id (paste/proper-page (safe-parse-long page 1))))

  (GET "/:id/fullscreen" [id]
    (fullscreen-paste id))

  (GET "/:id/edit" [id]
    (edit-paste-page id))

  (GET "/:id/fork" [id]
    (fork-paste-page id))

  (GET "/:id/delete" [id]
    (delete-paste-page id))

  (GET "/:id/raw" [id]
    (when-let [content (:raw-contents (paste/get-paste id))]
      (content-type "text/plain; charset=utf-8" content)))

  (GET "/:id/embed" {{:keys [id]} :params
                     {host "host"} :headers
                     scheme :scheme}
    (let [paste (paste/get-paste id)]
      (embed-page paste host scheme)))

  (GET "/:id.js" {{:keys [id linenumbers]} :params
                  {host "host"} :headers
                  scheme :scheme}
    (embed-paste id host scheme linenumbers))

  (GET "/:id" [id]
    (show-paste-page id))

  (GET "/:id/history/:version" [id version]
    (show-version-page id (safe-parse-long version)))

  (GET "/:id/history/:version/fullscreen" [id version]
    (fullscreen-version id (safe-parse-long version)))

  (GET "/:id/history/:version/raw" [id version]
    (when-let [content (:raw-contents (paste/get-version id (safe-parse-long version)))]
      (content-type "text/plain; charset=utf-8" content)))

  (GET "/:id/history/:version/fork" [id version]
    (fork-paste-page id (safe-parse-long version)))

  (POST "/:id/edit" {:keys [params]}
    (edit-paste params))

  (POST "/create" {:keys [params]}
    (create-paste params))

  ; Redirect legacy /paste/ prefixed URLs

  (GET ["/paste/:uri", :uri #".*"] {{:keys [uri]} :params
                                    query-string :query-string}
    (redirect (apply str "/" uri (when query-string ["?" query-string]))))

  (GET "/paste" {:keys [query-string]}
    (redirect (if query-string (str "/?" query-string) "/"))))
