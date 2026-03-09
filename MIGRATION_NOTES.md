# Java 11 Migration Notes — Snowman (Enterprise Application)

## Summary

This document describes the migration of the Snowman enterprise application from **Java 1.7/8** to **Java 11 (LTS)**. The migration covers build tooling upgrades, dependency updates, test framework changes, and CI/CD modernization. The goal is to run on JDK 11 with full build and test parity while keeping code changes minimal and behavior-neutral.

### Why Java 11?

- Java 8 reached end of public updates; Java 11 is a Long-Term Support (LTS) release with continued security patches and performance improvements.
- Java 11 brings improved garbage collection (G1 as default), unified JVM logging, the `java.net.http.HttpClient`, and better container awareness.
- Many libraries and frameworks are dropping Java 8 support, making 11 the minimum for continued ecosystem compatibility.

---

## Build Tooling Changes

### Maven Compiler Plugin

| Setting | Before | After |
|---|---|---|
| `java.version` property | `1.7` | `11` |
| `maven-compiler-plugin` version | `3.1` | `3.11.0` |
| Compiler configuration | `<source>` / `<target>` | `<release>11</release>` |

The `<release>` flag (introduced in JDK 9) replaces the older `<source>`/`<target>` pair. It ensures the compiler cross-checks against the specified platform API, preventing accidental use of newer APIs.

### Maven Shade Plugin

| Setting | Before | After |
|---|---|---|
| `maven-shade-plugin` version | `3.1.0` | `3.5.1` (or latest 3.x) |

The shade plugin must be upgraded for Java 11 compatibility, particularly for correct handling of multi-release JARs and module descriptors.

### Maven Surefire Plugin

| Setting | Before | After |
|---|---|---|
| `maven-surefire-plugin` version | (default/inherited) | `3.2.5` |

An explicit modern Surefire version is required for reliable test execution on JDK 11. Older versions may fail to fork correctly or mishandle module-system class loading.

### Maven Failsafe Plugin

| Setting | Before | After |
|---|---|---|
| `maven-failsafe-plugin` version | (not configured) | `3.2.5` |

Added for integration test support with JDK 11 compatibility.

### Maven Enforcer Plugin

Added to enforce a minimum JDK version of 11 at build time:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-enforcer-plugin</artifactId>
  <version>3.5.0</version>
  <executions>
    <execution>
      <goals><goal>enforce</goal></goals>
      <configuration>
        <rules>
          <requireJavaVersion>
            <version>[11,)</version>
          </requireJavaVersion>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### Liquibase Maven Plugin

| Setting | Before | After |
|---|---|---|
| `liquibase-maven-plugin` version | `3.0.5` | `4.25.1` (or latest 4.x) |

Liquibase 3.0.x predates Java 11 support. Version 4.x is required for JDK 11 compatibility.

---

## Dependency Changes

### Test Framework

| Dependency | Before | After | Rationale |
|---|---|---|---|
| Mockito (`mockito-all`) | `1.10.19` | Mockito 3.x+ (`mockito-core`) | Mockito 1.x does not support Java 11. The `mockito-all` artifact is discontinued; use `mockito-core` instead. Mockito 3.x+ uses ByteBuddy instead of CGLib, which supports the Java 11 module system. |
| PowerMock | `1.7.3` (`powermock-api-mockito`) | `2.0.9` (`powermock-api-mockito2`) | PowerMock 1.x depends on Mockito 1.x internals and fails on JDK 11. Version 2.0.9 is the minimum that supports both Mockito 2.x+ and Java 11. The artifact name changes from `powermock-api-mockito` to `powermock-api-mockito2`. |
| JUnit | `4.13.1` | `4.13.1` (no change) | JUnit 4.13.x is compatible with Java 11. No change required. |

**Note:** PowerMock is generally discouraged on Java 11+ due to its deep bytecode manipulation. Long-term, consider refactoring tests to remove PowerMock dependency in favor of Mockito alone (see [Follow-up Items](#follow-up-items)).

### Hibernate

| Dependency | Before | After | Rationale |
|---|---|---|---|
| `hibernate-core` | `5.4.24.Final` | `5.4.24.Final` (no change) | Already Java 11 compatible. |
| `hibernate-entitymanager` | `4.3.10.Final` | `5.4.24.Final` | **Version mismatch fix.** The entitymanager was on 4.3.x while core was on 5.4.x. These must be aligned. In Hibernate 5.x, `hibernate-entitymanager` is effectively merged into `hibernate-core`; the separate artifact is kept for backward compatibility but must match the core version. |
| `hibernate-c3p0` | `4.3.10.Final` | `5.4.24.Final` | Must be aligned with `hibernate-core` version. |

### Database Drivers

| Dependency | Before | After | Rationale |
|---|---|---|---|
| Apache Derby | `10.11.1.1` | `10.15.2.0`+ | Derby 10.11 does not support Java 11. Version 10.15+ is required for JDK 11 compatibility. |
| HSQLDB | `2.3.2` | `2.7.2`+ | Recommended upgrade for Java 11 compatibility and bug fixes. |
| H2 | `2.1.210` | `2.1.210` (no change) | Already Java 11 compatible. |
| MySQL Connector/J | `8.0.16` | `8.0.16` (no change) | Already Java 11 compatible. Consider updating to latest 8.0.x for bug fixes. |

### Application Dependencies

| Dependency | Before | After | Rationale |
|---|---|---|---|
| Spring Framework | `[4.3.18,)` (range) | Pin to `5.3.x` | Spring 4.3.x has limited Java 11 support. Spring 5.1+ officially supports Java 11. Pinning to a specific 5.3.x version (e.g., `5.3.31`) is recommended over using version ranges. |
| Embedded Jetty | `[9.4.11.v20180605,)` (range) | Pin to `9.4.x` latest | Jetty 9.4.x supports Java 11. Pin to a specific version (e.g., `9.4.54.v20240208`) instead of a version range. |
| EhCache | `2.9.1` | `2.10.9.2` | Upgrade for Java 11 compatibility improvements. |
| ActiveMQ | `5.10.0` | `5.17.x`+ | ActiveMQ 5.10 is very old; upgrade for Java 11 compatibility and security fixes. |
| Quartz | `2.3.2` | `2.3.2` (no change) | Already Java 11 compatible. |
| Jackson Databind | `[2.8.11.1,)` (range) | Pin to `2.15.x`+ | Pin to a specific modern version; version ranges can introduce unexpected upgrades. |
| Commons Lang 3 | `3.6` | `3.6` (no change) | Already Java 11 compatible. |
| Joda-Time | `2.9.9` | `2.9.9` (no change) | Compatible with Java 11. Consider migrating to `java.time` in a future effort. |
| SLF4J | `1.7.25` | `1.7.25` (no change) | Compatible with Java 11. |

### Removed JDK Modules (Java EE)

No JAXB (`javax.xml.bind.*`), JAX-WS, CORBA, or Nashorn usage was found in the source code. No additional dependencies are needed for removed JDK modules.

---

## Test Framework Changes

### Upgrade Path

The test stack requires coordinated upgrades:

1. **Mockito**: Replace `mockito-all:1.10.19` with `mockito-core:3.12.4` (or latest 3.x).
2. **PowerMock**: Replace `powermock-api-mockito:1.7.3` and `powermock-module-junit4:1.7.3` with `powermock-api-mockito2:2.0.9` and `powermock-module-junit4:2.0.9`.
3. **JUnit 4**: No changes needed; `4.13.1` works on Java 11.

### Known Test Considerations

- PowerMock 2.x on Java 11 may require `--add-opens` JVM arguments for Surefire to allow reflective access to internal JDK APIs:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>
      --add-opens java.base/java.lang=ALL-UNNAMED
      --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    </argLine>
  </configuration>
</plugin>
```

- These `--add-opens` flags should be considered **temporary**. The long-term goal is to remove PowerMock and eliminate the need for deep reflective access.

---

## CI/CD Changes

### Travis CI to GitHub Actions

| Setting | Before | After |
|---|---|---|
| CI Provider | Travis CI | GitHub Actions |
| JDK | `oraclejdk8` | Temurin JDK 11 (`actions/setup-java@v4`) |
| Build command | `mvn clean install -U -Dliquibase.should.run=false` | Same, targeting JDK 11 |

Travis CI is being replaced with GitHub Actions for:

- Better GitHub integration and reliability
- Temurin (Adoptium) JDK 11 distribution (vendor-agnostic, free)
- Maven dependency caching via `actions/setup-java`
- Easier matrix testing if needed (e.g., JDK 11 + 17 in future)

### Example GitHub Actions Workflow

```yaml
name: Java 11 Build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK 11
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '11'
          cache: maven
      - name: Build and Test
        run: mvn -B clean install -U -Dliquibase.should.run=false
```

---

## Known Considerations

### Module System (JPMS)

- The application runs on the **classpath** (no `module-info.java`). This is intentional to minimize migration scope.
- No JPMS module adoption is planned for this migration.

### Illegal Reflective Access

- PowerMock performs deep bytecode manipulation that triggers illegal reflective access warnings (and errors with `--illegal-access=deny`, the default in Java 16+).
- Targeted `--add-opens` flags are used in Surefire for test execution. These are **not** applied to production runtime.
- Plan to remove PowerMock in a follow-up to eliminate this requirement.

### Hibernate Version Alignment

- The pre-migration `pom.xml` had a **version mismatch**: `hibernate-core` at `5.4.24.Final` but `hibernate-entitymanager` and `hibernate-c3p0` at `4.3.10.Final`. This mismatch can cause runtime `NoSuchMethodError` or `ClassNotFoundException` issues.
- All Hibernate modules must be aligned to the same version (`5.4.24.Final`).

### Version Ranges in pom.xml

- Several dependencies use Maven version ranges (e.g., `[4.3.18,)` for Spring, `[9.4.11.v20180605,)` for Jetty, `[2.8.11.1,)` for Jackson). These are **not recommended** as they can cause non-reproducible builds.
- Pin all dependencies to specific versions for build reproducibility.

### GC and JVM Logging

- Java 11 defaults to the **G1 garbage collector** (changed from Parallel GC in Java 8).
- Legacy GC logging flags (e.g., `-XX:+PrintGCDetails`) are removed. Use **Unified Logging** instead:
  ```
  -Xlog:gc*:file=gc.log:time,uptime,level,tags
  ```
- Review any JVM flags in `run.sh` or deployment scripts for obsolete/removed options.

### TLS/Security

- Java 11 enables **TLS 1.3** by default. Verify that external endpoints (databases, message brokers, APIs) support TLS 1.2+.
- Default keystore type changed from JKS to **PKCS12**. If using custom keystores, verify compatibility.

---

## Follow-up Items

The following items are out of scope for the initial Java 11 migration but are recommended for future work:

1. **Remove PowerMock**: Refactor tests to use Mockito only, eliminating the need for `--add-opens` and deep reflective access. This also simplifies future JDK upgrades (17, 21).

2. **Migrate to JUnit 5**: Upgrade from JUnit 4 to JUnit 5 (Jupiter) for modern test features, better parameterized tests, and improved extension model.

3. **Pin dependency versions**: Replace all Maven version ranges (`[x,)`) with specific pinned versions for reproducible builds. Consider using `maven-enforcer-plugin`'s `requireUpperBoundDeps` rule.

4. **Migrate from Joda-Time to `java.time`**: The `java.time` API (introduced in Java 8) is the standard replacement for Joda-Time.

5. **Upgrade Spring to 5.3.x or 6.x**: Spring 5.3.x is the recommended baseline for Java 11. Spring 6.x targets Java 17+ and the Jakarta namespace.

6. **Adopt `java.net.http.HttpClient`**: Where the application makes HTTP calls, consider using the built-in Java 11 HTTP client instead of third-party libraries.

7. **Evaluate JPMS module adoption**: Consider adding `module-info.java` for better encapsulation, though this is a larger effort.

8. **Upgrade to Hibernate 5.6.x or 6.x**: Hibernate 5.6 is the latest 5.x with full Java 11 support. Hibernate 6.x targets Jakarta Persistence (for future Jakarta EE migration).

9. **Container/runtime updates**: If deploying in Docker, switch base images from Java 8 to Java 11 (e.g., `eclipse-temurin:11-jre`).

10. **Use `var` for local type inference**: Introduced in Java 10, `var` can simplify verbose local variable declarations. Apply sparingly and only where readability improves.

---

## References

- [Oracle JDK Migration Guide](https://docs.oracle.com/en/java/javase/11/migrate/)
- [Maven Compiler Plugin - release flag](https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html)
- [PowerMock Java 11 Compatibility](https://github.com/powermock/powermock/wiki/PowerMock-Configuration)
- [Hibernate ORM 5.4 Documentation](https://hibernate.org/orm/documentation/5.4/)
- [Spring Framework Java 11 Support](https://spring.io/blog/2018/11/14/spring-framework-5-1-2-available-now)
- [Eclipse Temurin (Adoptium)](https://adoptium.net/)
