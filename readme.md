# Keycloak Mandatory Attribute

This module implements _required action_ to check if _attribute_ of user's profile is filled or not.

This could be useful if you add mandatory attribute to the profile, and want users who have registered before to fill it out. 
In this case you need to add _required action_ to all users; for users who filled that attribute before 
there will be no changes during login, but for users with empty attribute the form will be shown.

**theme_directory** contains example of the form and messages. 
The form must contain input with name of the mandatory attribute. For example, if we are using the form to fill out 
attribute "phone", it must contain <input> (or <select>, for other cases) with *name="phone"*.
In case of error during form validation, module will set error with name *"yourAttr"* and value 
"mandatoryAttribute_*yourAttr*", where yourAttr is a name of attribute failed to validate. 

Later we can implement other comparison functions for attributes.

Sample configuration, add the block to **keycloak.conf**

```text
spi-required-action-mandatory_attribute-attr=phone
spi-required-action-mandatory_attribute-comparison=not_empty
spi-required-action-mandatory_attribute-form=ma-form.tpl
```

If you need to set up many mandatory attributes with different forms, add number from 1 to 9, 
use the syntax like:
```text
spi-required-action-mandatory_attribute-attr1=country
spi-required-action-mandatory_attribute-comparison1=not_empty
spi-required-action-mandatory_attribute-form1=ma-form-country.tpl

spi-required-action-mandatory_attribute-attr2=phone
spi-required-action-mandatory_attribute-comparison2=not_empty
spi-required-action-mandatory_attribute-form2=ma-form-phone.tpl
```