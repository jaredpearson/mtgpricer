
<#macro page title>
<!DOCTYPE html>
<html lang="en">
	<head>
		<#include "head.ftl">
		<title>MTG Pricer - ${title}</title>
	</head>
	<body>
		<nav class="navbar navbar-default">
			<div class="container-fluid">
				<div class="navbar-header">
					<a class="navbar-brand" href="/">MTG Pricer</a>
				</div>
			</div>
		</nav>
		<#nested>
		<#include "foot.ftl">
	</body>
</html>
</#macro>