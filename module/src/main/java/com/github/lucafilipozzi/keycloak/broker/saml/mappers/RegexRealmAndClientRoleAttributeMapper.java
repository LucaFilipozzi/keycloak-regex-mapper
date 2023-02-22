// Copyright 2021 Luca Filipozzi. Some rights reserved. See LICENSE.

package com.github.lucafilipozzi.keycloak.broker.saml.mappers;

import com.github.lucafilipozzi.keycloak.broker.util.RegexRealmAndClientRoleMapperUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Map an attribute to realm and client roles via regex.
 */
public class RegexRealmAndClientRoleAttributeMapper extends AbstractIdentityProviderMapper {

  public static final String PROVIDER_ID = "lucafilipozzi-saml-regex-attribute-mapper";

  public static final String SAML_ATTRIBUTE_NAME = "saml-attribute-name";

  protected static final String[] COMPATIBLE_PROVIDERS = {SAMLIdentityProviderFactory.PROVIDER_ID};

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(RegexRealmAndClientRoleAttributeMapper.class);

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty samlAttributeNameConfigProperty = new ProviderConfigProperty();
    samlAttributeNameConfigProperty.setName(SAML_ATTRIBUTE_NAME);
    samlAttributeNameConfigProperty.setLabel("SAML attribute name");
    samlAttributeNameConfigProperty.setHelpText("name of SAML attribute to search (friendly or otherwise)");
    samlAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(samlAttributeNameConfigProperty);

    ProviderConfigProperty clientRolesAttributeNameConfigProperty = new ProviderConfigProperty();
    clientRolesAttributeNameConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.CLIENT_ROLES_ATTRIBUTE_NAME);
    clientRolesAttributeNameConfigProperty.setLabel("client roles attribute name");
    clientRolesAttributeNameConfigProperty.setHelpText("only evaluate client roles having an attribute with this name");
    clientRolesAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(clientRolesAttributeNameConfigProperty);

    ProviderConfigProperty clientRolesRegularExpressionConfigProperty = new ProviderConfigProperty();
    clientRolesRegularExpressionConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.CLIENT_ROLES_REGULAR_EXPRESSION);
    clientRolesRegularExpressionConfigProperty.setLabel("client roles regular expression");
    clientRolesRegularExpressionConfigProperty.setHelpText("regular expression to apply to the SAML attribute to extract client roles; must specify two named-capturing groups: client and role");
    clientRolesRegularExpressionConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(clientRolesRegularExpressionConfigProperty);

    ProviderConfigProperty realmRolesAttributeNameConfigProperty = new ProviderConfigProperty();
    realmRolesAttributeNameConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.REALM_ROLES_ATTRIBUTE_NAME);
    realmRolesAttributeNameConfigProperty.setLabel("realm roles attribute name");
    realmRolesAttributeNameConfigProperty.setHelpText("only evaluate realm roles having an attribute with this name");
    realmRolesAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(realmRolesAttributeNameConfigProperty);

    ProviderConfigProperty realmRolesRegularExpressionConfigProperty = new ProviderConfigProperty();
    realmRolesRegularExpressionConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.REALM_ROLES_REGULAR_EXPRESSION);
    realmRolesRegularExpressionConfigProperty.setLabel("realm roles regular expression");
    realmRolesRegularExpressionConfigProperty.setHelpText("regular expression to apply to the SAML attribute to extract realm roles; must specify one named-capturing group: role");
    realmRolesRegularExpressionConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(realmRolesRegularExpressionConfigProperty);

    ProviderConfigProperty searchRolesAttributeNameAttributeNameConfigProperty = new ProviderConfigProperty();
    searchRolesAttributeNameAttributeNameConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.SEARCH_ROLES_ATTRIBUTE_NAME);
    searchRolesAttributeNameAttributeNameConfigProperty.setLabel("search roles attribute name");
    searchRolesAttributeNameAttributeNameConfigProperty.setHelpText("only evaluate realm or client roles having an attribute with this name");
    searchRolesAttributeNameAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(searchRolesAttributeNameAttributeNameConfigProperty);

    ProviderConfigProperty searchRolesAttributeNameRegularExpressionConfigProperty = new ProviderConfigProperty();
    searchRolesAttributeNameRegularExpressionConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.SEARCH_ROLES_REGULAR_EXPRESSION);
    searchRolesAttributeNameRegularExpressionConfigProperty.setLabel("search roles regular expression");
    searchRolesAttributeNameRegularExpressionConfigProperty.setHelpText("regular expression to apply to the SAML claim to search for roles having this attribute value; must specify one named-capturing groups: value");
    searchRolesAttributeNameRegularExpressionConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(searchRolesAttributeNameRegularExpressionConfigProperty);
  }

  @Override
  public String[] getCompatibleProviders() {
    return COMPATIBLE_PROVIDERS;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getDisplayCategory() {
    return "Role Importer";
  }

  @Override
  public String getDisplayType() {
    return "Regex Realm and Client Role Importer";
  }

  @Override
  public String getHelpText() {
    return "implements regex realm and client role importer";
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
    return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
  }

  @Override
  public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("import user");
    processUser(realm, user, mapper, context);
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("update user");
    processUser(realm, user, mapper, context);
  }

  private void processUser(RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, BrokeredIdentityContext context) {
    LOG.trace("process user");
    String samlAttributeName = mapper.getConfig().getOrDefault(SAML_ATTRIBUTE_NAME, "");
    AssertionType assertion = (AssertionType) context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
    Set<String> assertedValues = assertion.getAttributeStatements().stream()
        .flatMap(statement -> statement.getAttributes().stream())
        .filter(choice -> choice.getAttribute().getFriendlyName().equals(samlAttributeName) || choice.getAttribute().getName().equals(samlAttributeName))
        .flatMap(choice -> choice.getAttribute().getAttributeValue().stream())
        .map(Object::toString)
        .collect(Collectors.toSet());
    RegexRealmAndClientRoleMapperUtil.processUser(realm, user, mapper, assertedValues);
  }
}
