$(function() {
  $("#gentoken").click(function(event) {
    $.get('/token/generate',
          function(data) { $("#tokentext").html(data); });
  });
});
