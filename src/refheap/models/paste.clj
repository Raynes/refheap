(ns refheap.models.paste
  (:use [clojure.java.shell :only [sh]]
        [refheap.dates :only [parse-string]]
        [refheap.messages :only [error]])
  (:require [somnium.congomongo :as mongo]
            [noir.session :as session]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as format])
  (:import java.io.StringReader))

(def paste-id
  "The current highest paste-id."
  (atom
   (-> (mongo/fetch
        :pastes
        :sort {:id -1}
        :limit 1)
       first
       :id
       (or 0))))

(def lexers
  "A map of language names to pygments lexer names."
  {"Clojure" "clojure"
   "Factor" "factor"
   "Fancy" "fancy"
   "Groovy" "groovy"
   "Io" "io"
   "Ioke" "ioke"
   "Lua" "lua"
   "MiniD" "minid"
   "Perl" "perl"
   "Python 3" "python3"
   "Python 3 Traceback" "py3tb"
   "Python Console" "pycon"
   "Python" "python"
   "Python Traceback" "pytb"
   "Ruby Console" "irb"
   "Ruby" "ruby"
   "Duby" "ruby"
   "Tcl" "tcl" ;; Tickly.
   "C Object Dump" "c-objdump"
   "C++ Object Dump" "cpp-objdump"
   "D Object Dump" "d-objdump"
   "Gas" "gas"
   "LLVM" "llvm"
   "NASM" "nasm"
   "Object Dump" "objdump"
   "Ada" "ada"
   "BlitzMax" "bmax"
   "C" "c"
   "C++" "cpp"
   "Cython" "pyx"
   "Pyrex" "pyx"
   "D" "d"
   "Delphi" "dephi"
   "Dylan" "dylan"
   "eC" "ec"
   "Felix" "flx"
   "Fortran" "fortran"
   "GLSL" "glsl"
   "Go" "go"
   "Java" "java"
   "Modula-2" "m2"
   "Nimrod" "nim"
   "Objective-C" "objective-c"
   "ooc" "ooc"
   "Prolog" "prolog"
   "Scala" "scala"
   "Vala" "vala"
   "Boo" "boo"
   "C# Aspx" "aspx-cs"
   "C#" "csharp"
   "F#" "fsharp"
   "Nemerle" "nemerle"
   "VB.NET Apsx" "aspx-vb"
   "VB.NET" "vbnet"
   "Common Lisp" "cl"
   "Erlang" "erlang"
   "Erlang Shell" "erl"
   "Haskell" "hs"
   "Literate Haskell" "lhs"
   "OCaml" "ocaml"
   "Scheme" "scm"
   "Verilog" "v"
   "Matlab" "matlab"
   "Matlab Session" "matlabsession"
   "MuPAD" "mupad"
   "NumPy" "numpy"
   "R Console" "rout"
   "R" "r"
   "ABAP" "abap"
   "AppleScript" "applescript"
   "Asymptote" "asy"
   "Autohotkey" "ahk"
   "Awk" "awk"
   "Bash" "sh"
   "Bash Session" "console"
   "Batch" "bat"
   "Befunge" "befunge"
   "Brainfuck" "bf"
   "Gherkin" "cucumber"
   "Gnuplot" "gnuplot"
   "GoodData-CL" "gooddata-cl"
   "Hybris" "hy"
   "Logtalk" "logtalk"
   "MOOCode" "moocode"
   "Maql" "maql"
   "Modelica" "modelica"
   "MySQL" "mysql"
   "NewSpeak" "newspeak"
   "PostScript" "postscript"
   "Povray" "pov"
   "Protobuf" "protobuf"
   "REBOL" "rebol"
   "Redcode" "redcode"
   "Smalltalk" "squeak"
   "SQL" "sql"
   "Sqlite Console" "sqlite3"
   "TCSH" "csh"
   "Apache Configuration" "apacheconf"
   "BBCode" "bbcode"
   "CMake" "cmake"
   "Darcs Patch" "dpatch"
   "Debian Control" "control"
   "Diff" "diff"
   "INI" "ini"
   "IRC Logs" "irc"
   "Lighttpd" "lighty"
   "Makefile" "make"
   "Nginx Configuration" "nginx"
   "Java Properties" "properties"
   "rST" "rst"
   "LaTeX" "tex"
   "VimL" "vim"
   "YAML" "yaml"
   "ActionScript" "as"
   "CoffeeScript" "coffeescript"
   "CSS" "css"
   "DTD" "dtd"
   "HAML" "haml"
   "haXe" "hx"
   "HTML" "html"
   "Javascript" "js"
   "PHP" "php"
   "Plain Text" "text"
   "SASS" "sass"
   "Scaml" "scaml"
   "XML" "xml"})

(defn lookup-lexer
  "Look up a lexer given the name of a language. If one isn't found,
   the default plain-text lexer is used."
  [language]
  (lexers language "text"))

(defn pygmentize
  "Syntax highlight some code."
  [language text & [anchor?]]
  (:out
   (sh "./pygmentize" "-fhtml" (str "-l" (lookup-lexer language))
       (str "-Olinenos=table,stripnl=False,encoding=utf-8"
            (when anchor? ",anchorlinenos=true,lineanchors=L"))
       :dir "resources/pygments"
       :in text)))

(defn paste-map [paste-id id user language contents date private]
  {:paste-id (str paste-id)
   :id id
   :user (:id user)
   :language (if (lexers language)
               language
               "Plain Text")
   :raw-contents contents
   :summary (->> contents
                 StringReader.
                 io/reader
                 line-seq
                 (take 5)
                 (string/join "\n")
                 (pygmentize language))
   :private (boolean private)
   :date date
   :lines (let [lines (count (filter #{\newline} contents))]
            (if (= \newline (last contents))
              lines
              (inc lines)))
   :contents (pygmentize language contents true)})

(defn validate [contents]
  (cond
   (>= (count contents) 64000) {:error "That paste was too big. Has to be less than 64KB"}
   (not (re-seq #"\S" (str contents))) {:error "Your paste cannot be empty."}
   :else {:contents contents}))

(defn parse-date [date]
  (format/parse))

(defn paste
  "Create a new paste."
  [language contents private user]
  (let [validated (validate contents)]
    (if-let [error (:error validated)]
      error
      (let [id (swap! paste-id inc)
            result (mongo/insert!
                    :pastes
                    (paste-map
                     (when-not private id)
                     id
                     user
                     language
                     (:contents validated)
                     (format/unparse (format/formatters :date-time) (time/now))
                     private))]
        (if private
          (let [new (assoc result :paste-id (str (:_id result)))]
            (mongo/update! :pastes result new)
            new) 
          result)))))

(defn get-paste
  "Get a paste."
  [id]
  (mongo/fetch-one
   :pastes
   :where {:paste-id id}))

(defn update-paste
  "Update an existing paste."
  [old language contents private user]
  (let [validated (validate contents)]
    (if-let [error (:error validated)]
      error
      (let [old-private (:private old)
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
                   private)]
        (mongo/update! :pastes old paste)
        paste))))

(defn delete-paste
  "Delete an existing paste."
  [id]
  (mongo/destroy! :pastes {:paste-id id}))

(defn get-pastes
  "Get public pastes."
  [page]
  (mongo/fetch
   :pastes
   :where {:private false}
   :sort {:date -1}
   :limit 20
   :skip (* 20 (dec page))))

(defn count-pastes
  "Count pastes."
  [& [private?]]
  (mongo/fetch-count
   :pastes
   :where (when-not (nil? private?) {:private private?})))

(defn count-pages [n per]
  (long (Math/ceil (/ n per))))

(defn proper-page [n]
  (if (<= n 0) 1 n))