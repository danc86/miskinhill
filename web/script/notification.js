$(document).ready(function () {
    var notifs = new String(document.cookie).match(/miskinhill-notification=([^;]+)/);
    if (notifs) {
        var notif = unescape(notifs[1]);
        $('#notification-placeholder').append('<div class="notification">' + notif + '</div>');
    }
});
