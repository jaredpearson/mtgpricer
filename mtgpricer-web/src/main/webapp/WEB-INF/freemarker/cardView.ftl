<#include "common/main.ftl">

<@page title="${card.name}">
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-8">
				<h1>${card.name}</h1>
				<div>${cardSetName}</div>
			</div>
			<div class="col-md-4" style="text-align: right;">
				<#if latestPrice??>
					<h1>${latestPrice.doubleValue()?string.currency}</h1>
				</#if>
			</div>
		</div>
		<div class="row">
			<div class="col-md-8">
				<div id="chart"></div>
			
				<table class="table">
					<#list priceHistory as priceHistoryEntry>
						<tr>
							<td>${priceHistoryEntry.retrieved?string["yyyy-MM-dd hh:mm:ss a"]}</td>
							<td>
								<#if priceHistoryEntry.price??>
									${priceHistoryEntry.price.doubleValue()?string.currency}
								</#if>
							</td>
						</tr>
					</#list>
				</table>
			</div>
			<div class="col-md-4">
				<img src="/cards/${card.multiverseId?c}/image.hq.jpg" style="width: 300px; display: block; margin-left: auto; margin-right: auto;" title="${card.name}" alt="${card.name}" />
			</div>
		</div>
	</div>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.3/d3.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.8/c3.js"></script>
	<script type="text/javascript">
	var chart = c3.generate({
		bindto: '#chart',
		data: {
			x: 'x',
			columns: [
				['x', <#list priceHistory as priceHistoryEntry>'${priceHistoryEntry.retrieved?string["yyyy-MM-dd"]}'<#if priceHistoryEntry_has_next>,</#if></#list>],
		        ['Price', <#list priceHistory as priceHistoryEntry>${priceHistoryEntry.price.doubleValue()?c}<#if priceHistoryEntry_has_next>,</#if></#list>]
			]
		},
		axis: {
			y: {
				tick: {
					format: d3.format('$,.2f')
				}
			},
			x: {
				type: 'timeseries',
				tick: {
					format: '%Y-%m-%d'
				}
			}
		}
	});
	</script>
</@page>