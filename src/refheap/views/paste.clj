(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout]]
        [noir.response :only [redirect]])
  (:require [refheap.models.paste :as paste]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

(defpartial create-paste-page []
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
     [:br])]))

(defpage "/paste" []
  (binding [session/*noir-session* (atom {:user 1})]
    (create-paste-page)))

(defpage [:post "/paste/create"] {:keys [paste language private]}
  (redirect
   (str "/paste/"
        (:paste-id
         (paste/paste language paste private)))))

(defpage "/paste/:id" {:keys [id]}
  (layout
   [:div.syntax
    (:contents (paste/get-paste (Long. id)))]))