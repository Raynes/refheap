(function ( $, window ) {
  var refheap = refheap || {};

  /**
   * Select all of the text in the selected elements.
   */
  $.fn.selectText = function(){
    var doc = document, element = this[0], range, selection;
    if (doc.body.createTextRange) {
      range = document.body.createTextRange();
      range.moveToElementText(element);
      range.select();
    } else if (window.getSelection) {
      selection = window.getSelection();
      range = document.createRange();
      range.selectNodeContents(element);
      selection.removeAllRanges();
      selection.addRange(range);
    }
  };

  /**
   * Hilight the line of code indicated by the current hash.
   */
  refheap.hlLine = function () {
    $('a[style]').removeAttr("style");
    $('a[href|="' + window.location.hash + '"]').attr("style", "color: #FFE93B;");
  };

  /**
   * Given a number, n, return that number unless it is less than min
   * or greater than max in which case either max or min will be
   * returned respectively.
   */
  refheap.limit = function ( n, min, max ) {
    if ( n < min ) {
      return min;
    } else if ( n > max ) {
      return max;
    } else {
      return n;
    }
  };

  /**
   * Display the raw content of the paste in a pop-under entirely
   * selected for easy copying.
   */
  refheap.showRaw = function () {
    refheap.getRaw().done( function ( text ) {
      var $cont = $( "#container" ),
	  pos = $cont.position(), pre,
          sizeDisplay = function () {
            pre.css({
	      position: "absolute",
	      top: pos.top,
	      left: pos.left,
	      height: $cont.outerHeight(),
	      width: $cont.outerWidth(),
	      zIndex: 1000,
	      overflow: "scroll"
	    });         
          },
	  ctrlCHandler = function ( e ) {
	    $( pre ).fadeOut( "fast", function () {
	      $( document ).unbind( "keydown", ctrlCHandler );
              $( window ).off( "resize", sizeDisplay );
	    });
	  };
      pre = $( "<pre id=\"rawDisplay\"></pre>" )
	.text( text )
        .css("display", "none");

      sizeDisplay();

      pre.appendTo( "body" )
	.fadeIn( "fast", function () {
	  $( pre ).selectText();
      	  $("<span id=\"copyTextPrompt\">copy the text, press ESC to close</span>")
	    .appendTo( pre );
	  $( document ).bind( "keydown", "esc", ctrlCHandler );
          $( window ).on( "resize", sizeDisplay );
	});
    });
  };

  /**
   * Retrieve the raw text for the paste. Returns a promise.
   */
  refheap.getRaw = function () {
    return $.get( window.location.pathname + "/raw" );
  };

  /**
   * Prompt the user for a line number then navigate the browser to
   * the correct hash for that line.
   */
  refheap.gotoLine = function () {
    var promptLine = parseInt( prompt( "Go to line...", "1" ), 10 ),
	lineCount = $( "a[href^=#L-]" ).length,
	line = refheap.limit( promptLine, 1, lineCount );
    window.location.hash = "#L-" + line;
  };

  /**
   * Navigate the browser to the edit page for the past if the user is logged in.
   */
  refheap.gotoEdit = function () {
    // This is probably not the best way to test if the user has logged in.
    if ( $( "#userbutton" ).length > 0 ) {
      window.location.href = window.location.pathname + "/edit";
    }
  };

  $(function () {
    $(window).on( "hashchange", refheap.hlLine);
    if ( window.location.hash ) {
      refheap.hlLine();
    }

    $("#delete").click(function(event) {
      var r = confirm("Are you sure you want to delete this paste? There is no getting it back.");
      if (r == false) {
	event.preventDefault();
      }
    });

    // Setup hotkeys
    Mousetrap.bind( "alt+g", refheap.gotoLine );
    Mousetrap.bind( "alt+ctrl+e", refheap.gotoEdit );
    Mousetrap.bind( "alt+r", refheap.showRaw );
  });

}( jQuery, window ));

