<%@page import="javax.ws.rs.core.Response" %>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title><%=request.getAttribute("javax.servlet.error.status_code")%> <%=Response.Status.fromStatusCode(Integer.valueOf(request.getAttribute("javax.servlet.error.status_code").toString()))%></title>
</head>

<body>

<h2><%=request.getAttribute("javax.servlet.error.status_code")%> <%=Response.Status.fromStatusCode(Integer.valueOf(request.getAttribute("javax.servlet.error.status_code").toString()))%></h2>

<p>${requestScope['javax.servlet.error.message']}</p>

<p>If you believe you are receiving this message in error, please <a href="/contact/">notify us</a>.</p>

</body>
</html>