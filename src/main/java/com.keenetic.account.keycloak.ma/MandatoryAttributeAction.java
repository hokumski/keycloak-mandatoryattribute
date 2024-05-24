package com.keenetic.account.keycloak.ma;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;

public class MandatoryAttributeAction implements RequiredActionProvider {

    private static final Logger logger = Logger.getLogger(MandatoryAttributeAction.class);

    HashMap<String, MandatoryAttributeSettings> settings = new HashMap<>();

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

        LoginFormsProvider loginFormsProvider = requiredActionContext.form();
        Response challenge = loginFormsProvider.createForm(askAbout.formName);
        requiredActionContext.challenge(challenge);
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
