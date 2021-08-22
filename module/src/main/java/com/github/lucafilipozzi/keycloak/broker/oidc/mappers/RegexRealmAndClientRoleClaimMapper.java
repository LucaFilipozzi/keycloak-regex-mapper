// Copyright 2021 Luca Filipozzi. Some rights reserved. See LICENSE.

package com.github.lucafilipozzi.keycloak.broker.oidc.mappers;

import com.github.lucafilipozzi.keycloak.broker.util.RegexRealmAndClientRoleMapperUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Map a claim to realm and client roles via regex.
 */
public class RegexRealmAndClientRoleClaimMapper extends AbstractClaimMapper {

  public static final String PROVIDER_ID = "lucafilipozzi-oidc-regex-claim-mapper";

  public static final String OIDC_CLAIM_NAME = "oidc-claim-name";

  protected static final String[] COMPATIBLE_PROVIDERS = { KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID };

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  private static final Logger LOG = Logger.getLogger(RegexRealmAndClientRoleClaimMapper.class);

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty oidcClaimNameConfigProperty = new ProviderConfigProperty();
    oidcClaimNameConfigProperty.setName(OIDC_CLAIM_NAME);
    oidcClaimNameConfigProperty.setLabel("OIDC claim name");
    oidcClaimNameConfigProperty.setHelpText("name of OIDC claim to search");
    oidcClaimNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(oidcClaimNameConfigProperty);

    ProviderConfigProperty clientRolesAttributeNameConfigProperty = new ProviderConfigProperty();
    clientRolesAttributeNameConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.CLIENT_ROLES_ATTRIBUTE_NAME);
    clientRolesAttributeNameConfigProperty.setLabel("client roles attribute name");
    clientRolesAttributeNameConfigProperty.setHelpText("only evaluate client roles having an attribute with this name");
    clientRolesAttributeNameConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(clientRolesAttributeNameConfigProperty);

    ProviderConfigProperty clientRolesRegularExpressionConfigProperty = new ProviderConfigProperty();
    clientRolesRegularExpressionConfigProperty.setName(RegexRealmAndClientRoleMapperUtil.CLIENT_ROLES_REGULAR_EXPRESSION);
    clientRolesRegularExpressionConfigProperty.setLabel("client roles regular expression");
    clientRolesRegularExpressionConfigProperty.setHelpText("regular expression to apply to the OIDC claim to extract client roles; must specify two named-capturing groups: client and role");
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
    realmRolesRegularExpressionConfigProperty.setHelpText("regular expression to apply to the OIDC claim to extract realm roles; must specify one named-capturing groups: role");
    realmRolesRegularExpressionConfigProperty.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(realmRolesRegularExpressionConfigProperty);
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
    String oidcClaimName = mapper.getConfig().getOrDefault(OIDC_CLAIM_NAME, "");
    Set<String> assertedValues;
    Object claimValue = getClaimValue(context, oidcClaimName);
    if (claimValue instanceof List) {
      assertedValues = ((List<?>) claimValue).stream().map(String.class::cast).collect(Collectors.toSet());
    } else {
      assertedValues = Collections.emptySet();
    }
    RegexRealmAndClientRoleMapperUtil.processUser(realm, user, mapper, assertedValues);
  }
}
