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
                    <table class="hide table table-bordered">
                        <#list cardSet.unknownCards as unknownCard>
                            <tr data-raw-name="${unknownCard.rawName}">
                                <td class="col-md-8">${unknownCard.rawName}</td>
                                <td class="col-md-4">
                                    <select name="action[${unknownCard.rawName}]" onchange="javascript: onCardActionChange(event, '${cardSet.code?js_string}', '${unknownCard.rawName?js_string}')">
                                        <option value="_">No Action</option>
                                        <option value="_ignore">Ignore</option>
                                        <#list cardSet.unusedCards>
                                            <option value="_"></option>
                                            <#items as unusedCard>
                                                <#if unusedCard.number??>
                                                    <option value="number_${unusedCard.number}">${unusedCard.name} (${unusedCard.number})</option>
                                                <#elseif unusedCard.multiverseId??>
                                                    <option value="multiverse_${unusedCard.multiverseId?c}">${unusedCard.name}</option>
                                                </#if>
                                            </#items>
                                        </#list>
                                    </select>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </#if>
        </#items>
    </#list>
    <script src="/js/priceDataExplorer.js"></script>
</@settingsPage>