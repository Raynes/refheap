(ns refheap.utilities
  (:require [refheap.models.paste :as paste]
            [clojure.string :refer [lower-case join]]))

(defn to-booleany
  "Convert numbers and various representations of 'true' and 'false'
   to their actual true or false counterparts."
  [s]
  (when s
    (let [s (lower-case s)]
      (case s
        "0" false
        "1" true
        "true" true
        "false" false
        false))))

(def js-char-replacements
  (merge char-escape-string
         {\' "\\'"}))

(defn escape-string
  "Escapes all escape sequences in a string to make it suitable
   for passing to another programming language. Kind of like what
   pr-str does for strings but without the wrapper quotes."
  [s]
  (join (map #(js-char-replacements % %) s)))

(defn nil-comp
  "Composes functions together just like `comp`, but stops and returns nil
   if any of the functions in the composition return nil."
  [& fns]
  (let [fns (reverse fns)]
    (fn [& args]
      (let [[start & tail] fns]
        (reduce
         (fn [acc f]
           (if (nil? acc)
             (reduced nil)
             (f acc)))
         (apply start args)
         tail)))))

(defn pluralize [n s]
  (str n " " s (when-not (= n 1) "s")))
