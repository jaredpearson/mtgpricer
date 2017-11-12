<#include "common/settings.ftl">

<@settingsPage title="Price Data Explorer">
    <h2>Price Data Explorer</h2>
    <#list cardSets>
        <h3>Unknown Cards</h3>
        <p>Below are cards retrieved from the site but not found within the catalog.</p>
        <div style="margin-bottom: 1em;">
            <a class="btn btn-default" href="#" role="button" onclick="javascript: saveParserRules(event)">Save parser rule changes</a>
            <a class="btn btn-default" href="#" role="button" onclick="javascript: downloadParserRules(event)">Download parser rules</a>
        </div>
        <#items as cardSet>
            <#if cardSet.code?? && cardSet.unknownCards?size gt 0>
                <div data-cardset-code="${cardSet.code!}" class="panel panel-default">
                    <div class="panel-heading" onclick="javascript: toggleCardSetTable(event, '${cardSet.code?js_string}')" style="cursor: pointer">
                        <span class="chevron glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                        <a href="#" onclick="javascript: toggleCardSetTable(event, '${cardSet.code?js_string}')">${cardSet.name}</a>
                        <span class="badge pull-right">${cardSet.unknownCards?size}</span>
                    </div>
                    <div class="table-container hide"></div>
                </div>
            </#if>
        </#items>
    </#list>
    <script src="/js/priceDataExplorer.js"></script>
    <script>
    startPriceDataExplorer(${priceSiteInfo.id?c});
    </script>
</@settingsPage>