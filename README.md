[![license][license-img]][license-url]
[![latest tag][latest-tag-img]][latest-tag-url]
[![latest release][latest-release-img]][latest-release-url]

[![build][build-img]][build-url]
[![analyze][analyze-img]][analyze-url]
[![dependabot][dependabot-img]][dependabot-url]

[![languages][languages-img]][languages-url]
[![alerts][alerts-img]][alerts-url]
[![code quality][code-quality-img]][code-quality-url]

[![maintainability][maintainability-img]][maintainability-url]
[![technical debt][technical-debt-img]][technical-debt-url]
[![vulnerabilities][vulnerabilities-img]][vulnerabilities-url]

# keycloak-regex-mapper

This project provides a [keycloak][keycloak] broker mapper that maps a
multivalued OIDC claim (e.g.: groups) or SAML attribute (e.g.: groupMembership)
into one or more realm and/or client role assignments based on regular
expressions.

## usage

### deployment

Copy `keycloak-regex-mapper-«version».ear` to `${KEYCLOAK_HOME}/deployments`.

### configuration

TODO

## development

### project structure

This project follows the module/bundle approach to packaging keycloak extensions:

* `module` builds the jar that contains the keycloak extensions

* `bundle` builds the ear that contains the jar from `module` and the jars for
  any not-provided dependencies

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

[maintainability-img]: https://badgen.net/codeclimate/maintainability/LucaFilipozzi/keycloak-regex-mapper?icon=codeclimate
[maintainability-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-regex-mapper/maintainability
[technical-debt-img]: https://badgen.net/codeclimate/tech-debt/LucaFilipozzi/keycloak-regex-mapper?icon=codeclimate
[technical-debt-url]: https://codeclimate.com/github/LucaFilipozzi/keycloak-regex-mapper/maintainability
[vulnerabilities-img]: https://badgen.net/snyk/LucaFilipozzi/keycloak-regex-mapper/main/pom.xml
[vulnerabilities-url]: https://snyk.io/test/github/lucafilipozzi/keycloak-regex-mapper?targetFile=pom.xml
