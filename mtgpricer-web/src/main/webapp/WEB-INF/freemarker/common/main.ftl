
<#macro page title>
<!DOCTYPE html>
<html lang="en">
	<head>
		<#include "head.ftl">
		<title>MTG Pricer - ${title}</title>
		<link href="/css/main.css" media="all" rel="stylesheet" /> 
	</head>
	<body>
		<nav class="navbar navbar-default">
			<div class="container-fluid">
				<div class="navbar-header">
					<a class="navbar-brand" href="/">MTG Pricer</a>
				</div>
				<ul class="nav navbar-nav navbar-right">
					<#if (_auth?? && _auth.isAuthenticated)>
						<li><a href="/logout">Logout</a></li>
					<#else>
						<li><a href="/login">Login</a></li>
					</#if>
				</ul>
				<form action="/search" class="navbar-form navbar-right" role="search">
					<input name="q" type="text" class="form-control" placeholder="Search">
				</form>
			</div>
		</nav>
		<#nested>
		<#include "foot.ftl">
	</body>
</html>
</#macro>