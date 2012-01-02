(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons]]
        [noir.response :only [redirect]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

(defn create-paste-page [lang & [old]]
  (layout
   [:div#pastearea
    (fh/form-to
     [:post (if old
              (str "/paste/" (:paste-id old) "/edit")
              "/paste/create")]
     (fh/drop-down "language"
                   (sort #(.compareToIgnoreCase % %2)
                         (keys paste/lexers))
                   (or lang (:language old "Clojure")))
     [:div#submit
      (when (session/get :user)
        (list (fh/check-box :private (:private old))
              (fh/label :private "Private")))
      (fh/submit-button (if old "Edit!" "Paste!"))]
     [:div#main-container
      (fh/text-area :paste (:raw-contents old))
      [:div#main-right "&nbsp;Instructions and/or kittens."]]
     [:div.clear])]))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date]} (paste/get-paste id)]
    (layout
     (list
      [:div.floater
       [:div#pasteinfo
        [:span.info language]
        [:span.info "Lines: " lines]
        [:span.info "Private: " private]
        [:span#last.info "Pasted by "
         (if (= user "anonymous")
           user
           (ph/link-to (str "/users/" user) user))
         " on "
         (date-string date)
         (when (= user (:username (session/get :user)))
           [:div#edit
            [:a.nice {:href (str "/paste/" id "/edit")} "edit"]
            [:a#delete.evil {:href (str "/paste/" id "/delete")} "delete"]])]]
       [:div#paste.syntax
        contents]]
      [:div.clear]))))

(defn pastes [ps]
  (for [{:keys [paste-id summary date user]} ps]
    (list
     [:span.header
      (ph/link-to (str "/paste/" paste-id) paste-id)
      " pasted by "
      (if (= user "anonymous")
        user
        (ph/link-to (str "/users/" user) user))
      " on "
      (date-string date)]
     [:div.syntax summary]
     [:br])))

(defn all-pastes-page [page]
  (layout
   [:div
    (pastes (paste/get-pastes page))
    (page-buttons (paste/count-pastes page) page)]))

(defn fail []
  (layout
   [:p.centered (session/flash-get :error)]))

(defpage "/paste" {:keys [lang]}
  (create-paste-page lang))

(defpage "/paste/:id/edit" {:keys [id]}
  (let [paste (paste/get-paste id)]
    (when (= (:user paste) (:username (session/get :user)))
      (create-paste-page nil paste))))

(defpage "/paste/:id/delete" {:keys [id]}
  (when (= (:user (paste/get-paste id))
           (:username (session/get :user)))
    (paste/delete-paste id)
    (redirect "/paste")))

(defpage [:post "/paste/:id/edit"] {:keys [id paste language private]}
  (if-let [paste (paste/update-paste
                  (paste/get-paste id)
                  language
                  paste
                  private)]
    (redirect (str "/paste/" (:paste-id paste)))
    (fail)))

(defpage [:post "/paste/create"] {:keys [paste language private]}
  (if-let [paste (paste/paste language paste private)]
    (redirect (str "/paste/" (:paste-id paste)))
    (fail)))

(defpage "/paste/:id" {:keys [id]}
  (show-paste-page id))

(defpage "/pastes" {:keys [page]}
  (all-pastes-page (Long. (or page "1"))))