<#include "common/main.ftl">

<@page title="Search Results">
	<h1>Search Results</h1>
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-8">
			
				<#if found == 0>
					<div>Nothing found for "${query?html}"</div>
				<#else>
					<table class="table" style="margin-bottom: 0;">
						<#list results as result>
							<tr>
								<td>
									<#if result.card.multiverseId??>
										<a href="/cards/${result.card.multiverseId?c}">${result.card.name}</a>
									<#else>
										${result.card.name}
									</#if>
								</td>
								<td><a href="/sets/${result.card.setCode}">${result.card.setCode}</td>
							</tr>
						</#list>
					</table>
				</#if>
				
				<nav>
					<ul class="pagination pagination-sm">
						<li <#if !hasPrevious>class="disabled" </#if>>
							<#if hasPrevious>
								<a href="/search?q=${query?url}&start=${previousStart}" aria-label="Previous">
									<span aria-hidden="true">&laquo;</span>
								</a>
							<#else>
								<span aria-label="Previous">
									<span aria-hidden="true">&laquo;</span>
								</span>
							</#if>
						</li>
						<li <#if !hasNext>class="disabled" </#if>>
							<#if hasNext>
								<a href="/search?q=${query?url}&start=${nextStart}" aria-label="Next">
									<span aria-hidden="true">&raquo;</span>
								</a>
							<#else>
								<span aria-label="Next">
									<span aria-hidden="true">&raquo;</span>
								</span>
							</#if>
						</li>
					</ul>
				</nav>
			</div>
			<div class="col-md-4"></div>
		</div>
	</div>
</@page>