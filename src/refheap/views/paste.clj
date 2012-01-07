(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons header]]
        [noir.response :only [redirect content-type]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))


(defn private-checkbox [old]
  (list (fh/check-box :private (:private old))
               (fh/label :private "Private")))

(defn create-paste-page [lang & [old]]
  (layout
   [:div#main-container
    [:div#paste-container
     (fh/form-to
      [:post (if old
               (str "/paste/" (:paste-id old) "/edit")
               "/paste/create")]
      [:div#paste-header
       (fh/drop-down "language"
                     (sort #(.compareToIgnoreCase % %2)
                           (keys paste/lexers))
                     (or lang (:language old "Clojure")))
       (when (session/get :user)
         (private-checkbox old))
       (fh/submit-button (if old "Edit!" "Paste!"))]
      (fh/text-area :paste (:raw-contents old)))]
    [:div#main-right
     (ph/unordered-list ["Throw some text in that big white box over on the left" "Select what language you want to use for syntax highlighting" "Hit 'Paste!'" "Share, edit, refine; enjoy."])
     [:p "Protip: if you get tired of selecting your language every time you drop in, you can specify it in the URL and bookmark the link. Example: \""
      (ph/link-to "http://refheap.com/paste?lang=Ruby" "http://refheap.com/paste?lang=Ruby") "\""]
     [:p "We have an API! Generate a token "
      (ph/link-to "/api" "here")
      " and find examples in our "
      (ph/link-to "https://github.com/Raynes/refheap/wiki/Documentation:-API" "documentation")
      ". Happy hacking!"]
     [:p "Send feedback, feature requests, and bug reports "
      (ph/link-to "http://github.com/raynes/refheap/issues" "here") "."]]]
   [:div.clear]))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date fork] :as all} (paste/get-paste id)]
    (layout
     (list
      [:div.floater
       [:div#pasteinfo
        [:span.info language]
        [:span.info "Lines: " lines]
        (when private
          [:span {:class "info private"} "Private"])
        [:span#last.info
         (if fork "Forked by " "Pasted by ")
         (if user
           (let [user (:username (users/get-user-by-id user))]
             (ph/link-to (str "/users/" user) user))
           "anonymous")
         (when fork
           (list
            " from "
            (if-let [paste (:paste-id (paste/get-paste-by-id fork))]
              (ph/link-to (str "/paste/" paste) paste)
              "[deleted]")))
         " on "
         (date-string date)
         [:div#edit
          (ph/link-to (str "/paste/" id "/embed") "embed")
          (ph/link-to (str "/paste/" id "/raw") "raw")
          (ph/link-to (str "/paste/" id "/fullscreen") "maximize")
          (when-not (= user (:id (session/get :user)))
            (ph/link-to (str "/paste/" id "/fork") "fork"))
          (when (and user (= user (:id (session/get :user))))
            (list
             [:a {:href (str "/paste/" id "/edit")} "edit"]
             [:a#delete.evil {:href (str "/paste/" id "/delete")} "delete"]))]]]
       [:div#paste.syntax
        contents]]
      [:div.clear]))))

(defn fullscreen-paste [id]
  (when-let [contents (:contents (paste/get-paste id))]
    (ph/html5
     (header)
     [:body#fullscreen
      [:div.syntax contents]])))

(defpage "/paste/:id/fullscreen" {:keys [id]}
  (fullscreen-paste id))

(defpage "/paste/:id/framed" {:keys [id]}
  (fullscreen-paste id))

(defn render-paste-preview [paste]
  (let [{:keys [paste-id lines summary date user]} paste]
    (list
     [:div.preview-header
      (if user
        (let [user (:username (users/get-user-by-id user))]
          (ph/link-to (str "/users/" user) user))
      "anonymous")
      " on "
      (date-string date)
      [:div.right
       "[" (ph/link-to (str "/paste/" paste-id) "Link") "]"]]
     [:div.syntax summary
      (if (> lines 5) [:div.more (ph/link-to (str "/paste/" paste-id) "more...")])]
     [:br])))

(defn render-embed-page [paste]
  (let [{:keys [paste-id content]} paste]
    (layout
     (list
      [:div.written
       [:p "Please copy the following html element onto your webpage, and change the inline size/styling as needed:"]
       [:p "&lt;iframe style=\"width: 648px; height: 400px; border: 0px;\" src=\"http://refheap.com/paste/" paste-id "/framed\" /&gt;"]]))))

(defn pastes [ps]
  [:div#preview-container
   (for [paste ps]
     (render-paste-preview paste))])

(defn all-pastes-page [page]
  (let [paste-count (paste/count-pastes false)]
    (if (> page (paste/count-pages paste-count 20))
      (redirect "/paste")
      (layout
       [:div
        (pastes (paste/get-pastes page))
        (page-buttons "/pastes" paste-count 20 page)
        [:div.clear]]))))

(defn fail [error]
  (layout
   [:p.error error]))

(defpage "/paste" {:keys [lang]}
  (create-paste-page lang))

(defpage "/paste/:id/edit" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (when (= (:user paste) (:id (session/get :user)))
      (create-paste-page nil paste))))

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