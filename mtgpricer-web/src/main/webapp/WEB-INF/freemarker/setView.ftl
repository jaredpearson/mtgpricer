<#include "common/main.ftl">

<@page title="${cardSetName}">
	<h1>${cardSetName}</h1>
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-8">
				<table class="table">
					<#list cards as card>
						<tr>
							<td>
								<#if card.multiverseId??>
									<a href="/cards/${card.multiverseId?c}">${card.name}</a>
								<#else>
									${card.name}
								</#if>
							</td>
							<td style="text-align: right;">
								<#if card.latestPrice??>
									${card.latestPrice?string.currency}
								</#if>
							</td>
						</tr>
					</#list>
				</table>
			</div>
			<div class="col-md-4"></div>
		</div>
	</div>
</@page>