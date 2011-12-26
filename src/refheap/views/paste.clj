(ns refheap.views.paste
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout]])
  (:require [refheap.models.paste :as paste]
            [noir.session :as session]
            [hiccup.form-helpers :as fh]
            [hiccup.page-helpers :as ph]))

(defpartial create-paste-page []
  (layout
   [:div#pastearea
    (fh/form-to
     [:post "/pastes/create"]
     (fh/drop-down "language" (keys paste/lexers) "Clojure")
     (fh/text-area "paste")
     (when (session/get :user)
       (fh/label "Private? ")
       (fh/check-box "private"))
     (fh/submit-button "Paste!"))]))

(defpage "/paste" []
  (create-paste-page))