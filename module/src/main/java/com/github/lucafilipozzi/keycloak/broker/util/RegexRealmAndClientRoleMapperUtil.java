// Copyright 2021 Luca Filipozzi. Some rights reserved. See LICENSE.

package com.github.lucafilipozzi.keycloak.broker.util;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * Utilities for adjusting user's realm and client role assignments.
 */
public final class RegexRealmAndClientRoleMapperUtil {

  public static final String CLIENT_ROLES_ATTRIBUTE_NAME = "client-roles-attribute-name";

  public static final String CLIENT_ROLES_REGULAR_EXPRESSION = "client-roles-regular-expression";

  public static final String REALM_ROLES_ATTRIBUTE_NAME = "realm-roles-attribute-name";

  public static final String REALM_ROLES_REGULAR_EXPRESSION = "realm-roles-regular-expression";

  public static final String SEARCH_ROLES_ATTRIBUTE_NAME = "search-roles-attribute-name";

  public static final String SEARCH_ROLES_REGULAR_EXPRESSION = "search-roles-regular-expression";

  private static final Logger LOG = Logger.getLogger(RegexRealmAndClientRoleMapperUtil.class);

  private RegexRealmAndClientRoleMapperUtil() {
    throw new UnsupportedOperationException();
  }

  public static void processUser(RealmModel realm, UserModel user, IdentityProviderMapperModel mapper, Set<String> assertedValues) {
    LOG.trace("process user");

    // adjust the user's client role assignments
    String clientRolesRegularExpression = mapper.getConfig().getOrDefault(CLIENT_ROLES_REGULAR_EXPRESSION, "");
    String clientRolesAttributeName = mapper.getConfig().getOrDefault(CLIENT_ROLES_ATTRIBUTE_NAME, "");
    if(clientRolesRegularExpression != "" && clientRolesAttributeName != "")
        RegexRealmAndClientRoleMapperUtil.adjustUserClientRoleAssignments(realm, user, assertedValues, clientRolesRegularExpression, clientRolesAttributeName);

    // adjust the user's realm role assignments
    String realmRolesRegularExpression = mapper.getConfig().getOrDefault(REALM_ROLES_REGULAR_EXPRESSION, "");
    String realmRolesAttributeName = mapper.getConfig().getOrDefault(REALM_ROLES_ATTRIBUTE_NAME, "");
    if(realmRolesRegularExpression != "" && realmRolesRegularExpression != "")
        RegexRealmAndClientRoleMapperUtil.adjustUserRealmRoleAssignments(realm, user, assertedValues, realmRolesRegularExpression, realmRolesAttributeName);

    // adjust the user's attribute-based (search) role assignments
    String searchRolesRegularExpression = mapper.getConfig().getOrDefault(SEARCH_ROLES_REGULAR_EXPRESSION, "");
    String searchRolesAttributeName = mapper.getConfig().getOrDefault(SEARCH_ROLES_ATTRIBUTE_NAME, "");
    if(searchRolesRegularExpression != "" && searchRolesAttributeName != "")
        RegexRealmAndClientRoleMapperUtil.adjustUserSearchRoleAssignments(realm, user, assertedValues, searchRolesRegularExpression, searchRolesAttributeName);
  }

  private static void adjustUserClientRoleAssignments(RealmModel realm, UserModel user, Set<String> assertedValues, String regularExpression, String attributeName) {
    LOG.trace("adjust user client role assignments");

    Pattern pattern = Pattern.compile(regularExpression);

    // determine the client roles that the user should have
    Set<RoleModel> wantRoles = assertedValues.stream()
        .map(pattern::matcher)
        .filter(Matcher::matches)
        .filter(matcher -> matcher.groupCount() == 2)
        .filter(matcher -> matcher.group("client") != null)
        .filter(matcher -> matcher.group("role") != null)
        .flatMap(matcher ->
            realm.getClientsStream()
                .filter(client -> client.getClientId().equalsIgnoreCase(matcher.group("client")))
                .flatMap(ClientModel::getRolesStream)
                .filter(clientRole -> clientRole.getAttributeStream(attributeName).findAny().isPresent())
                .filter(clientRole -> clientRole.getName().equalsIgnoreCase(matcher.group("role"))))
        .collect(Collectors.toSet());

    // determine the client roles that user does have
    Set<RoleModel> haveRoles = user.getRoleMappingsStream()
        .filter(RoleModel::isClientRole)
        .filter(clientRole -> clientRole.getAttributes().containsKey(attributeName))
        .collect(Collectors.toSet());

    // assign the client roles that the user should have but doesn't
    Sets.difference(wantRoles, haveRoles).forEach(user::grantRole);

    // un-assign the client roles that the user has but shouldn't
    Sets.difference(haveRoles, wantRoles).forEach(user::deleteRoleMapping);
  }

  private static void adjustUserRealmRoleAssignments(RealmModel realm, UserModel user, Set<String> assertedValues, String regularExpression, String attributeName) {
    LOG.trace("adjust user realm role assignments");

    Pattern pattern = Pattern.compile(regularExpression);

    // determine the realm roles that the user should have
    Set<RoleModel> wantRoles = assertedValues.stream()
        .map(pattern::matcher)
        .filter(Matcher::matches)
        .filter(matcher -> matcher.groupCount() == 1)
        .filter(matcher -> matcher.group("role") != null)
        .flatMap(matcher ->
            realm.getRolesStream()
                .filter(realmRole -> !realmRole.isClientRole())
                .filter(realmRole -> realmRole.getAttributeStream(attributeName).findAny().isPresent())
                .filter(realmRole -> realmRole.getName().equalsIgnoreCase(matcher.group("role"))))
        .collect(Collectors.toSet());

    // determine the realm roles that the user does have
    Set<RoleModel> haveRoles = user.getRoleMappingsStream()
        .filter(realmRole -> !realmRole.isClientRole())
        .filter(realmRole -> realmRole.getAttributes().containsKey(attributeName))
        .collect(Collectors.toSet());

    // assign the realm roles that the user should have but doesn't
    Sets.difference(wantRoles, haveRoles).forEach(user::grantRole);

    // un-assign the realm roles that the user has but shouldn't
    Sets.difference(haveRoles, wantRoles).forEach(user::deleteRoleMapping);
  }

  private static void adjustUserSearchRoleAssignments(RealmModel realm, UserModel user, Set<String> assertedValues, String regularExpression, String attributeName) {
    LOG.trace("adjust user attribute-based role assignments");

    // TODO
  }
}
