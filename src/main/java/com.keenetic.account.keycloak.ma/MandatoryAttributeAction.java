package com.keenetic.account.keycloak.ma;

import jakarta.ws.rs.core.MultivaluedHashMap;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.MessageType;
import org.keycloak.models.UserModel;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.slf4j.event.KeyValuePair;

import java.util.HashMap;
import java.util.Map;

public class MandatoryAttributeAction implements RequiredActionProvider {

    private static final Logger logger = Logger.getLogger(MandatoryAttributeAction.class);

    HashMap<String, MandatoryAttributeSettings> settings = new HashMap<>();
    HashMap<String,String> jsvarsheader = new HashMap<>();

    @Override
    public void evaluateTriggers(RequiredActionContext requiredActionContext) {

    }

    Boolean isSatisfied(MandatoryAttributeSettings settings, String userAttrVal) {
        if (settings.comparison.equals("not_empty") && (userAttrVal == null || userAttrVal.isEmpty())) {
            return false;
        }
        return true;
    }

    MandatoryAttributeSettings selectAttributeToConfirm(UserModel userModel) {
        for (String attr : this.settings.keySet()) {
            MandatoryAttributeSettings setting = this.settings.get(attr);
            if (setting.formName.isEmpty()) {
                continue; // this setting is incorrect, and we must ignore it
            }
            String attrVal = userModel.getFirstAttribute(attr);
            if (!isSatisfied(setting, attrVal)) {
                return setting;
            }
        }
        return null;
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext requiredActionContext) {

        UserModel userModel = requiredActionContext.getUser();
        MandatoryAttributeSettings askAbout = selectAttributeToConfirm(userModel);

        if (askAbout == null) {
            requiredActionContext.success();
            return;
        }

        StringBuilder jsCode = getJsCode(requiredActionContext);
        LoginFormsProvider loginFormsProvider = requiredActionContext.form();
        if (jsCode.length() > 0) {
            jsCode = new StringBuilder("<script>\n" + jsCode + "</script>\n");
            loginFormsProvider.setAttribute("jsvarsheader", jsCode.toString());
        }

        Response challenge = loginFormsProvider.createForm(askAbout.formName);
        requiredActionContext.challenge(challenge);
    }

    private @NotNull StringBuilder getJsCode(RequiredActionContext requiredActionContext) {
        MultivaluedMap<String, String> headers = requiredActionContext.getHttpRequest().getHttpHeaders().getRequestHeaders();
        StringBuilder jsCode = new StringBuilder();
        for (Map.Entry<String, String> pair : this.jsvarsheader.entrySet()) {
            String val = headers.getFirst(pair.getValue());
            if (val == null) {
                val = "";
            }
            String varLine = String.format("var %s = \"%s\"; \n", pair.getKey(), val);
            jsCode.append(varLine);
        }
        return jsCode;
    }


    @Override
    public void processAction(RequiredActionContext requiredActionContext) {

        MultivaluedMap<String, String> formData =
                requiredActionContext.getHttpRequest().getDecodedFormParameters();

        UserModel userModel = requiredActionContext.getUser();

        MandatoryAttributeSettings settingWithError = null;
        for (String formParam : formData.keySet()) {
            if (this.settings.containsKey(formParam)) {
                MandatoryAttributeSettings setting = this.settings.get(formParam);
                String paramVal = formData.get(formParam).get(0);
                if (isSatisfied(setting, paramVal)) {
                    userModel.setSingleAttribute(formParam, paramVal);
                } else {
                    settingWithError = setting;
                    break;
                }
            }
        }

        if (settingWithError != null) {
            LoginFormsProvider loginFormsProvider = requiredActionContext.form();
            loginFormsProvider.setError("mandatoryAttribute_" + settingWithError.attribute);
            Response challenge = loginFormsProvider.createForm(settingWithError.formName);
            requiredActionContext.challenge(challenge);
            return;
        }

        // Checking again, if other attributes are set to be mandatory
        MandatoryAttributeSettings askAbout = selectAttributeToConfirm(userModel);
        if (askAbout != null) {
            LoginFormsProvider loginFormsProvider = requiredActionContext.form();
            Response challenge = loginFormsProvider.createForm(askAbout.formName);
            requiredActionContext.challenge(challenge);
            return;
        }

        requiredActionContext.success();
    }

    @Override
    public void close() {

    }
}
