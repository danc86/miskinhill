var default_baseurl = 'http://getcopy.edina.ac.uk/resolve/';

var fields = [
    'atitle', 'btitle', 'jtitle', 'isbn', 'issn', 'au', 'volume', 
    'issue', 'date', 'place', 'pub', 'date', 'spage', 'epage'
];
var openurl_qs_from_citation = function (baseurl, citation) {
    var openurl_args = ['url_ver=Z39.88-2004'];
    if ($(citation).hasClass('book')) {
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:book', 'rft.genre=book');
    } else if ($(citation).hasClass('bookitem')) {
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:book', 'rft.genre=bookitem');
    } else if ($(citation).hasClass('article')) {
        openurl_args.push('rft_val_fmt=info:ofi/fmt:kev:mtx:journal', 'rft.genre=article');
    }
    $.each(fields, function () {
        var field = this;
        $(citation).find('.' + field).each(function () {
            var value = $(this).attr('title') || $(this).text().replace(/\s+/g, ' ');
            openurl_args.push('rft.' + encodeURIComponent(field) + '=' + encodeURIComponent(value));
        });
    });
    $(citation).append(' <a class="openurl" href="' + baseurl + '?' + openurl_args.join('&') + '">' +
            '<img src="/images/silk/world_link.png" alt="OpenURL" /></a>');
    $(citation).find('a.openurl').hover(
            function () { $(citation).addClass('openurl-hover'); }, 
            function () { $(citation).removeClass('openurl-hover'); });
};

$(document).ready(function () {
    if ($('.citation').length) {
        // only show the openurl-control if there are any citations we can openurlify
        $('.metabox-container:first').append(
                '<div class="openurl-control metabox">' + 
                '   <h4>Citations</h4>' + 
                '   <p><label for="openurl-baseurl">OpenURL resolver:</label>' +
                '   <input id="openurl-baseurl" type="text" value="' + default_baseurl + '" />' +
                '   <button id="show-openurl">Show OpenURL links</button></p>' +
                '</div>');
    }
    $('#show-openurl').click(function () {
        $('.citation').each(function () {
            openurl_qs_from_citation($('#openurl-baseurl').attr('value'), this);
        });
    });
});
