$(document).ready(function() {

  // Chosen
  $('select#language').chosen()

  // CodeMirror
  var loaded = {}

  function syncGetScript(script) {
    $.ajax({
      url: script,
      dataType: "script",
      async: false
    })
  }

  function getMode(mode) {
    var isLoaded = loaded[mode]
    var callback = arguments[1]
    if (!isLoaded) {
      syncGetScript("/js/codemirror/mode/" + mode + "/" + mode + ".js")
      loaded[mode] = true
      if (callback) { callback() }
    } else if (callback) { callback() }
  }

  function loadFn(mode) {
    return function(cm) {
      var setMode = function () {
        cm.setOption("mode", mode)
      }
      if (mode == "htmlmixed") {
        getMode("css")
        getMode("xml")
        getMode("javascript")
        getMode(mode, setMode)
      } else if (mode == "php") {
        getMode("css")
        getMode("javascript")
        getMode("xml")
        getMode("clike")
        getMode(mode, setMode)
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
                "HTML"            : loadFn("htmlmixed"),
                "MySQL"           : loadFn("mysql") }

  function loadMode(selected, editor) {
    lang = langs[selected]
    if (lang) {
      lang(editor)
    } else {
      editor.setOption("mode", null)
    }
  }

  var editor = CodeMirror.fromTextArea(document.getElementById('paste'), {
    lineNumbers: true,
    theme: 'cmtn'
  })

  var selected = $("#language option:selected").text()
  loadMode(selected, editor)

  $("#language").change(function () {
    loadMode($("#language option:selected").text(), editor)
  })

  editor.focus()

  // Set height of the code editor dynamically.
  function setCodeHeight() {
    var currentHeight = $(window).height()
    if (currentHeight > 600) {
      $('.CodeMirror-scroll').height(currentHeight - 200)
    }
  }

  $(window).resize(function() { setCodeHeight() })

  setCodeHeight()
})
