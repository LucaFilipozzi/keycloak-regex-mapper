[![license][license-img]][license-url]
[![latest tag][latest-tag-img]][latest-tag-url]
[![latest release][latest-release-img]][latest-release-url]

[![build][build-img]][build-url]
[![analyze][analyze-img]][analyze-url]
[![dependabot][dependabot-img]][dependabot-url]

[![languages][languages-img]][languages-url]
[![alerts][alerts-img]][alerts-url]
[![code quality][code-quality-img]][code-quality-url]

[![lines of code][lines-of-code-img]][lines-of-code-url]
[![maintainability][maintainability-img]][maintainability-url]
[![technical debt][technical-debt-img]][technical-debt-url]

# keycloak-regex-mapper

This project provides a [Keycloak][keycloak] broker mapper that maps a
multivalued OIDC claim (e.g.: groups) or SAML attribute (e.g.: groupMembership)
into one or more realm and/or client role assignments based on regular
expressions.

## usage

### deployment

Copy `keycloak-regex-mapper-«version».jar` to `${KEYCLOAK_HOME}/providers`.

### configuration

The _Advanced Claim to Role_ (OIDC) and _Advanced Attribute to Role_ (SAML) mappers included with
Keycloak provide a mechanism to map specific claim/attribute values to a specific target realm or
client. This can be tedious to configure if there are many target roles that should be mapped.

The purpose of the _Regex Realm and Client Role Importer_ mappers (one for OIDC, one for SAML)
included in this project is to provide a mechanism to map many entries in an OIDC claim
( e.g., `groups`) or SAML attribute (e.g.: `groupMembership`) to target roles using a single
configured mapper.

The mechanism relies on two principles:

* that the claim / attribute provider uses clientId and realmName values when naming things... in
  other words, the mapping exists on the claim/attribute provider
* assigning an attribute to each realm and claim role to be managed by the mapper

#### OIDC Example

Suppose that the claim provider has a group structure as follows:

```
/IdentityBrokers
  /idb1                         # this is the realm
    /Roles                      # these are the realm roles
      SupportAnalyst            # A
        member=alice
        member=bob
    /ServiceProviders           # these are the clients
      /sp1                      # B
        /Roles                  # these are the client roles for sp1
          Impersonator          # C
            member=alice
      /sp2                      # D
        /Roles                  # these are the client roles for sp2
          OtherRole             # E
            member=bob
```

Then, when Alice logs in to / through idb1, the `groups` claim would contain:

```
IdentityBroker/idb1/Roles/SupportAnalysts
IdentityBroker/idb1/ServiceProviders/sp1/Roles/Impersonator
```

Whereas Bob's would contain:

```
IdentityBroker/idb1/Roles/SupportAnalysts
IdentityBroker/idb1/ServiceProviders/sp2/Roles/OtherRole
```

At the identity broker, realm and client roles would be configured as follows:

```
Roles                           # these are the realm roles
  SupportAnalyst                # matches A above
Clients
  sp1                           # matches B above
    Roles                       # these are the client roles for sp1
      Impersonator              # matches C above
        attribute:
          key="automatically mapped"
          value="true"
  sp2                           # matches D above
    Roles                       # these are the client roles for sp2
      OtherRole                 # matches E above
        attribute:
          key="automatically mapped"
          value="true"
```

And the _Regex Realm and Client Role Importer_ mapper would be configured as follows:

| configuration key               | value                                                                    |
| ------------------------------- | ------------------------------------------------------------------------ |
| type                            | `Regex Realm and Client Role Importer`                                   |
| name                            | `groups to realm and client roles`                                       |
| sync mode override              | `force`                                                                  |
| OIDC claim name                 | `groups`                                                                 |
| client roles attribute name     | `automatically mapped`                                                   |
| client roles regular expression | `/IdentityBrokers/idb1/ServiceProviders/(?<client>.*)/Roles/(?<role>.*)` |
| realm roles attribute name      | `automatically mapped`                                                   |
| realm roles regular expression  | `/IdentityBrokers/idb1/Roles/(?<role>.*)`                                |

The purpose of the the `client roles attribute name` and the `realm roles attribute name` is to flag
for the mapper which client and realm roles to assign / un-assign. Otherwise, every role not
matching the regular expressions would be un-assigned, including those that might have been locally
assigned by an administrator.

Take note of the named groupings (e.g.: `(?<client>.*)` in the regular expressions:

* the `client roles regular expression` needs two: `client` and `role`.
* The `realm roles regular expression` only needs one: `role`.

#### SAML example

Suppose the attribute provider draws group membership from an LDAP server structured as follows:

```
dc=example,dc=com
  ou=IdentityBrokers
    ou=idb1
      ou=Roles                  # realm roles
        cn=SystemAnalyst
          member=alice
          member=bob
      ou=ServiceProviders
        ou=sp1
          ou=Roles              # client roles for sp1
            cn=Impersonator
              member=alice
        ou=sp2
          ou=Roles              # client roles for sp2
            cn=OtherRole
              member=bob
```

For Alice, groupMembership would contain:

```
                          cn=SystemAnalyst,ou=Roles,ou=idb1,ou=IdentityBrokers,dc=example,dc=com
cn=Impersonator,ou=Roles,ou=sp1,ou=ServiceProviders,ou=idb1,ou=IdentityBrokers,dc=example,dc=com
```

For Bob, groupMembership would contain:

```
                          cn=SystemAnalyst,ou=Roles,ou=idb1,ou=IdentityBrokers,dc=example,dc=com
   cn=OtherRole,ou=Roles,ou=sp2,ou=ServiceProviders,ou=idb1,ou=IdentityBrokers,dc=example,dc=com
```

Assuming the same realm and client role configuration as above (in the OIDC example), then the _Regex
Realm and Client Role Importer_ mapper would be configured as follows:

| configuration key               | value                                                                                                       |
| ------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| type                            | `Regex Realm and Client Role Importer`                                                                      |
| name                            | `groups to realm and client roles`                                                                          |
| sync mode override              | `force`                                                                                                     |
| SAML attribute name             | `groupMembership`                                                                                           |
| client roles attribute name     | `automatically mapped`                                                                                      |
| client roles regular expression | `cn=(^<role>.*),ou=Roles,ou=(^<client>.*),ou=ServiceProviders,ou=idb1,ou=IdentityBrokers,dc=example,dc=com` |
| realm roles attribute name      | `automatically mapped`                                                                                      |
| realm roles regular expression  | `cn=(^<role>.*),ou=Roles,ou=idb1,ou=IdentityBrokers,dc=example,dc=com`                                      |

## development

### project structure

This project follows the module/bundle approach to packaging keycloak extensions:

* `module` builds the jar that contains the keycloak extensions

* `bundle` builds the ear that contains the jar from `module` and any jars that are
  not designated as `provided` dependencies

### coding conventions

This project uses:

* [checkstyle][checkstyle] to achieve compliance with the [Google Java Style Guide][style-guide].
  Please add the checkstyle plugin to your IDE.

* [SonarLint][sonarlint] to improve code quality and code security.
  Please add the SonarLint plugin to your IDE.

---
Copyright 2021 Luca Filipozzi. Some rights reserved. See [LICENSE][license-url].

[keycloak]: https://keycloak.org/

[style-guide]: https://google.github.io/styleguide/javaguide.html
[checkstyle]: https://checkstyle.sourceforge.io/
[sonarlint]: https://www.sonarlint.org/

[latest-release-img]: https://badgen.net/github/release/LucaFilipozzi/keycloak-regex-mapper?icon=github&label=latest%20release
[latest-release-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/releases/latest
[latest-tag-img]: https://badgen.net/github/tag/LucaFilipozzi/keycloak-regex-mapper?icon=github
[latest-tag-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/tags
[license-img]: https://badgen.net/github/license/LucaFilipozzi/keycloak-regex-mapper?icon=github
[license-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/blob/main/LICENSE

[analyze-img]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/actions/workflows/analyze.yml/badge.svg
[analyze-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/actions/workflows/analyze.yml
[build-img]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/actions/workflows/build.yml/badge.svg
[build-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/actions/workflows/build.yml
[dependabot-img]: https://badgen.net/github/dependabot/LucaFilipozzi/keycloak-regex-mapper?icon=dependabot
[dependabot-url]: https://github.com/LucaFilipozzi/keycloak-regex-mapper/network/dependencies

[languages-img]: https://badgen.net/lgtm/langs/g/LucaFilipozzi/keycloak-regex-mapper?icon=lgtm
[languages-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-regex-mapper/logs/languages/lang:java
[alerts-img]: https://badgen.net/lgtm/alerts/g/LucaFilipozzi/keycloak-regex-mapper/java?icon=lgtm
[alerts-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-regex-mapper/alerts
[code-quality-img]: https://badgen.net/lgtm/grade/g/LucaFilipozzi/keycloak-regex-mapper/java?icon=lgtm
[code-quality-url]: https://lgtm.com/projects/g/LucaFilipozzi/keycloak-regex-mapper/context:java

[lines-of-code-img]: https://badgen.net/codeclimate/loc/LucaFilipozzi/keycloak-regex-mapper?icon=codeclimate
[lines-of-code-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-regex-mapper
[maintainability-img]: https://badgen.net/codeclimate/maintainability/LucaFilipozzi/keycloak-regex-mapper?icon=codeclimate
[maintainability-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-regex-mapper/maintainability
[technical-debt-img]: https://badgen.net/codeclimate/tech-debt/LucaFilipozzi/keycloak-regex-mapper?icon=codeclimate
[technical-debt-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-regex-mapper/maintainability
