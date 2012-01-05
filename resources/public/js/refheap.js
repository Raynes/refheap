// Just some BrowserID stuff.

$(document).ready(function(){
    $("#signin").click(function(event) {
      navigator.id.getVerifiedEmail(function(assertion) {
      if (assertion) {
        $.post('/user/verify',
               { assertion: assertion }, 
               function(data) { $("body").html(data) })
      }
      })
    })
  
  $("#delete").click(function(event) {
    var r = confirm("Are you sure you want to delete this paste? There is no getting it back.")
    if (r == false) {
      event.preventDefault()
    }
  })

  $("#gentoken").click(function(event) {
    $.get('/token/generate',
          function(data) { $("#tokentext").html(data) })
  })
});
