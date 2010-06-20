<%@page session="false" %>
<%@page import="au.com.miskinhill.web.HttpStatusReason" %>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title><%=request.getAttribute("javax.servlet.error.status_code")%> <%=HttpStatusReason.forStatusCode(request.getAttribute("javax.servlet.error.status_code"))%></title>
</head>

<body>

<h2><%=request.getAttribute("javax.servlet.error.status_code")%> <%=HttpStatusReason.forStatusCode(request.getAttribute("javax.servlet.error.status_code"))%></h2>

<p>${requestScope['javax.servlet.error.message']}</p>

<p>If you believe you are receiving this message in error, please <a href="/contact/">notify us</a>.</p>

</body>
</html>