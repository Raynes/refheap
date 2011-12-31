(ns refheap.models.paste
  (:use [clojure.java.shell :only [sh]]
        [refheap.dates :only [parse-string]])
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
   (:paste-id
    (first
     (mongo/fetch
      :pastes
      :sort {:paste-id -1}
      :limit 1)))))

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
  [language text]
  (:out
   (sh "./pygmentize" "-fhtml" (str "-l" (lookup-lexer language))
       "-Olinenos=table,anchorlinenos=true,lineanchors=L,stripnl=False"
       :dir "resources/pygments"
       :in text)))

(defn paste-map [id language contents date private]
  {:paste-id id
   :user (:username (session/get :user) "anonymous")
   :language language
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
   :contents (pygmentize language contents)})

(defmacro when-short
  "Convenience macro. Just 'when' with a predicate that checks
   the length of contents and makes sure it is < 64k."
  [contents & body]
  `(when (< (count ~contents) 64000)
     ~@body))

(defn parse-date [date]
  (format/parse))

(defn paste
  "Create a new paste."
  [language contents private]
  (when-short contents
    (mongo/insert!
     :pastes
     (paste-map
      (swap! paste-id inc)
      language
      contents
      (format/unparse (format/formatters :date-time) (time/now))
      private))))

(defn get-paste
  "Get a paste."
  [id]
  (mongo/fetch-one
   :pastes
   :where {:paste-id (if (string? id) (Long. id) id)}))

(defn update-paste
  "Update an existing paste."
  [old language contents private]
  (when-short contents
    (let [paste (paste-map
                 (:paste-id old)
                 language
                 contents
                 (:date old)
                 private)]
      (mongo/update! :pastes old paste)
      paste)))

(defn delete-paste
  "Delete an existing paste."
  [id]
  (mongo/destroy! :pastes {:paste-id (Long. id)}))

(defn get-pastes
  "Get public pastes."
  [page]
  (mongo/fetch
   :pastes
   :where {:private false}
   :sort {:date -1}
   :limit 20
   :skip (* 10 (dec page))))

(defn count-pastes
  "Count pastes."
  [private?]
  (mongo/fetch-count
   :pastes
   :where {:private private?}))

(defn count-pages [n]
  (long (Math/ceil (/ n 10))))