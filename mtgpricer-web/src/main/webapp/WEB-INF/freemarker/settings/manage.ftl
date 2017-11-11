<#include "common/settings.ftl">
<#assign updateInProgressLabel = "Update in progress">

<@settingsPage title="Manage">
	<h2>Rip</h2>
	<p>Crawls the price sites for price information.</p>
	<div id="ripRequestStatus">
		<#if latestRipDate??>
			Last updated ${latestRipDate}
		<#elseif latestRipStartDate??>
			${updateInProgressLabel}
		</#if>
	</div>
	<button id="executeRip" class="btn btn-default" <#if !canCreateNewRipRequest!false>disabled="disabled"</#if>>Execute Now</button>
	
	<h2>Price Data Explorer</h2>
	<table class="table table-bordered">
	    <thead>
	        <tr>
	            <th>Retrieved</th>
	            <th>Sets</th>
	            <th>Cards</th>
	        </tr>
	    </thead>
	    <#list priceSites as priceSite>
	    <tr>
	        <td><a href="/settings/priceDataExplorer/${priceSite.id?c}">${priceSite.retrieved?datetime}</a></td>
	        <td>${priceSite.numberOfCardSets}</td>
	        <td>${priceSite.numberOfCards}</td>
	    </tr>
	    </#list>
	</table>

<script type="text/javascript">

var executeRipEl = document.getElementById('executeRip');
var ripRequestStatusEl = document.getElementById('ripRequestStatus');

executeRipEl.addEventListener('click', function() {
	executeRipEl.setAttribute('disabled', 'disabled');
	ripRequestStatusEl.innerHTML = '${updateInProgressLabel?js_string}';
	
	// create the rip request
	var xhr = new XMLHttpRequest();
	xhr.onload = function(e) {
		if (this.status === 200) {
			var createResult = JSON.parse(this.responseText);
			if (createResult.id) {
				pollRipRequest(createResult.id, updateRipRequestStatus);
			} else {
				alert('Unknown value returned by rip request.');
				console.log(this.responseText);
			}
			
		} else {
			alert('Failed to request new rip');
			console.log(this.responseText);
		}
	};
	xhr.open("post", "/api/ripRequest", true);
	xhr.send();
});

<#if ripInProgress!false>
pollRipRequest(${ripInProgressId}, updateRipRequestStatus);
</#if>

function updateRipRequestStatus(ripRequest) {
	if (ripRequest.finishDate) {
		ripRequestStatusEl.innerHTML = 'Rip Successful';
		
		executeRipEl.removeAttribute('disabled');
	}
}

/**
 * @param ripRequestId the ID of the rip request
 * @param callback function invoked on each poll invocation 
 */
function pollRipRequest(ripRequestId, callback) {
	var xhr = new XMLHttpRequest();
	xhr.onload = function(e) {
		if (this.status === 200) {
			var ripRequestData = JSON.parse(this.responseText);
			callback(ripRequestData);
			
			// if it hasn't finished, then poll for another
			// TODO: change to streaming/cometd instead of polling
			if (!ripRequestData.finishDate) {
				window.setTimeout(function() {
					pollRipRequest(ripRequestId, callback);
				}, 700);
			}
		} else {
			alert('Failed to get rip information');
			console.log(this.responseText);
		}
	};
	xhr.open("get", "/api/ripRequest/" + ripRequestId, true);
	xhr.send();
}

</script>
</@settingsPage>