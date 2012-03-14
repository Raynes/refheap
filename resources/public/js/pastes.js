function hlLine() {
  $('a[style]').removeAttr("style")
  $('a[href|="' + window.location.hash + '"]').attr("style", "color: #FFE93B;")
}

$(document).ready(function () {
  $("#delete").click(function(event) {
    var r = confirm("Are you sure you want to delete this paste? There is no getting it back.")
    if (r == false) {
      event.preventDefault()
    }
  })
  
  $(window).bind('hashchange', hlLine)
  
  if (window.location.hash) {
    hlLine()
  }
})
