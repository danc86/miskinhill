var baseurl = 'http://getcopy.edina.ac.uk/resolve/';

var book_referent_fields = ['btitle', 'isbn', 'au', 'place', 'pub', 'date', 'spage', 'epage'];
var bookitem_referent_fields = ['btitle', 'atitle', 'isbn', 'au', 'place', 'pub', 'date', 'spage', 'epage'];
var article_referent_fields = ['atitle', 'jtitle', 'issn', 'au', 'volume', 'issue', 'date', 'spage', 'epage'];
var openurl_qs_from_citation = function (citation) {
    var openurl_args = ['url_ver=Z39.88-2004'];
    var fields = undefined;
    if ($(citation).hasClass('book')) {
        fields = book_referent_fields;
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:book', 'rft.genre=book');
    } else if ($(citation).hasClass('bookitem')) {
        fields = bookitem_referent_fields;
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:book', 'rft.genre=bookitem');
    } else if ($(citation).hasClass('article')) {
        fields = article_referent_fields;
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:journal', 'rft.genre=article');
    }
    $.each(fields, function () {
        var field = this;
        $(citation).find('.' + field).each(function () {
            var value = $(this).attr('title') || $(this).text().replace(/\s+/g, ' ');
            openurl_args.push('rft.' + encodeURIComponent(field) + '=' + encodeURIComponent(value));
        });
    });
    $(citation).append(' <a class="openurl" href="' + baseurl + '?' + openurl_args.join('&') + '"><img src="/images/silk/world_link.png" alt="OpenURL" /></a>');
    $(citation).find('a.openurl').hover(
            function () { $(citation).addClass('openurl-hover'); }, 
            function () { $(citation).removeClass('openurl-hover'); });
};

$(document).ready(function () {
    $('.citation').each(function () { openurl_qs_from_citation(this); });
});
