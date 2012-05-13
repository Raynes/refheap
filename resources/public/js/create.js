(function ( $, window ) {

  var refheap = refheap || {};

  /**
   * Used to keep track of which modes have been loaded.
   */
  refheap.loaded = {};
  
  /**
   * List of available language modes. Some langauges have other mode
   * dependencies, in this case the depencies should be listed first
   * with the final mode being the mode that will be set on the editor.
   */
  refheap.langs = 
    { "C++"             : ["clike"],
      "C"               : ["clike"],
      "Objective-C"     : ["clike"],
      "Clojure"         : ["clojure"],
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
      "Ruby"            : ["ruby"],
      "Rust"            : ["rust"],
      "Scheme"          : ["scheme"],
      "Emacs Lisp"      : ["scheme"],
      "Smalltalk"       : ["smalltalk"],
      "Verilog"         : ["verilog"],
      "XML"             : ["xml"],
      "YAML"            : ["yaml"],
      "HTML"            : ["css", "xml", "javascript", "htmlmixed"],
      "MySQL"           : ["mysql"] };

  /**
   * Setup the editor for the specified language.
   */
  refheap.setupLang = function ( lang, editor ) {
    var modes = refheap.langs[lang], i, promises = [];
    if ( modes ) {
      for ( i = 0; i < modes.length; i++ ) {
        if ( !( modes[i] in refheap.loaded ) ) {
          promises.push( $.getScript( "/js/codemirror/mode/" + modes[i] + "/" + 
                                      modes[i] + ".js" ) );
        }
      }

      $.when.apply( $, promises ).done( function () {
        for ( var i = 0; i < modes.length; i++ ) {
          refheap.loaded[modes[i]] = true;
        }
        editor.setOption( "mode", modes[modes.length - 1] );
      }); 
    } else {
      editor.setOption( "mode", null );
    }
  };

  /**
   *  Set height of the code editor dynamically.
   */
  refheap.setCodeHeight = function () {
    var currentHeight = $( window ).height();
    if ( currentHeight > 600 ) {
      $( ".CodeMirror-scroll" ).height( currentHeight - 200 );
    }
  };


  $( function () {
    $( "select#language" ).chosen();

    var editor = CodeMirror.fromTextArea( $('#paste')[0], { lineNumbers: true, 
                                                            theme: 'cmtn'} ), 
        setLang = function () {
          refheap.setupLang( $("#language option:selected").text(), editor );
        };

    $("#language").on( "change", setLang );

    setLang();
    editor.focus();
    $(window).on( "resize", refheap.setCodeHeight );
    refheap.setCodeHeight();
    editor.refresh();
  });
  
}( jQuery, window ));

