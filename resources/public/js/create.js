(function ( $, window ) {

  var refheap = refheap || {};

  /**
   * Used to keep track of which modes have been loaded.
   */
  refheap.loaded = {};

  /**
   * List of available language modes. Some languages have other mode
   * dependencies, in this case the dependencies should be listed first
   * with the final mode being the mode that will be set on the editor.
   */
  refheap.langs =
    { "C++"             : ["clike"],
      "C"               : ["clike"],
      "Java"            : ["clike"],
      "Objective-C"     : ["clike"],
      "Clojure"         : ["clojure"],
      "Apricot"         : ["clojure"],
      "CoffeeScript"    : ["coffeescript"],
      "CSS"             : ["css"],
      "Diff"            : ["diff"],
      "Go"              : ["go"],
      "Groovy"          : ["groovy"],
      "Haskell"         : ["haskell"],
      "Javascript"      : ["javascript"],
      "Lua"             : ["lua"],
      "Delphi"          : ["pascal"],
      "Perl"            : ["perl"],
      "PHP"             : ["css", "javascript", "xml", "clike", "php"],
      "Java Properties" : ["properties"],
      "Python"          : ["python"],
      "R"               : ["r"],
      "RPM Spec"        : null,
      "Ruby"            : ["ruby"],
      "Rust"            : ["rust"],
      "Scheme"          : ["scheme"],
      "Emacs Lisp"      : ["scheme"],
      "Smalltalk"       : ["smalltalk"],
      "Verilog"         : ["verilog"],
      "XML"             : ["xml"],
      "YAML"            : ["yaml"],
      "HTML"            : ["css", "xml", "javascript", "htmlmixed"],
      "MySQL"           : ["mysql"],
      "OCaml"           : ["ocaml"] };

  refheap.notLoaded = function(mode) {
    return !(mode in refheap.loaded);
  };

  refheap.setMode = function(modes, editor) {
    var notLoaded = $.grep(modes, refheap.notLoaded),
        promises = $.map(notLoaded, function(mode, index) {
          return $.getScript("/js/codemirror/mode/" + mode + "/" + mode + ".js");
        });
    return $.when.apply($, promises).done(function() {
      $.each(notLoaded, function(mode, i) {
        refheap.loaded[mode] = true;
      });
      editor.setOption( "mode", modes[modes.length - 1]);
    });
  };

  /** This is ugly and horrible. */
  refheap.setNonStandardMode = function(lang, mode, relpath, editor) {
    $.getScript("/js/codemirror/mode/" + relpath, function() {
      refheap.loaded[lang] = true;
      editor.setOption("mode", mode);
    });
  };

  /**
   * Setup the editor for the specified language.
   */
  refheap.setupLang = function ( lang, editor ) {
    switch (lang) {
      case "RPM Spec":
        refheap.setNonStandardMode(lang, "spec", "rpm/spec/spec.js", editor);
        break;
      default:
        var modes = refheap.langs[lang];
        if ( modes ) {
          refheap.setMode(modes, editor);
        } else {
          editor.setOption( "mode", null );
        }
        break;
    }
  };

  /**
   *  Set height of the code editor dynamically.
   */
  refheap.setCodeHeight = function (editor) {
    var currentHeight = $( window ).height();
    if ( currentHeight > 600 ) {
      $( ".CodeMirror" ).height( currentHeight - 200 );
      editor.refresh();
    }
  };

  /**
   * Toggles the private checkbox.
   */
  refheap.togglePrivate = function () {
    $( "#private" ).attr( "checked", !$(" #private" ).attr( "checked" ) );
  };

  /**
   * Submits the form.
   */
  refheap.paste = function () {
    $( "form[name=paste]" ).submit();
  };


  $( function () {
    $( "select#language" ).chosen();

    // Setup hotkeys

    var editor = CodeMirror.fromTextArea( $('#paste')[0], { lineNumbers: true,
                                                            theme: 'cmtn'} ),
        setLang = function () {
          refheap.setupLang( $("#language option:selected").text(), editor );
        };
        toggleWrapping = function () {
          editor.setOption("lineWrapping", !editor.getOption("lineWrapping"));
        };

    $("#language").on( "change", setLang );

    setLang();
    editor.focus();
    $(window).on( "resize", function () {
      refheap.setCodeHeight(editor);
    });

    editor.setOption( "extraKeys", {
      "Alt-P": refheap.togglePrivate,
      "Ctrl-Enter": refheap.paste,
      "Alt-W": toggleWrapping
      
    });
    refheap.setCodeHeight(editor);
  });

}( jQuery, window ));
