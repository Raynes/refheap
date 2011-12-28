(ns refheap.views.about
  (:use [noir.core :only [defpage defpartial]]
        [refheap.views.common :only [layout]]
        [hiccup.page-helpers :only [link-to]]))

(defn about-page []
  (layout
   [:div.written
    [:p
     "The Refusal Heap (a synonym for 'dump') is a pastebin written in Clojure, leveraging"
     " awesome technologies such as the "
     (link-to "http://webnoir.org" "Noir")
     " web framework, "
     (link-to "http://mongodb.org" "MongoDB")
     ", and the excellent "
     (link-to "http://pygments.org/" "Pygments")
     ", syntax highlighting tool. Because of that, I was able to develop a pastebin that"
     " supports a remarkable number of languages with no effort at all. In addition, it"
     " also supports highlighting non-code things such irb sessions and IRC logs."]
    [:p
     "This site is totally open source and entirely non-profit. It is mostly a personal"
     " experiment in designing and implementing a non-trivial website in order to gain"
     " experience with web development and design. That said, I do want it to be useful"
     " and not-ugly. The goal is to have an excellent pastebin that the Clojure community"
     " can proudly call its own."]
    [:p
     "If you'd like to take a look at the code and/or contribute, fork it "
     (link-to "https://github.com/Raynes/refheap" "on Github!")]]))

(defpage "/about" [] (about-page))