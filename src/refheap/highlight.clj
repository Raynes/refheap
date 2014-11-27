(ns refheap.highlight
  (:require [me.raynes.conch :refer [let-programs]]
            [clojure.string :as string]
            [refheap.markdown :refer [to-html]]))

(def lexers
  "A map of language names to pygments lexer names."
  {"Clojure" {:short "clojure"
              :exts #{"clj" "cljs"}}
   "Apricot" {:short "clojure"
              :exts #{"apr"}}
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
           :exts #{"pl"}}
   "Python Console" {:short "pycon"}
   "Python" {:short "python"
             :exts #{"py"}}
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
   "Racket" {:short "racket"
             :exts #{"rkt" "rktl"}}
   "Scheme" {:short "scm"
             :exts #{"scm" "ss"}}
   "Emacs Lisp" {:short "scm"
                 :exts #{"el"}}
   "Coq" {:short "coq"
          :exts #{"v"}}
   "Verilog" {:short "v"}
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
   "Shell Session" {:short "shell-session"
                    :exts #{"shell-session"}}
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
   "NewLisp" {:short "newlisp"
              :exts #{"lsp" "nl"}}
   "PostScript" {:short "postscript"
                 :exts #{"ps"}}
   "Protobuf" {:short "protobuf"
               :exts #{"proto"}}
   "REBOL" {:short "rebol"
            :exts #{"rebol"}}
   "COBOL" {:short "cobol"
            :exts #{"cob" "COB" "cpy" "CPY"}}
   "Rust" {:short "rust"
           :exts #{"rs" "rc"}}
   "Smali" {:short "smali"
            :exts #{"smali"}}
   "Ceylon" {:short "ceylon"
             :exts #{"ceylon"}}
   "Julia" {:short "julia"
             :exts #{"jl"}}
   "Julia Console" {:short "jlcon"}
   "AutoIt" {:short "autoit"
             :exts #{"au3"}}
   "Puppet" {:short "puppet"
             :exts #{"pp"}}
   "HXML" {:short "hxml"
           :exts #{"hxml"}}
   "TypeScript" {:short "ts"
                 :exts #{"ts"}}
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
           :exts #{"yaml" "yml"}}
   "ActionScript" {:short "as"
                   :exts #{"as"}}
   "CoffeeScript" {:short "coffeescript"
                   :exts #{"coffeescript" "coffee"}}
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
   "Elixir Console" {:short "iex"}
   "Markdown" {:short "Markdown"
               :exts #{"md" "markdown"}}
   "RPM Spec" {:short "spec"
               :exts #{"spec"}}})

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

(defn highlight
  "Syntax highlight some code. If anything other than markdown, highlight
   with pygments. If markdown, render with pegdown."
  [language text & [anchor?]]
  (if (= language "Markdown")
    (try
      {:success (to-html text)}
      (catch Exception _
        {:error "There was an error pasting."}))
    (let-programs [pygmentize "./pygmentize"]
      (let [output (pygmentize "-fhtml" (str "-l" language)
                               (str "-Olinenos=table,stripnl=False,encoding=utf-8"
                                    (when anchor? ",anchorlinenos=true,lineanchors=L"))
                               {:dir "resources/pygments"
                                :in text})]
        (if (seq output)
          {:success output}
          {:error "There was an error pasting."})))))
