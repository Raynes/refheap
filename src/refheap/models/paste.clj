(ns refheap.models.paste
  (:use [clojure.java.shell :only [sh]])
  (:require [somnium.congomongo :as mongo]
            [noir.session :as session]))

(def paste-count
  "The current count of pastes."
  (atom (mongo/fetch-count :pastes)))

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
       :dir "resources/pygments"
       :in text)))

(defn paste
  "Create a new paste."
  [language contents private]
  (let [user (session/get :username "anonymous")
        id (swap! paste-count inc)]    
    (mongo/insert! :pastes {:paste-id id
                            :user user
                            :language language
                            :raw-contents contents
                            :private (boolean private)
                            :lines (count (filter #{\newline} contents))
                            :contents (pygmentize language contents)})))

(defn get-paste
  "Get a paste."
  [id]
  (mongo/fetch-one :pastes :where {:paste-id id}))