var show_footnote = function () {
    $('.floating-footnote').remove();
    var id = this.hash;
    var ff = $(id).clone(true).addClass('floating-footnote').hide();
    ff.css('left', $(this).parent().offset().left + 'px');
    ff.css('top', ($(this).offset().top + 20) + 'px');
    ff.prepend('<div style="float: right;">' +
            '<a href="#" class="close-button"><img src="/images/silk/cross.png" alt="[close]" title="Close this footnote" /></a>' + 
            '</div>');
    ff.find('.close-button').click(function () {
        ff.fadeOut('normal', function () { ff.remove(); });
        return false;
    });
    ff.insertAfter($(id)); // doesn't really matter where we insert it, but want to avoid stuffing up p + p styles
    ff.slideDown();
    return false;
};

var hide_footnote = function () {
    $('.floating-footnote').slideUp('fast', function () { $(this).remove(); });
    return false;
};

$(document).ready(function () {
    $('.footnote-anchor').click(show_footnote);
});
