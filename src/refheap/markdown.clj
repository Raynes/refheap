(ns refheap.markdown
  (:require [me.raynes.laser :as laser])
  (:import (org.pegdown PegDownProcessor Extensions)))

(defn make-pegdown
  "Create a PegDownProcessor instance with the hardwraps and
   fenced code blocks extensions"
  []
  (PegDownProcessor.
   (reduce bit-or 0 [Extensions/HARDWRAPS
                     Extensions/FENCED_CODE_BLOCKS])))

(defn wrap-code [html]
  (laser/fragment-to-html
   (laser/fragment
    (laser/parse-fragment html)
    (laser/element= :pre) (laser/add-class "md-code"))))

(defn to-html
  "Convert markdown to html."
  [s]
  (wrap-code (.markdownToHtml (make-pegdown) s)))
