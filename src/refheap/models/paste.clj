(ns refheap.models.paste
  (:refer-clojure :exclude [sort find])
  (:require [noir.session :as session]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as format]
            [conch.core :as sh]
            [refheap.dates :refer [parse-string]]
            [refheap.messages :refer [error]]
            [monger.collection :as mc]
            [monger.query :refer [with-collection find sort limit skip]])
  (:import java.io.StringReader
           org.bson.types.ObjectId))

(def paste-id
  "The current highest paste-id."
  (atom
   (-> (with-collection "pastes"
         (find {})
         (sort {:id -1})
         (limit 1))
       first
       :id
       (or 0))))

(def lexers
  "A map of language names to pygments lexer names."
  {"Clojure" {:short "clojure"
              :exts #{"clj" "cljs"}}
   "Factor" {:short "factor"
             :exts #{"factor"}}
   "Fancy" {:short "fancy"
            :exts #{"fy"}}
   "Groovy" {:short "groovy"
             :exts #{"groovy"}}
   "Io" {:short "io"
         :exts #{"io"}}
   "Ioke" {:short "ioke"
           :exts #{"ioke"}}
   "Lua" {:short "lua"
          :exts #{"lua"}}
   "Perl" {:short "perl"
           :exts #{".pl"}}
   "Python Console" {:short "pycon"}
   "Python" {:short "python"
             :exts #{".py"}}
   "Python Traceback" {:short "pytb"}
   "Ruby Console" {:short "irb"}
   "Ruby" {:short "ruby"
           :exts #{"rb"}}
   "Mirah" {:short "ruby"
            :exts #{"mirah"}}
   "Tcl" {:short "tcl"
          :exts #{"mirah"}}
   "C Object Dump" {:short "c-objdump"}
   "C++ Object Dump" {:short "cpp-objdump"}
   "D Object Dump" {:short "d-objdump"}
   "Gas" {:short "gas"}
   "LLVM" {:short "llvm"}
   "NASM" {:short "nasm"}
   "Object Dump" {:short "objdump"}
   "Ada" {:short "ada"
          :exts #{"ada"}}
   "BlitzMax" {:short "bmax"}
   "C" {:short "c"
        :exts #{"c" "h"}}
   "C++" {:short "cpp"
          :exts #{"cpp"}}
   "Cython" {:short "pyx"
             :exts #{"pyx"}}
   "D" {:short "d"
        :exts #{"d"}}
   "Delphi" {:short "delphi"} ;; What the fuck *is* the right extension for this?
   "Dylan" {:short "dylan"
            :exts #{"dylan"}}
   "Felix" {:short "flx"
            :exts #{"flx"}}
   "Fortran" {:short "fortran"
              :exts #{"fortran"}}
   "GLSL" {:short "glsl"}
   "Go" {:short "go"
         :exts #{"go"}}
   "Java" {:short "java"
           :exts #{"java"}}
   "Modula-2" {:short "m2"
               :exts #{"def" "mod"}}
   "Nimrod" {:short "nim"
             :exts #{"nim" "nimrod"}}
   "Objective-C" {:short "objective-c"
                  :exts #{"m"}}
   "ooc" {:short "ooc"
          :exts #{"ooc"}}
   "Prolog" {:short "prolog"
             :exts #{"pro"}}
   "Scala" {:short "scala"
            :exts #{"scala"}}
   "Vala" {:short "vala"
           :exts #{"vala"}}
   "Boo" {:short "boo"
          :exts #{"boo"}}
   "C#" {:short "csharp"
         :exts #{"cs"}}
   "F#" {:short "fsharp"
         :exts #{"fs"}}
   "Nemerle" {:short "nemerle"
              :exts #{"n"}}
   "VB.NET" {:short "vbnet"
             :exts #{"vb"}}
   "Common Lisp" {:short "cl"
                  :exts #{"lisp"}}
   "Erlang" {:short "erlang"
             :exts #{"erl"}}
   "Erlang Shell" {:short "erl"}
   "Haskell" {:short "hs"
              :exts #{"hs"}}
   "Literate Haskell" {:short "lhs"
                       :exts #{"lhs"}}
   "OCaml" {:short "ocaml"
            :exts #{"ml"}}
   "Scheme" {:short "scm"
             :exts #{"scm" "ss"}}
   "Emacs Lisp" {:short "scm"
                 :exts #{"el"}}
   "Verilog" {:short "v"
              :exts #{"v"}}
   "Matlab" {:short "matlab"}
   "MuPAD" {:short "mupad"}
   "NumPy" {:short "numpy"}
   "R Console" {:short "rout"}
   "R" {:short "r"
        :exts #{"r"}}
   "AppleScript" {:short "applescript"
                  :exts #{"applescript"}}
   "Autohotkey" {:short "ahk"
                 :exts #{"ahk"}}
   "Awk" {:short "awk"
          :exts #{"awk"}}
   "Bash" {:short "sh"
           :exts #{"sh"}}
   "Bash Session" {:short "console"}
   "Batch" {:short "bat"
            :exts #{"bat"}}
   "Befunge" {:short "befunge"
              :exts #{"befunge"}}
   "Brainfuck" {:short "bf"
                :exts #{"bf"}}
   "Cucumber" {:short "cucumber"}
   "MOOCode" {:short "moocode"}
   "MySQL" {:short "mysql"}
   "NewSpeak" {:short "newspeak"}
   "PostScript" {:short "postscript"
                 :exts #{"ps"}}
   "Protobuf" {:short "protobuf"
               :exts #{"proto"}}
   "REBOL" {:short "rebol"
            :exts #{"rebol"}}
   "Redcode" {:short "redcode"}
   "Smalltalk" {:short "squeak"
                :exts #{"st"}}
   "SQL" {:short "sql"
          :exts #{"sql"}}
   "TCSH" {:short "csh"}
   "Apache Configuration" {:short "apacheconf"}
   "BBCode" {:short "bbcode"}
   "CMake" {:short "cmake"}
   "Darcs Patch" {:short "dpatch"}
   "Diff" {:short "diff"
           :exts #{"diff"}}
   "INI" {:short "ini"
          :exts #{"ini"}}
   "IRC Logs" {:short "irc"}
   "Lighttpd" {:short "lighty"}
   "Makefile" {:short "make"}
   "Nginx Configuration" {:short "nginx"}
   "Java Properties" {:short "properties"
                      :exts #{"properties"}}
   "rST" {:short "rst"
          :exts #{"rst"}}
   "LaTeX" {:short "tex"
            :exts #{"tex"}}
   "VimL" {:short "vim"
           :exts #{"vim"}}
   "YAML" {:short "yaml"
           :exts #{"yaml"}}
   "ActionScript" {:short "as"
                   :exts #{"as"}}
   "CoffeeScript" {:short "coffeescript"
                   :exts #{"coffeescript"}}
   "CSS" {:short "css"
          :exts #{"css"}}
   "DTD" {:short "dtd"}
   "HAML" {:short "haml"
           :exts #{"haml"}}
   "haXe" {:short "hx"
           :exts #{"hx"}}
   "HTML" {:short "html"
           :exts #{"html"}}
   "Javascript" {:short "js"
                 :exts #{"js"}}
   "PHP" {:short "php"
          :exts #{"php"}}
   "Plain Text" {:short "text"
                 :ext #{"txt"}}
   "SASS" {:short "sass"
           :exts #{"scss" "sass"}}
   "Scaml" {:short "scaml"
            :exts #{"scaml"}}
   "XML" {:short "xml"
          :exts #{"xml"}}
   "Kotlin" {:short "kotlin"
             :exts #{"kt"}}
   "Elixir" {:short "ex"
             :exts #{"ex" "exs"}}
   "Elixir Console" {:short "iex"}})

(defn lookup-lexer
  "Selects a language."
  [lang]
  (or
   (if (and lang (.startsWith lang "."))
     (first
      (filter (fn [[_ v]]
                (when-let [exts (:exts v)]
                  (exts (string/join (rest lang)))))
              lexers))
     (when-let [lang-map (lexers lang)]
       [lang lang-map]))
   ["Plain Text" {:short "text"}]))

(defn pygmentize
  "Syntax highlight some code."
  [language text & [anchor?]]
  (let [proc (sh/proc "./pygmentize" "-fhtml" (str "-l" language)
                      (str "-Olinenos=table,stripnl=False,encoding=utf-8"
                           (when anchor? ",anchorlinenos=true,lineanchors=L"))
                      :dir "resources/pygments")]
    (sh/feed-from-string proc text)
    (sh/done proc)
    (sh/stream-to-string proc :out)))

(defn preview
  "Get the first 5 lines of a string."
  [s]
  (->> s StringReader. io/reader line-seq (take 5) (string/join "\n")))

(defn paste-map [paste-id id user language contents date private fork]
  (let [[name {:keys [short]}] (lookup-lexer language)]
    {:paste-id (str paste-id)
     :id id
     :_id (ObjectId.)
     :user (:id user)
     :language name
     :raw-contents contents
     :summary (pygmentize short (preview contents))
     :private (boolean private)
     :date date
     :lines (let [lines (count (filter #{\newline} contents))]
              (if (= \newline (last contents))
                lines
                (inc lines)))
     :contents (pygmentize short contents true)
     :fork fork}))

(defn validate [contents]
  (cond
    (>= (count contents) 64000) {:error "That paste was too big. Has to be less than 64KB"}
    (not (re-seq #"\S" (str contents))) {:error "Your paste cannot be empty."}
    :else {:contents contents}))

(defn parse-date [date]
  (format/parse))

(defn paste
  "Create a new paste."
  [language contents private user & [fork]]
  (let [validated (validate contents)]
    (if-let [error (:error validated)]
      error
      (let [id (swap! paste-id inc)
            result (mc/insert-and-return "pastes" (paste-map (when-not private id)
                              id
                              user
                              language
                              (:contents validated)
                              (format/unparse (format/formatters :date-time) (time/now))
                              private
                              fork))]
        (if private
          (let [new (assoc result :paste-id (str (:_id result)))]
            (mc/update "pastes" result new)
            new)
          result)))))

(defn get-paste
  "Get a paste."
  [id]
  (mc/find-one-as-map "pastes" {:paste-id id}))

(defn get-paste-by-id
  "Get a paste by its :id key (which is the same regardless of being public or private."
  [id]
  (mc/find-one-as-map "pastes" {:id id}))

(defn update-paste
  "Update an existing paste."
  [old language contents private user]
  (let [validated (validate contents)
        error (:error validated)]
    (cond
      error error
      (nil? user) "You must be logged in to edit pastes."
      (not= (:id user) (:user old)) "You can only edit your own pastes!"
      :else (let [old-private (:private old)
                  new-private (boolean private)
                  paste (paste-map
                         (cond
                           (= old-private new-private) (:paste-id old)
                           (false? new-private) (:id old)
                           (true? new-private) (str (:_id old)))
                         (:id old)
                         user
                         language
                         (:contents validated)
                         (:date old)
                         private
                         (:fork old))]
              (mc/update "pastes" old paste)
              paste))))

(defn delete-paste
  "Delete an existing paste."
  [id]
  (mc/remove "pastes" {:paste-id id}))

(defn get-pastes
  "Get public pastes."
  [page]
  ;; TODO: monger.query provides proper pagination support, I think it
  ;; makes sense to switch to that later. MK.
  (with-collection "pastes"
    (find {:private false})
    (sort {:date -1})
    (limit 20)
    (skip (* 20 (dec page)))))

(defn count-pastes
  "Count pastes."
  [& [private?]]
  (mc/count "pastes" (if-not (nil? private?)
                       {:private private?}
                       {})))

(defn count-pages [n per]
  (long (Math/ceil (/ n per))))

(defn proper-page [n]
  (if (<= n 0) 1 n))
