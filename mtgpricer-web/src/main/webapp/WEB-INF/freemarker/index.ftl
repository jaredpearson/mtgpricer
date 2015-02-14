<#macro top cardPriceDiffs>
	<table class="table table-bordered table-striped">
		<#list cardPriceDiffs as cardPriceDiff>
			<tr>
				<td><a href="/cards/${cardPriceDiff.card.multiverseId?c}">${cardPriceDiff.card.name}</a></td>
				<td><a href="/sets/${cardPriceDiff.card.setCode}">${cardPriceDiff.card.setCode}</a></td>
				<td style="text-align: right;">${cardPriceDiff.value.doubleValue()?string.currency}</td>
			</tr>
		</#list>
	</table>
</#macro>
<#include "common/main.ftl">

<@page title="Home">

<style>
.tableTitle {
	font-weight: 700;
	padding: 8px;
	border-top: 1px solid #ddd;
	border-left: 1px solid #ddd;
	border-right: 1px solid #ddd;
	text-align: center;
}
</style>
	<div class="container-fluid">
		<div class="row">
			
			<div class="col-md-4">
				<div class="tableTitle">Top 7 Day Price Increases in Standard</div>
				<@top cardPriceDiffs=topPositiveCardsStandard />
			</div>
			
			<div class="col-md-4">
				<div class="tableTitle">Top 7 Day Price Increases</div>
				<@top cardPriceDiffs=topPositiveCards />
			</div>
			
			<div class="col-md-4">
				<div class="tableTitle">Top 7 Day Price Decreases</div>
				<@top cardPriceDiffs=topNegativeCards />
			</div>
			
		</div>
		<div class="row">
			<div class="col-md-12">
				<ul>
					<#list cardSets as cardSet>
						<li><a href="/sets/${cardSet.code}">${cardSet.name}</a></li>
					</#list>
				</ul>
			</div>
		</div>
	</div>
</@page>