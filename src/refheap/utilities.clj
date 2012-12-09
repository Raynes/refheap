(ns refheap.utilities
  (:require [refheap.models.paste :as paste]
            [clojure.string :refer [lower-case join]]))

#_(defn regenerate
  "Regenerates a paste's pygmentized text and preview text from
  its raw-contents. This is useful for when a lexer fails (because
  you typoed it in the language list) and you need to regenerate
  after you've fixed it."
  [id]
  (let [paste (paste/get-paste id)
        contents (:raw-contents paste)
        lexer (-> paste
                  :language
                  paste/lookup-lexer
                  second
                  :short)]
    (mongo/update!
      :pastes
      paste
      (assoc paste
             :contents (paste/pygmentize lexer contents)
             :summary (paste/pygmentize lexer (paste/preview contents))))))

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

(defn escape-string
  "Escapes all escape sequences in a string to make it suitable
   for passing to another programming language. Kind of like what
   pr-str does for strings but without the wrapper quotes."
  [s]
  (join (map #(char-escape-string % %) s)))