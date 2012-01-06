function hlLine() {
  $('a[style]').removeAttr("style")
  $('a[href|="' + window.location.hash + '"]').attr("style", "color: #FFE93B;")
}

$(document).ready(function(){
    $("#signin").click(function(event) {
      navigator.id.getVerifiedEmail(function(assertion) {
      if (assertion) {
        $.ajax({type: "POST",
              url: "/user/verify",
              data: { assertion: assertion},
              dataType: "json",
              success: function(data) {
                if (data) {
                   $("#useri").html(data["login-html"]);
                   $("#language").after(data["private-html"])
                   $("body").html(data["chooselogin-html"])
                }
              },
             });
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

  $(window).bind('hashchange', hlLine)

  if (window.location.hash) {
    hlLine()
  }
});
