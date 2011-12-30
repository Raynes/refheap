(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout avatar page-buttons]]
        [noir.response :only [redirect]]
        [refheap.dates :only [date-string]])
  (:require [refheap.models.paste :as paste]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

(defn create-paste-page []
  (layout
   [:div#pastearea
    (fh/form-to
     [:post "/paste/create"]
     (fh/drop-down "language"
                   (sort #(.compareToIgnoreCase % %2)
                         (keys paste/lexers))
                   "Clojure")
     (fh/text-area :paste)
     [:div#submit
      (when (session/get :user)
        (list (fh/label :private "Private")
              (fh/check-box :private)))
      (fh/submit-button "Paste!")]
     [:div.clear])]))

(defn show-paste-page [id]
  (when-let [{:keys [lines private user contents language date]} (paste/get-paste (Long. id))]
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
         [:div#edit
          [:a {:href (str "/paste/" id "/edit")} "edit"]]]]
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

(defn too-big []
  (layout
   [:p.centered "That paste was too big. Has to be less than 64KB"]))

(defpage "/paste" []
  (create-paste-page))

(defpage [:post "/paste/create"] {:keys [paste language private]}
  (if-let [paste (paste/paste language paste private)]
    (redirect (str "/paste/" (:paste-id paste)))
    (too-big)))

(defpage "/paste/:id" {:keys [id]}
  (show-paste-page id))

(defpage "/pastes" {:keys [page]}
  (all-pastes-page (Long. (or page "1"))))