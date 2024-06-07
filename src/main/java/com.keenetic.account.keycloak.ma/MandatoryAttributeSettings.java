package com.keenetic.account.keycloak.ma;

import java.util.HashMap;

public class MandatoryAttributeSettings {
    public String attribute;
    public String comparison;
    public String formName;

    public MandatoryAttributeSettings(String attribute, String comparison, String formName) {
        this.attribute = attribute;
        this.comparison = comparison;
        this.formName = formName;
    }
}
