(ns refheap.views.paste
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [refheap.highlight :refer [lexers]]
            [refheap.utilities :refer [to-booleany escape-string]]
            [noir.session :as session]
            [noir.response :refer [content-type]]
            [stencil.core :as stencil]
            [me.raynes.laser :refer [defragment] :as l]
            [clojure.java.io :refer [resource]]
            [compojure.core :refer [defroutes GET POST]]
            [refheap.views.common :refer [layout avatar page-buttons static]]
            [noir.response :refer [redirect content-type]]
            [refheap.dates :refer [date-string]]
            [clojure.string :refer [split join]]))

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
                                       (str "/paste/" (:paste-id old) "/edit")
                                       "/paste/create"))
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
      html (l/parse (resource "refheap/views/templates/fullscreen.html"))]
  (defn fullscreen-paste [id]
    (when-let [contents (:contents (paste/get-paste id))]
      (l/document html
                  (l/element= :head) (l/content [head show-head])
                  (l/class= :syntax) (l/content (l/unescaped contents))))))

(defragment show-paste-page-fragment (resource "refheap/views/templates/pasted.html")
  [{:keys [lines private user contents language date fork]} id paste-user]
  [user-id (:id (session/get :user))]
  (l/id= :language) (l/content language)
  (l/element= :abbr) (comp #(update-in % [:attrs :title] str lines)
                           (l/content (str lines " L")))
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
                                      (str "<a href=\"/paste/" paste "\">" paste "</a>")
                                      "[deleted]"))))
                            " on "
                            (date-string date)])
  (l/id= :embed) (l/attr :href (str "/paste/" id "/embed"))
  (l/id= :raw) (l/attr :href (str "/paste/" id "/raw"))
  (l/id= :fullscreen) (l/attr :href (str "/paste/" id "/fullscreen"))
  (if (or (and user-id (= user user-id)) (some #{id} (session/get :anon-pastes)))
    [(l/id= :owner) #(l/fragment (l/zip (:content %))
                                 (l/id= "editb") (l/attr :href (str "/paste/" id "/edit"))
                                 (l/id= "delete") (l/attr :href (str "/paste/" id "/delete")))]
    [(l/id= :owner) (l/remove)])
  (if (and user-id (not= user user-id))
    [(l/id= :fork) (l/attr :href (str "/paste/" id "/fork"))]
    [(l/id= :fork) (l/remove)])
  (l/id= :paste) (l/content (l/unescaped contents)))

(defn show-paste-page [id]
  (when-let [paste (paste/get-paste id)]
    (let [paste-user (if-let [user (:user paste)]
                       (:username (users/get-user-by-id user))
                       "anonymous")]
      (layout
       (show-paste-page-fragment paste id paste-user)
       (str paste-user "'s paste: " id)
       show-head))))

(defn paste-preview [node paste header]
  (let [{:keys [paste-id lines summary date user private]} paste]
    (l/at node
          (l/class= :more) (l/insert :left (l/unescaped summary))
          (if (> lines 5)
            [(l/class= :more) (l/attr :href (str "/paste/" paste-id))]
            [(l/class= :more) (l/remove)])
          (l/class= :syntax) (l/insert :left header))))

(defragment render-paste-previews (resource "refheap/views/templates/preview.html")
  [pastes header-fn]
  (l/class= :preview-header) #(for [paste pastes]
                                 (paste-preview % paste (header-fn paste))))

(defragment embed-page-fragment (resource "refheap/views/templates/embed.html")
  [id host scheme]
  (l/id= :script) (l/content (str "<script src=\"" (name scheme) "://" host "/paste/" id ".js\"></script>")))

(defn embed-page [paste host scheme]
  (let [id (:paste-id paste)]
    (layout (embed-page-fragment id host scheme)
            (str "Embedding paste " id))))

(defn embed-paste [id host scheme lines?]
  (content-type
   "text/javascript"
   (stencil/render-file
    "refheap/views/templates/embedjs"
    {:id id
     :content (escape-string (:contents (paste/get-paste id)))
     :url (str (name scheme) "://" host "/css/embed.css")
     :nolinenos (and lines? (not (to-booleany lines?)))})))

(defragment paste-header (resource "refheap/views/templates/allheader.html")
  [paste]
  [{:keys [paste-id date user]} paste]
  (l/id= :id) (comp (l/attr :href (str "/paste/" paste-id))
                     (l/content (str "Paste " paste-id)))
  (l/class= :right) (l/content [(if-let [user (and user (:username (users/get-user-by-id user)))]
                                  (l/node :a :attrs {:href (str "/users/" user)} :content user)
                                   "anonymous")
                                 " on "
                                 (date-string date)]))

(defn all-pastes-page [page]
  (let [paste-count (paste/count-pastes false)]
    (if (> page (paste/count-pages paste-count 20))
      (redirect "/paste")
      (layout
       (l/node :div :attrs {:class "clearfix"}
               :content (concat (render-paste-previews (paste/get-pastes page) paste-header)
                                (page-buttons "/pastes" paste-count 20 page)))
       "All pastes"
       show-head))))

(defn fail [error]
  (layout (l/node :p :attrs {:class "error"} :content error) "You broke it."))

(defn edit-paste-page [{:keys [id]}]
  (let [user (session/get-in [:user :id])
        anon-pastes (session/get :anon-pastes)
        paste (paste/get-paste id)]
    (when (or (and user (= (:user paste) user)) (some #{id} anon-pastes))
      (paste-page nil paste))))

(defn fork-paste-page [{:keys [id]}]
  (let [user (:id (session/get :user))
        paste (paste/get-paste id)]
    (when (and user paste (not= (:user paste) user))
      (let [forked (paste/paste (:language paste)
                                (:raw-contents paste)
                                (:private paste)
                                (session/get :user)
                                (:id paste))]
        (redirect (str "/paste/" (:paste-id forked)))))))

(defn delete-paste-page [{:keys [id]}]
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
      (redirect (str "/paste/" (:paste-id paste)))
      (fail paste))))

(defn create-paste [{:keys [paste language private]}]
  (let [paste (paste/paste language paste private (session/get :user))]
    (if (map? paste)
      (redirect (str "/paste/" (:paste-id paste)))
      (fail paste))))

(defroutes paste-routes
  (GET "/paste/:id/fullscreen" {{:keys [id]} :params}
    (fullscreen-paste id))

  (GET "/paste/:id/framed" {{:keys [id]} :params}
    (fullscreen-paste id))

  (GET "/paste" {{:keys [lang]} :params}
    (paste-page lang))

  (GET "/paste/:id/edit" {:keys [params]}
    (edit-paste-page params))

  (GET "/paste/:id/fork" {:keys [params]}
    (fork-paste-page params))

  (GET "/paste/:id/delete" {:keys [params]}
    (delete-paste-page params))

  (GET "/paste/:id/raw" {{:keys [id]} :params}
    (when-let [content (:raw-contents (paste/get-paste id))]
      (content-type "text/plain; charset=utf-8" content)))

  (GET "/paste/:id/embed" {{:keys [id]} :params
                           {host "host"} :headers
                           scheme :scheme}
    (let [paste (paste/get-paste id)]
      (embed-page paste host scheme)))

  (POST "/paste/:id/edit" {:keys [params]}
    (edit-paste params))

  (POST "/paste/create" {:keys [params]}
    (create-paste params))

  (GET "/paste/:id" {{:keys [id linenumbers]} :params
                     {host "host"} :headers
                     scheme :scheme}
    (let [[id ext] (split id #"\.")]
      (if ext
        (embed-paste id host scheme linenumbers)
        (show-paste-page id))))

  (GET "/pastes" [page]
    (all-pastes-page (paste/proper-page (Long. (or page "1"))))))

