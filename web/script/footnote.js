// this should be in jquery probably
var bind = function (func, subj) {
    return function () { return func.call(subj); };
};

function FloatingFootnote(footnote_anchor) {
    this.footnote_selector = footnote_anchor.hash;
    this.div = $(this.footnote_selector).clone(true);
    this.div.hide();
    this.div.addClass('floating-footnote');
    this.div.css('left', $('.body-text').offset().left + 'px');
    this.div.css('top', ($(footnote_anchor).offset().top + 10) + 'px');
    this.div.prepend('<div style="float: right;">' +
            '<a href="#" class="close-button"><img src="/images/silk/cross.png" alt="[close]" title="Close this footnote" /></a>' + 
            '</div>');
    this.div.find('.close-button').click(bind(this.close, this));
    this.div.insertAfter($(this.footnote_selector)); // doesn't really matter where we insert it, but want to avoid stuffing up p + p styles
    FloatingFootnote.instances[this.footnote_selector] = this;
}
FloatingFootnote.prototype.open = function () {
    this.div.slideDown('normal');
    // close all others
    for (s in FloatingFootnote.instances) {
        if (s != this.footnote_selector)
            FloatingFootnote.instances[s].close();
    }
};
FloatingFootnote.prototype.close = function () {
    delete FloatingFootnote.instances[this.footnote_selector];
    var div = this.div;
    this.div.fadeOut('normal', function () { div.remove(); });
    return false; // for onclick
};
FloatingFootnote.instances = {}; // map of footnote id selector to FloatingFootnote instance

$(document).ready(function () {
    $('.footnote-anchor').click(function () {
        if (FloatingFootnote.instances[this.hash]) {
            // one is already shown, close it
            FloatingFootnote.instances[this.hash].close();
        } else {
            new FloatingFootnote(this).open();
        }
        return false;
    });
});
