$(document).ready(function () {
    $('a.citation-link').hover(
            function () { $(this).parent().addClass('citation-hover'); }, 
            function () { $(this).parent().removeClass('citation-hover'); });
});
