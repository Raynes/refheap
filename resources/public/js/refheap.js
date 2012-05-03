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
                   $("body").html(data["chooselogin-html"])
                 }
               },
        });
      }
    })
  })
});
