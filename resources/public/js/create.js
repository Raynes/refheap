$(document).ready(function() {

  // Chosen
  $('select#language').chosen();

  // CodeMirror
  var editor = CodeMirror.fromTextArea(document.getElementById('paste'), {
    lineNumbers: true,
    theme: 'cmtn',
  })
})
