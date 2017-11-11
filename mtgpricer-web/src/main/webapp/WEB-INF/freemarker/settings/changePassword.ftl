<#include "common/settings.ftl">

<@settingsPage title="Change Password">
	<h2>Change Password</h2>
	<#if success!false>
		<div class="callout callout-success">Password changed successfully.</div>
	<#else>
		<#if formError??>
			<div class="callout callout-error">
				${formError?html}
			</div>
		</#if>
		<form action="${postUrl?html}" method="POST" class="form-horizontal">
			<div class="form-group has-feedback <#if missingPassword??>has-error</#if>">
				<label class="col-sm-2 control-label">Current Password</label>
				<div class="col-sm-10">
					<input name="currentPassword" type="password" class="form-control" />
					<#if missingPassword??>
						<span class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>
					</#if>
				</div>
			</div>
			<div class="form-group has-feedback <#if missingNewPassword??>has-error</#if>">
				<label class="col-sm-2 control-label">New Password</label>
				<div class="col-sm-10">
					<input name="newPassword" type="password" class="form-control" />
					<#if missingNewPassword??>
						<span class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>
					</#if>
				</div>
			</div>
			<div class="form-group has-feedback <#if missingConfirmPassword??>has-error</#if>">
				<label class="col-sm-2 control-label">Confirm Password</label>
				<div class="col-sm-10">
					<input name="confirmNewPassword" type="password" class="form-control" />
					<#if missingConfirmPassword??>
						<span class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>
					</#if>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button class="btn btn-default">Change</button>
				</div>
			</div>
		</form>
	</#if>
</@settingsPage>