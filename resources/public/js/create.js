$(document).ready(function() {

  // Chosen
  $('select#language').chosen()

  var loaded = {}

  function getMode(mode, callback) {
    var isLoaded = loaded[mode]
    if (!isLoaded) {
      $.getScript("/js/codemirror/mode/" + mode + "/" + mode + ".js", callback)
      loaded[mode] = true
    } else { callback() }
  }

  function loadFn(mode) {
    return function(cm) {
      var setMode = function () {
        cm.setOption("mode", mode)
      }
      if (mode == "htmlmixed") {
        getMode("css", function () {
          getMode("xml", function () {
            getMode("javascript", function () {
              getMode(mode, setMode)
            })
          })
        })
      } else {
        getMode(mode, setMode)
      }
    }
  }

  var langs = { "C++"             : loadFn("clike"),
                "C"               : loadFn("clike"),
                "Objective-C"     : loadFn("clike"),
                "Clojure"         : loadFn("clojure"),
                "CoffeeScript"    : loadFn("coffeescript"),
                "CSS"             : loadFn("css"),
                "Diff"            : loadFn("diff"),
                "Go"              : loadFn("go"),
                "Groovy"          : loadFn("groovy"),
                "Haskell"         : loadFn("haskell"),
                "Javascript"      : loadFn("javascript"),
                "Lua"             : loadFn("lua"),
                "Delphi"          : loadFn("pascal"),
                "Perl"            : loadFn("perl"),
                "PHP"             : loadFn("php"),
                "Java Properties" : loadFn("properties"),
                "Python"          : loadFn("python"),
                "R"               : loadFn("r"),
                "Ruby"            : loadFn("ruby"),
                "Rust"            : loadFn("rust"),
                "Scheme"          : loadFn("scheme"),
                "Emacs Lisp"      : loadFn("scheme"),
                "Smalltalk"       : loadFn("smalltalk"),
                "Verilog"         : loadFn("verilog"),
                "XML"             : loadFn("xml"),
                "YAML"            : loadFn("yaml"),
                "HTML"            : loadFn("htmlmixed") }

  function loadMode(selected, editor) {
    lang = langs[selected]
    if (lang) {
      lang(editor)
    } else {
      editor.setOption("mode", null)
    }
  }

  // CodeMirror
  var editor = CodeMirror.fromTextArea(document.getElementById('paste'), {
    lineNumbers: true,
    theme: 'cmtn'
  })

  var selected = $("#language option:selected").text()
  loadMode(selected, editor)

  $("#language").change(function () {
    loadMode($("#language option:selected").text(), editor)
  })
})
