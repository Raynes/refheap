(ns refheap.markdown
  (:require [me.raynes.laser :as laser]
            [me.raynes.cegdown :as md]))

(defn wrap-code [html]
  (str "<div class=\"markdown\">\n"
       (laser/fragment-to-html
        (laser/fragment
         (laser/parse-fragment html)
         (laser/element= :pre) (laser/add-class "md-code")))
       "</div>"))

(defn to-html
  "Convert markdown to html."
  [s]
  (wrap-code (md/to-html s [:fenced-code-blocks :tables])))
