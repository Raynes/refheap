/*
 * Refheap UI Enhancements
 */

$( function ( $, window ) {

  var refheap = refheap || {};

  /**
   * Prompt the user for their BrowserID and verify via XHR POST.
   */
  refheap.signIn = function () {
    navigator.id.get( function ( assertion ) {
      $.post( "/user/verify", { assertion: assertion }, function ( data ) {
        if ( data ) {
          $( "#useri" ).html( data["login-html"] );
          $( "body" ).html( data["chooselogin-html"] );
        }
      });
    });
  };

  $( function () {
    $( "#signin" ).click( refheap.signIn );
  });
}( jQuery, window ));

