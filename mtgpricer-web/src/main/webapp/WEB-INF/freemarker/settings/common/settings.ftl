<#include "../../common/main.ftl">

<#macro settingsPage title>
<@page title=title>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-2">
                <#include "../menu.ftl">
            </div>
            <div class="col-md-10">
                <#nested>
            </div>
        </div>
    </div>
</@page>
</#macro>