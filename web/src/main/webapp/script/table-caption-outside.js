/* Add support for table-caption: top-outside; if it's not already supported */

var caption_outside_supported = function () {
	var test_div = document.createElement('div');
	test_div.style.cssText = 'caption-side: top-outside;';
	return !!test_div.style.captionSide;
};

if (!caption_outside_supported()) {
	$(function () {
		$('table caption').each(function (i, caption) {
			$('<div class="hacked-table-caption" />').append($(caption).contents()).insertBefore(caption.parentNode);
			$(caption).remove();
		});
	});
}
