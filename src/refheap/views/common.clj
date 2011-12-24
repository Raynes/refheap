(ns refheap.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "refheap"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))
