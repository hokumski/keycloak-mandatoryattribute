package com.keenetic.account.keycloak.ma;

import org.keycloak.Config;
import org.keycloak.authentication.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import java.util.HashMap;


public class MandatoryAttributeActionFactory implements RequiredActionFactory  {

    private static final MandatoryAttributeAction SINGLETON = new MandatoryAttributeAction();

    @Override
    public String getDisplayText() {
        return "Mandatory Attribute - Configurable";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession keycloakSession) {
        return SINGLETON;
    }


    @Override
    public void init(Config.Scope scope) {

//    for (String s: scope.getPropertyNames()) {
//      System.out.println(s + ": " + scope.get(s, ""));
//      String name = s.substring(s.lastIndexOf("-")+1);
//      System.out.println(name + ": " + scope.get(name, ""));
//    }

        HashMap<String, MandatoryAttributeSettings> settings = new HashMap<>();
        for (int i=0; i<10; i++) {
            String selector;
            if (i == 0) {
                selector = "";
            } else {
                selector = String.valueOf(i);
            }
            String attr = scope.get("attr" + selector, "");
            if (!attr.isEmpty()) {
                String comparison = scope.get("comparison" + selector, "not_empty");
                String formName = scope.get("form" + selector, "");
                if (!formName.isEmpty()) {
                    MandatoryAttributeSettings mas = new MandatoryAttributeSettings(attr, comparison, formName);
                    settings.put(attr, mas);
                }
            }
        }
        SINGLETON.settings = settings;

        if (settings.keySet().isEmpty()) {
            System.out.println("Mandatory Attribute: no attributes are configured");
        } else {
            System.out.println("Mandatory Attribute: " + settings.keySet().size() + " attribute(s) are set to be mandatory");
            for (String attr : settings.keySet()) {
                MandatoryAttributeSettings mas = settings.get(attr);
                System.out.printf("* \"%s\" is set to be \"%s\", uses form \"%s\"%n", mas.attribute, mas.comparison, mas.formName);
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "mandatory_attribute";
    }

}
