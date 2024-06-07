<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        ${msg("maPhoneTitle")}
    <#elseif section = "form">
    <div class="form-group">
        <div id="kc-terms-text">
            ${kcSanitize(msg("maPhoneText"))?no_esc}
        </div>
    </div>

    <form class="form-actions" action="${url.loginAction}" method="POST">

            <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="phone" class="${properties.kcLabelClass!}">${msg("phone")}</label>
                    </div>
                    <div class="${properties.kcInputWrapperClass!}">
                        <input type="text" id="phone" class="${properties.kcInputClass!}" name="phone"
                               value=""
                               aria-invalid="<#if messagesPerField.existsError('phone')>true</#if>"
                        />

                        <#if messagesPerField.existsError('phone')>
                            <span id="input-error-phone" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('phone'))?no_esc}
                            </span>
                        </#if>
                    </div>
            </div>

            <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doRegister")}"/>
            </div>

            **<#if jsvarheader??>${jsvarheader}</#if>**

            <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                <br/>
                <div class="${properties.kcFormOptionsWrapperClass!}">
                    <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                </div>
            </div>

    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
