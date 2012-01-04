(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons]]
        [noir.response :only [redirect]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [refheap.models.users :as users]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

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
         (list (fh/check-box :private (:private old))
               (fh/label :private "Private")))
       (fh/submit-button (if old "Edit!" "Paste!"))]
      (fh/text-area :paste (:raw-contents old)))]
    [:div#main-right
     (ph/unordered-list ["Throw some text in that big white box over on the left" "Select what language you want to use for syntax highlighting" "Hit 'Paste!'" "Share, edit, refine; enjoy."])
     [:p "Protip: if you get tired of selecting your language every time you drop in, you can specify it in the URL and bookmark the link. Example: \""
      (ph/link-to "http://refheap.com/paste?lang=Ruby" "http://refheap.com/paste?lang=Ruby") "\""]
     [:p "Send feedback, feature requests, and bug reports "
      (ph/link-to "http://github.com/raynes/refheap/issues" "here") "."]]]
   [:div.clear]))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date]} (paste/get-paste id)]
    (layout
     (list
      [:div.floater
       [:div#pasteinfo
        [:span.info language]
        [:span.info "Lines: " lines]
        (when private
          [:span {:class "info private"} "Private"])
        [:span#last.info "Pasted by "
         (if user
           (let [user (:username (users/get-user-by-id user))]
             (ph/link-to (str "/users/" user) user))
           "anonymous")
         " on "
         (date-string date)
         (when (and user (= user (:id (session/get :user))))
           [:div#edit
            [:a {:href (str "/paste/" id "/edit")} "edit"]
            [:a#delete.evil {:href (str "/paste/" id "/delete")} "delete"]])]]
       [:div#paste.syntax
        contents]]
      [:div.clear]))))

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
  
(defn pastes [ps]
  [:div#preview-container
   (for [paste ps]
     (render-paste-preview paste))])

(defn all-pastes-page [page]
  (let [paste-count (paste/count-pastes)]
    (if (> page (paste/count-pages paste-count 20))
      (redirect "/paste")
      (layout
       [:div
        (pastes (paste/get-pastes page))
        (page-buttons "/pastes" paste-count 20 page)]))))

(defn fail [error]
  (layout
   [:p.error error]))

(defpage "/paste" {:keys [lang]}
  (create-paste-page lang))

(defpage "/paste/:id/edit" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (when (= (:user paste) (:id (session/get :user)))
      (create-paste-page nil paste))))

(defpage "/paste/:id/delete" {:keys [id]}
  (when-let [user (:user (paste/get-paste id))]
    (when (= user (:id (session/get :user)))
      (paste/delete-paste id)
      (redirect "/paste"))))

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