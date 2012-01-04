(ns refheap.views.about
  (:use [noir.core            :only [defpage]]
        [refheap.views.common :only [layout]]
        [hiccup.page-helpers  :only [link-to]]))

(defn about-page []
  (layout
   [:div.written
    [:p
     "The Reference Heap is a pastebin written in Clojure, leveraging awesome technologies"
     " such as the "
     (link-to "http://webnoir.org" "Noir")
     " web framework, "
     (link-to "http://mongodb.org" "MongoDB")
     ", and the excellent "
     (link-to "http://pygments.org/" "Pygments")
     " syntax highlighting tool. Because of that,  we were able to develop a pastebin that"
     " supports a remarkable number of languages with very little effort. In addition, it"
     " also supports highlighting non-code things such irb sessions and IRC logs."]
    [:p
     "This site is written and designed by Anthony Grimes and Alex McNamara. "
     "If you'd like to take a look at the code and/or contribute, fork it "
     (link-to "https://github.com/Raynes/refheap" "on Github!")]]))

(defpage "/about" [] (about-page))