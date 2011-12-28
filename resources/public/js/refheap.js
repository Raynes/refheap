// Just some BrowserID stuff.

$(document).ready(function(){
    $("#signin").click(function(event) {
      navigator.id.getVerifiedEmail(function(assertion) {
      if (assertion) {
        $.post('/user/verify',
               { assertion: assertion }, 
               function(data) { $("body").html(data)})
      } else {
        alert("Login failure!")
      }
      })
    })
});
