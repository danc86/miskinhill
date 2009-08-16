
from string import Template

import webob.exc

MH_ERROR_TEMPLATE = Template('''\
<?xml version="1.0" encoding="UTF-8" ?>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
        <title>${status} - Miskin Hill</title>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <link rel="stylesheet" type="text/css" href="/style/common.css" />
	</head>
	<body>
        <div id="banner">
            <h1><a href="/">Miskin Hill</a></h1>
            <ul>
                <li><a href="/journals/">journals</a></li>
                <li><a href="/about/">about</a></li>
                <li><a href="/contact/">contact</a></li>
            </ul>
        </div>
        <div id="bodywrapper">
            <h1>${status}</h1>
            ${body}
            <div id="footer">
                Hosted by <a href="http://www.sjkwi.com.au/">SJK Web Industries</a>.
                Content copyright the publishers and each contributor.
                <a href="http://code.miskinhill.com.au/">Code</a> copyright Miskin Hill Academic Publishing.
                Metadata is free!
            </div>
        </div>
	</body>
</html>''')

HTTPException = webob.exc.HTTPException

class HTTPFound(webob.exc.HTTPFound):
    html_template_obj = MH_ERROR_TEMPLATE

class HTTPSeeOther(webob.exc.HTTPSeeOther):
    html_template_obj = MH_ERROR_TEMPLATE

class HTTPNotFound(webob.exc.HTTPNotFound):
    html_template_obj = MH_ERROR_TEMPLATE

class HTTPNotAcceptable(webob.exc.HTTPNotAcceptable):
    html_template_obj = MH_ERROR_TEMPLATE

class HTTPMethodNotAllowed(webob.exc.HTTPMethodNotAllowed):
    html_template_obj = MH_ERROR_TEMPLATE

