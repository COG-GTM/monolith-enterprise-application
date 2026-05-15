# Security Audit Report — Snowman Enterprise Application

**Date:** 2026-05-15  
**Scope:** Full codebase static analysis (red team perspective)  
**Branch:** `master`  
**Repository:** `COG-GTM/monolith-enterprise-application`

---

## Executive Summary

A comprehensive static security audit of the Snowman Enterprise Application reveals **29 findings** across 8 categories. The application has fundamental security deficiencies stemming from a complete absence of authentication/authorization, hardcoded credentials throughout the codebase, plaintext password storage, use of Java Object serialization over JMS (a known RCE vector), and critically outdated dependencies with known CVEs. The application targets end-of-life Java 7, uses open-ended Maven version ranges creating supply chain risk, and runs an unencrypted HTTP-only embedded Jetty server with no security headers.

**Finding Counts by Severity:**

| Severity | Count |
|----------|-------|
| Critical | 6     |
| High     | 10    |
| Medium   | 9     |
| Low      | 3     |
| Info     | 1     |
| **Total**| **29**|

---

## Findings

---

### [CRITICAL] 1. Hardcoded Database Credentials in AbstractJDBCDao

- **Category:** Credential Exposure
- **File(s):** `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/AbstractJDBCDao.java:24-25`
- **Description:** Database username and password are hardcoded as `static final String` constants directly in the source code.
- **Evidence:**
  ```java
  private static final String DATABASE_USERNAME = "username";
  private static final String DATABASE_PASSWORD = "password";
  ```
- **Impact:** Anyone with source code access (e.g., via a repository leak, insider threat, or decompiled JAR) obtains valid database credentials. An attacker can connect directly to the MySQL database and read/modify/delete all data.
- **Remediation:** Move credentials to environment variables or a secrets manager (e.g., HashiCorp Vault, AWS Secrets Manager). Use Spring's `PropertySourcesPlaceholderConfigurer` to inject values from externalized configuration. Never commit credentials to source control.

---

### [CRITICAL] 2. Hardcoded Database Credentials in application.properties

- **Category:** Credential Exposure
- **File(s):** `src/main/resources/application.properties:4-5`
- **Description:** JDBC username and password are stored in plaintext in a properties file committed to source control.
- **Evidence:**
  ```properties
  jdbc.username=username
  jdbc.password=password
  ```
- **Impact:** Same as Finding #1 — full database access for anyone who obtains the source. These values are loaded by `application-context-db.xml` and used by both the Hibernate `SessionFactory` and the `JdbcTemplate`.
- **Remediation:** Externalize credentials using environment variables (`${DB_USERNAME}`), JNDI, or a secrets vault. Use `.gitignore` to exclude any file containing real credentials.

---

### [CRITICAL] 3. Hardcoded Database Credentials in liquibase.properties

- **Category:** Credential Exposure
- **File(s):** `src/main/resources/db/liquibase.properties:3-4`
- **Description:** Liquibase migration configuration contains hardcoded database credentials committed to the repository.
- **Evidence:**
  ```properties
  username: username
  password: password
  ```
- **Impact:** Provides a third independent source of database credentials in the codebase. Used by the Liquibase Maven plugin during the `process-resources` phase.
- **Remediation:** Reference environment variables in the Liquibase properties file or pass credentials via Maven command-line properties (`-Dliquibase.username=$DB_USER`).

---

### [CRITICAL] 4. Complete Absence of Authentication

- **Category:** Authentication & Authorization
- **File(s):** `pom.xml` (no `spring-security` dependency), `src/main/resources/webapp/WEB-INF/web.xml` (no security filter)
- **Description:** The application has zero authentication. There is no Spring Security dependency in `pom.xml`, no security filter configured in `web.xml`, and no custom authentication logic anywhere in the codebase. All REST endpoints are fully open.
- **Evidence:**
  - `pom.xml`: No `spring-security-core`, `spring-security-web`, or `spring-security-config` dependency.
  - `web.xml`: Only configures the Spring `DispatcherServlet` — no `DelegatingFilterProxy` or security filter chain.
  - Grep for `spring-security|SecurityFilter|csrf|authentication` returns zero matches in source code.
- **Impact:** Any network-reachable client can call any endpoint, including destructive operations like `DELETE /employee/{id}`, `DELETE /user/{id}`, `DELETE /client/{id}`, and management endpoints like `/cache/{name}/clear` and `/health`. This is a complete bypass of the CIA triad.
- **Remediation:** Add Spring Security with proper authentication (e.g., JWT, OAuth2, or session-based). Configure a `WebSecurityConfigurerAdapter` with endpoint-level access rules. At minimum, protect all write/delete endpoints and management endpoints.

---

### [CRITICAL] 5. No Password Hashing — Plaintext Password Storage and Retrieval

- **Category:** Authentication & Authorization
- **File(s):**
  - `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/impl/UserDaoImpl.java:39`
  - `src/main/java/com/mycompany/entapp/snowman/domain/service/impl/UserServiceImpl.java:27`
  - `src/main/resources/db/changelog/001_Create_Schema.xml:64` (password column is `VARCHAR(20)`)
- **Description:** Passwords are stored and retrieved as plain strings throughout the entire stack. The `user` table stores passwords as `VARCHAR(20)`, the DAO reads them directly via `rs.getString("password")`, and the service layer passes them through with no hashing.
- **Evidence:**
  ```java
  // UserDaoImpl.java:39
  user.setPassword(rs.getString("password"));
  ```
  ```xml
  <!-- 001_Create_Schema.xml:64 -->
  <column name="password" type="VARCHAR(20)"/>
  ```
  No imports of `bcrypt`, `PBKDF2`, `MessageDigest`, or any hashing library exist in the codebase.
- **Impact:** A database breach exposes all user passwords in cleartext. Given password reuse, this likely compromises users' accounts on other services.
- **Remediation:** Hash passwords with bcrypt (or PBKDF2/Argon2) before storage. Increase the `password` column size to at least `VARCHAR(60)` for bcrypt hashes. Never return password hashes in API responses.

---

### [CRITICAL] 6. ActiveMQ JMS ObjectMessage Deserialization (RCE Vector)

- **Category:** Injection / Deserialization
- **File(s):** `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/adapter/InvoiceSystemAdapter.java:38`
- **Description:** The `InvoiceSystemAdapter` creates JMS `ObjectMessage` instances using `session.createObjectMessage(clientDTO)`. Combined with ActiveMQ 5.10.0 (see Finding #19), this enables Java deserialization attacks. If an attacker can inject messages into the broker queue, they can achieve Remote Code Execution.
- **Evidence:**
  ```java
  // InvoiceSystemAdapter.java:38
  ObjectMessage objectMessage = session.createObjectMessage(clientDTO);
  ```
  The `ClientDTO` class implements `Serializable`, and so do its nested `ProjectDTO` objects.
- **Impact:** Remote Code Execution (RCE) via crafted serialized Java objects. ActiveMQ 5.10.0 is specifically vulnerable to CVE-2015-5254, which exploits ObjectMessage deserialization.
- **Remediation:** Replace `ObjectMessage` with `TextMessage` using JSON serialization (e.g., Jackson). If ObjectMessage must be used, configure ActiveMQ's `ClassFilter` to whitelist only expected classes. Upgrade ActiveMQ to a patched version.

---

### [HIGH] 7. Plaintext Passwords in Database Seed Data

- **Category:** Credential Exposure
- **File(s):** `src/main/resources/db/changelog/004_Insert_Dummy_Users.xml:12-15`
- **Description:** The Liquibase migration inserts dummy users with easily guessable plaintext passwords (`password`, `admin`, `test`, `dev`).
- **Evidence:**
  ```sql
  insert into user values (1, 'username', 'password', ...);
  insert into user values (2, 'admin', 'admin', ...);
  insert into user values (3, 'test', 'test', ...);
  insert into user values (4, 'dev', 'dev', ...);
  ```
- **Impact:** If this migration runs in production (or any shared environment), default accounts with trivially guessable credentials exist. Combined with the lack of authentication, any user can access these records and obtain the passwords.
- **Remediation:** Remove seed data from production migrations. If seed data is necessary for development, gate it behind a profile/environment check. Always use hashed passwords even in seed data.

---

### [HIGH] 8. Password Exposed in User.toString()

- **Category:** Credential Exposure
- **File(s):** `src/main/java/com/mycompany/entapp/snowman/domain/model/User.java:109`
- **Description:** The `toString()` method includes the `password` field, meaning any log statement, debug output, or exception message that prints a `User` object will leak the plaintext password.
- **Evidence:**
  ```java
  // User.java:106-113
  public String toString() {
      return new ToStringBuilder(this)
          .append("userId", userId)
          .append("username", username)
          .append("password", password)  // Line 109
          .append("email", email)
          .append("firstname", firstname)
          .append("lastname", lastname)
          .toString();
  }
  ```
- **Impact:** Passwords leak into application logs, stack traces, and debug output. Log aggregation systems (ELK, Splunk, Datadog) would store these in plaintext, potentially accessible to operations staff and attackers who gain log access.
- **Remediation:** Remove `password` from `toString()`. Consider also removing it from `equals()` and `hashCode()`.

---

### [HIGH] 9. Password Returned in User GET API Response

- **Category:** Data Exposure
- **File(s):**
  - `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/UserResource.java:11` (password field)
  - `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/mappers/UserResourceMapper.java:30`
  - `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/UserRestEndpoint.java:29-32`
- **Description:** The `GET /user/{userId}` endpoint returns the user's password in the JSON response. The `UserResourceMapper.mapUserToUserResource()` copies the password field to the DTO, and the `UserResource` DTO has a public `password` field with getter/setter.
- **Evidence:**
  ```java
  // UserResourceMapper.java:30
  userResource.setPassword(user.getPassword());
  ```
  The `UserResource.java` even has a TODO comment acknowledging the issue:
  ```java
  private String password; //TODO password shouldn't be raw string
  ```
- **Impact:** Any client calling `GET /user/{id}` receives the plaintext password. Combined with no authentication and sequential integer IDs (IDOR), an attacker can enumerate all user passwords.
- **Remediation:** Never return passwords in API responses. Use `@JsonIgnore` on the password field in `UserResource`, or remove it from the response mapper entirely.

---

### [HIGH] 10. No Authorization / Access Control (RBAC)

- **Category:** Authorization
- **File(s):** All endpoints under `src/main/java/.../infrastructure/rest/endpoint/` and `src/main/java/.../infrastructure/management/`
- **Description:** No role-based access control exists. There are no `@Secured`, `@PreAuthorize`, `@RolesAllowed` annotations, and no programmatic authorization checks. Any user (or anonymous caller) can invoke any operation.
- **Evidence:** Grep for RBAC annotations returns zero matches. The management endpoints (`/health`, `/cache/{name}/clear`) are equally unprotected.
- **Impact:** Any caller can delete employees, users, clients, and projects. Management operations like cache clearing can be used for denial-of-service.
- **Remediation:** Implement Spring Security with role-based access control. Restrict destructive operations (DELETE, cache clear) to admin roles. Protect management endpoints with a separate security realm or require elevated privileges.

---

### [HIGH] 11. Missing @RequestBody on POST Endpoints (Employee, User, Project)

- **Category:** Input Validation / Data Binding
- **File(s):**
  - `src/main/java/.../rest/endpoint/EmployeeRestEndpoint.java:36`
  - `src/main/java/.../rest/endpoint/UserRestEndpoint.java:36`
  - `src/main/java/.../rest/endpoint/ProjectRestEndpoint.java:39`
- **Description:** Multiple POST endpoints use `@Valid` but lack `@RequestBody`, causing Spring to use form parameter binding instead of JSON body parsing. Only `ClientRestEndpoint` correctly uses `@RequestBody`.
- **Evidence:**
  ```java
  // EmployeeRestEndpoint.java:36 — missing @RequestBody
  public ResponseEntity createEmployee(@Valid EmployeeResource employeeResource) { ... }

  // UserRestEndpoint.java:36 — missing @RequestBody
  public ResponseEntity createNewUser(@Valid UserResource userResource) { ... }

  // ProjectRestEndpoint.java:39 — missing @RequestBody
  public ResponseEntity<?> createProject(@Valid ProjectResource projectResource) { ... }
  ```
- **Impact:** Clients sending JSON bodies will find their data silently ignored (fields default to null/0). Form binding also opens the door to parameter pollution attacks and makes Content-Type validation impossible.
- **Remediation:** Add `@RequestBody` annotation before the resource parameter on all POST/PUT endpoints that accept JSON.

---

### [HIGH] 12. No Bean Validation Constraints on Any DTO

- **Category:** Input Validation
- **File(s):**
  - `src/main/java/.../rest/resources/UserResource.java`
  - `src/main/java/.../rest/resources/EmployeeResource.java`
  - `src/main/java/.../rest/resources/ClientResource.java`
  - `src/main/java/.../rest/resources/ProjectResource.java`
- **Description:** While endpoints use `@Valid`, none of the resource DTOs have any Bean Validation annotations (`@NotNull`, `@Size`, `@Pattern`, `@NotBlank`, `@Email`, etc.). The `@Valid` annotation is effectively a no-op.
- **Evidence:** Grep for `@NotNull|@Size|@Pattern|@NotBlank|@NotEmpty|@Min|@Max|@Email` returns zero matches in the entire source tree.
- **Impact:** No server-side input validation exists. Null values, empty strings, oversized inputs, and malformed data are all accepted without checking. This can lead to data corruption, unexpected exceptions, and potential injection attacks.
- **Remediation:** Add appropriate Bean Validation annotations to all DTO fields. Example: `@NotBlank @Size(max=20) private String username;`

---

### [HIGH] 13. No Global Exception Handler — Stack Trace Leakage

- **Category:** Error Handling / Information Disclosure
- **File(s):** Entire codebase (absence)
- **Description:** No `@ControllerAdvice` or `@ExceptionHandler` is defined anywhere. When exceptions occur (and they will — `UserDaoImpl.saveUser()` throws raw `RuntimeException`, `ClientRestEndpoint` wraps `SnowmanException` in `RuntimeException`), Spring's default error handling returns full stack traces in the HTTP response.
- **Evidence:**
  ```java
  // UserDaoImpl.java:49
  throw new RuntimeException("Not Yet Implemented");

  // ClientRestEndpoint.java:41
  throw new RuntimeException(e);
  ```
  Grep for `@ControllerAdvice|@ExceptionHandler` returns zero matches.
- **Impact:** Stack traces expose internal class names, package structure, library versions, database query details, and file paths — all valuable reconnaissance for attackers.
- **Remediation:** Implement a `@ControllerAdvice` class with `@ExceptionHandler` methods that return sanitized error responses (error code + generic message, no stack trace).

---

### [HIGH] 14. Insecure Direct Object References (IDOR) on All Entities

- **Category:** Authorization / Access Control
- **File(s):**
  - `src/main/java/.../rest/endpoint/EmployeeRestEndpoint.java:29` (`/employee/{employeeId}`)
  - `src/main/java/.../rest/endpoint/UserRestEndpoint.java:29` (`/user/{userId}`)
  - `src/main/java/.../rest/endpoint/ClientRestEndpoint.java:29` (`/client/{clientId}`)
  - `src/main/java/.../rest/endpoint/ProjectRestEndpoint.java:32` (`/project/{projectId}`)
- **Description:** All entity endpoints use sequential integer IDs with no authentication or ownership verification. An attacker can enumerate and access/modify/delete any entity by iterating through IDs.
- **Evidence:**
  ```java
  // UserRestEndpoint.java:29
  @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
  public ResponseEntity<UserResource> getUser(@PathVariable("userId") String userId) { ... }
  ```
  Combined with no authentication (Finding #4), any caller can access `GET /user/1`, `GET /user/2`, etc.
- **Impact:** Full enumeration and exfiltration of all users, employees, clients, and projects. Destructive operations (DELETE) can wipe all data.
- **Remediation:** Implement authentication, then add authorization checks verifying the caller has access to the requested resource. Consider using UUIDs instead of sequential integers to prevent easy enumeration.

---

### [HIGH] 15. End-of-Life Java 7 Target

- **Category:** Dependency / Platform Security
- **File(s):** `pom.xml:14`
- **Description:** The project targets Java 7 (`<java.version>1.7</java.version>`), which reached end of public updates in April 2015. Java 7 has numerous unpatched CVEs and lacks modern security features.
- **Evidence:**
  ```xml
  <java.version>1.7</java.version>
  ```
- **Impact:** The runtime is vulnerable to all Java CVEs published after April 2015. No security patches are available. Modern security features (TLS 1.3, stronger crypto defaults, module system restrictions) are unavailable.
- **Remediation:** Upgrade to at minimum Java 11 LTS (preferably Java 17 or 21 LTS). Update source/target compatibility and refactor any deprecated API usage.

---

### [HIGH] 16. Open-Ended Maven Version Ranges (Supply Chain Risk)

- **Category:** Supply Chain / Dependency Management
- **File(s):** `pom.xml:12,17,24`
- **Description:** Three critical dependencies use open-ended version ranges (`[x.y.z,)`) — meaning Maven will resolve to the *latest available version* at build time. Builds are non-reproducible and vulnerable to dependency confusion or malicious version injection.
- **Evidence:**
  ```xml
  <spring.version>[4.3.18,)</spring.version>           <!-- Line 12 -->
  <jetty.version>[9.4.11.v20180605,)</jetty.version>   <!-- Line 17 -->
  <jackson.databind.version>[2.8.11.1,)</jackson.databind.version>  <!-- Line 24 -->
  ```
- **Impact:** A compromised or malicious version published to Maven Central would be automatically pulled into builds. Different builds can use different versions, making vulnerability tracking impossible. An attacker who publishes a higher version number to a configured repository could inject malicious code.
- **Remediation:** Pin all dependency versions to exact values. Use a dependency management tool (Dependabot, Renovate) for controlled updates. Consider using a Maven BOM for Spring dependencies.

---

### [MEDIUM] 17. ActiveMQ 5.10.0 — Known Deserialization CVEs

- **Category:** Dependency Vulnerabilities
- **File(s):** `pom.xml:35`
- **Description:** ActiveMQ 5.10.0 is from 2014 and is affected by multiple critical CVEs.
- **Evidence:**
  ```xml
  <activemq-spring.version>5.10.0</activemq-spring.version>
  ```
  Known CVEs include:
  - **CVE-2015-5254**: Unsafe deserialization of ObjectMessage payloads allows Remote Code Execution.
  - **CVE-2016-3088**: Path traversal in the web console allows arbitrary file write.
  - **CVE-2023-46604**: Critical RCE via ClassPathXmlApplicationContext (CVSS 10.0).
- **Impact:** Remote Code Execution, arbitrary file write, and complete system compromise depending on the CVE exploited.
- **Remediation:** Upgrade to ActiveMQ 5.18.x or later (or migrate to ActiveMQ Artemis). Apply ClassFilter restrictions for deserialization.

---

### [MEDIUM] 18. No TLS/HTTPS — Cleartext HTTP Traffic

- **Category:** Transport Security
- **File(s):** `src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java:26`
- **Description:** The embedded Jetty server uses a plain `ServerConnector` with no SSL/TLS configuration. All traffic is transmitted in cleartext HTTP.
- **Evidence:**
  ```java
  final ServerConnector serverConnector = new ServerConnector(server);
  serverConnector.setPort(resolvePort());
  ```
  No `SslContextFactory`, no `HttpConfiguration.SecureRequestCustomizer`, no keystore configuration.
- **Impact:** All data in transit — including user credentials (plaintext passwords!), employee data, and client information — is visible to any network observer (man-in-the-middle attacks, network sniffing).
- **Remediation:** Configure Jetty with `SslContextFactory` and a TLS certificate. Redirect HTTP to HTTPS. Use TLS 1.2+ with strong cipher suites. In production, consider terminating TLS at a reverse proxy (nginx, HAProxy).

---

### [MEDIUM] 19. No Security Headers

- **Category:** Transport / Configuration Security
- **File(s):** Entire codebase (absence)
- **Description:** No security-related HTTP response headers are configured. There is no filter, interceptor, or configuration setting any of the standard security headers.
- **Evidence:** Grep for `X-Content-Type-Options|X-Frame-Options|Content-Security-Policy|Strict-Transport-Security` returns zero matches.
- **Impact:** The application is vulnerable to:
  - **Clickjacking** (no `X-Frame-Options`)
  - **MIME type sniffing attacks** (no `X-Content-Type-Options: nosniff`)
  - **Cross-site scripting** (no `Content-Security-Policy`)
  - **Protocol downgrade attacks** (no `Strict-Transport-Security`)
- **Remediation:** Add a servlet filter or Spring interceptor that sets security headers on all responses.

---

### [MEDIUM] 20. No CORS Configuration

- **Category:** Transport / Configuration Security
- **File(s):** Entire codebase (absence)
- **Description:** No CORS configuration exists — no `@CrossOrigin` annotations, no `CorsFilter`, no `WebMvcConfigurer.addCorsMappings()`.
- **Evidence:** Grep for `@CrossOrigin|CorsFilter|cors` returns zero matches.
- **Impact:** Without explicit CORS configuration, the default behavior depends on the servlet container and Spring version. The API may be either overly restrictive (blocking legitimate cross-origin requests) or overly permissive.
- **Remediation:** Explicitly configure CORS with allowed origins, methods, and headers appropriate for the deployment environment.

---

### [MEDIUM] 21. No CSRF Protection

- **Category:** Transport / Configuration Security
- **File(s):** Entire codebase (absence)
- **Description:** Without Spring Security, there is no CSRF token validation on state-changing POST/DELETE endpoints. Any website can craft requests to modify or delete data.
- **Evidence:** No Spring Security dependency means no `CsrfFilter` in the filter chain. The POST endpoints for creating/updating users, employees, clients, and projects have no CSRF protection.
- **Impact:** An attacker can embed malicious forms or JavaScript on any website that submits requests to the application on behalf of an authenticated user (if authentication is ever added). Currently, with no auth, CSRF is moot — but this becomes critical if authentication is implemented.
- **Remediation:** Implement Spring Security which provides CSRF protection by default. For REST APIs using token-based auth (e.g., JWT in headers), CSRF is less of a concern, but should still be explicitly configured.

---

### [MEDIUM] 22. Unauthenticated JMS Broker Connection

- **Category:** Configuration Security
- **File(s):**
  - `src/main/resources/application.properties:14`
  - `src/main/resources/META-INF/application-context-messaging.xml:8`
- **Description:** The ActiveMQ broker connection uses `tcp://localhost:61616` with no authentication credentials configured.
- **Evidence:**
  ```properties
  jms.brokerUrl=tcp://localhost:61616
  ```
  ```xml
  <bean id="amqConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
      <constructor-arg index="0" value="${jms.brokerUrl}"/>
  </bean>
  ```
  No username/password is set on the `ActiveMQConnectionFactory`.
- **Impact:** Any process on the network that can reach port 61616 can connect to the broker, produce messages, consume messages, and potentially inject malicious serialized objects (see Finding #6).
- **Remediation:** Configure ActiveMQ broker authentication. Pass credentials via the `ActiveMQConnectionFactory` constructor or properties. Use TLS for broker connections.

---

### [MEDIUM] 23. Hibernate show_sql=true in Production Configuration

- **Category:** Information Disclosure
- **File(s):** `src/main/resources/application.properties:9`
- **Description:** SQL logging is enabled, which outputs all executed SQL statements to logs.
- **Evidence:**
  ```properties
  hibernate.show_sql=true
  ```
- **Impact:** SQL statements may contain sensitive data (user information, query parameters). In production, this creates excessive log volume and exposes internal database schema to anyone with log access.
- **Remediation:** Set `hibernate.show_sql=false` in production. Use profile-based configuration (`application-prod.properties`) to control logging levels per environment.

---

### [MEDIUM] 24. Unsafe Error Handling — Null Connection Propagation

- **Category:** Error Handling
- **File(s):**
  - `src/main/java/.../db/dao/AbstractJDBCDao.java:35-44`
  - `src/main/java/.../db/dao/impl/ApplicationInfoDaoImpl.java:39-65`
  - `src/main/java/.../db/health/DBHealthCheck.java:25-57`
- **Description:** In `AbstractJDBCDao.getConnection()`, SQL exceptions are caught and only logged — the method returns `null` on failure. Callers like `ApplicationInfoDaoImpl` and `DBHealthCheck` then call `connection.createStatement()` on the null reference, causing a `NullPointerException`.
- **Evidence:**
  ```java
  // AbstractJDBCDao.java:35-44
  protected Connection getConnection() {
      Connection connection = null;
      try {
          connection = DriverManager.getConnection(...);
      } catch (SQLException e) {
          LOG.error("{}", e);
      }
      return connection;  // Returns null on failure
  }
  ```
- **Impact:** Database connection failures cascade into `NullPointerException` with full stack traces returned to the client (see Finding #13). This obscures the real error and leaks internal implementation details.
- **Remediation:** Throw a proper application exception from `getConnection()` instead of returning null. Create a `DatabaseConnectionException` that extends `SnowmanException`.

---

### [MEDIUM] 25. Mass Assignment Risk on Resource DTOs

- **Category:** Input Validation
- **File(s):**
  - `src/main/java/.../rest/resources/UserResource.java` (userId field settable)
  - `src/main/java/.../rest/resources/EmployeeResource.java` (employeeId field settable)
  - `src/main/java/.../rest/resources/ClientResource.java` (clientId field settable)
  - `src/main/java/.../rest/resources/ProjectResource.java` (projectId field settable)
- **Description:** All resource DTOs have setters for their ID fields (`userId`, `employeeId`, `clientId`, `projectId`). The mappers copy these IDs directly to domain objects. An attacker can set arbitrary IDs during creation or update to overwrite other entities.
- **Evidence:**
  ```java
  // UserResourceMapper.java:17
  user.setUserId(userResource.getUserId());
  ```
- **Impact:** An attacker can forge entity IDs during create/update operations to overwrite or impersonate other entities.
- **Remediation:** Use `@JsonIgnore` on ID fields for deserialization (input), or use separate DTOs for create vs. read operations where the create DTO lacks ID fields.

---

### [HIGH] 26. Use of java.sql.Statement (Potential SQL Injection Surface)

- **Category:** Injection
- **File(s):**
  - `src/main/java/.../db/dao/impl/ApplicationInfoDaoImpl.java:42`
  - `src/main/java/.../db/health/DBHealthCheck.java:32`
- **Description:** `ApplicationInfoDaoImpl` and `DBHealthCheck` use `java.sql.Statement` (not `PreparedStatement`) to execute queries. While the current queries use static strings and are not directly vulnerable, using `Statement` is a dangerous pattern that invites SQL injection when queries are modified to include parameters.
- **Evidence:**
  ```java
  // ApplicationInfoDaoImpl.java:42
  stmt = connection.createStatement();
  ResultSet rs = stmt.executeQuery(SELECT_FROM_APP_INFO_QUERY);

  // DBHealthCheck.java:32
  stmt = connection.createStatement();
  ResultSet rs = stmt.executeQuery(SELECT_MIN_1_FROM_APP_INFO);
  ```
  Note: `UserDaoImpl` correctly uses `JdbcTemplate` with parameterized queries (`?` placeholders).
- **Impact:** Currently no direct SQL injection because queries are static. However, this pattern is one developer change away from being exploitable. The use of `Statement` instead of `PreparedStatement` is a recognized insecure coding practice.
- **Remediation:** Replace `Statement` with `PreparedStatement` in all cases. Better yet, replace raw JDBC usage with Spring's `JdbcTemplate` (which uses `PreparedStatement` internally) as already done in `UserDaoImpl`.

---

### [LOW] 27. Travis CI Using End-of-Life JDK

- **Category:** CI/CD Security
- **File(s):** `.travis.yml:4`
- **Description:** The CI configuration uses `oraclejdk8`, but Travis CI has deprecated Oracle JDK support. The build command also skips Liquibase migrations.
- **Evidence:**
  ```yaml
  language: java
  install: "mvn clean install -U -Dliquibase.should.run=false"
  jdk:
     - oraclejdk8
  ```
- **Impact:** CI builds may fail or use an outdated JDK. The `-Dliquibase.should.run=false` flag means database migrations are never validated in CI.
- **Remediation:** Update to `openjdk11` or `openjdk17`. Consider adding security scanning (OWASP Dependency-Check, SpotBugs) to the CI pipeline.

---

### [LOW] 28. Maven Shade Plugin Strips JAR Signatures

- **Category:** Build Security
- **File(s):** `pom.xml:286-293`
- **Description:** The Maven Shade plugin is configured to exclude `META-INF/*.SF`, `*.DSA`, `*.RSA` files, which removes digital signatures from all bundled dependencies.
- **Evidence:**
  ```xml
  <excludes>
      <exclude>META-INF/*.SF</exclude>
      <exclude>META-INF/*.DSA</exclude>
      <exclude>META-INF/*.RSA</exclude>
  </excludes>
  ```
- **Impact:** JAR signature verification is disabled for all dependencies in the uber-jar. This is standard practice for shade plugins (signatures become invalid in the merged JAR) but means you lose the ability to verify dependency integrity at runtime.
- **Remediation:** This is acceptable for shade/uber-jar packaging. Compensate by using Maven Enforcer plugin with dependency signature verification during build, and hash-based verification in CI. Consider switching to Spring Boot's executable JAR format which preserves nested JARs.

---

### [LOW] 29. Outdated Dependency Versions with Known CVEs

- **Category:** Dependency Vulnerabilities
- **File(s):** `pom.xml` (various lines)
- **Description:** Multiple dependencies are outdated and have known security vulnerabilities.
- **Evidence:**

  | Dependency | Version | Notable CVEs |
  |---|---|---|
  | Hibernate Core | 5.4.24.Final | CVE-2020-25638 (SQL injection via literal handling) |
  | Hibernate EntityManager | 4.3.10.Final | Multiple CVEs, EOL version |
  | Hibernate C3P0 | 4.3.10.Final | CVE-2019-5427 (C3P0 XXE) |
  | MySQL Connector/J | 8.0.16 | CVE-2021-2471, CVE-2022-21363 |
  | HSQLDB | 2.3.2 | CVE-2022-41853 (RCE via allowedClassNames) |
  | H2 Database | 2.1.210 | CVE-2022-23221 (RCE via JDBC URL) |
  | Derby | 10.11.1.1 | Multiple CVEs, EOL |
  | EhCache | 2.9.1 | Outdated, EOL (replaced by Ehcache 3.x) |
  | Hibernate Validator | 5.4.2.Final | CVE-2020-10693 (bypass via EL injection) |
  | Jackson Databind | [2.8.11.1,) | Many CVEs in 2.8.x-2.9.x range (polymorphic deserialization) |
  | Mockito | 1.10.19 | Outdated (current is 5.x) |
  | PowerMock | 1.7.3 | Abandoned project |
  | Liquibase Maven Plugin | 3.0.5 | Ancient version (current is 4.x) |

- **Impact:** Each CVE represents a potential attack vector. The most critical are the RCE vulnerabilities in H2, HSQLDB, and Jackson Databind.
- **Remediation:** Update all dependencies to their latest stable versions. Run OWASP Dependency-Check in CI to catch future vulnerabilities. Remove unused database drivers (the project uses MySQL but ships H2, HSQLDB, and Derby).

---

### [INFO] 30. .gitignore Excludes .env but No .env File Is Committed

- **Category:** Configuration
- **File(s):** `.gitignore:41`
- **Description:** The `.gitignore` correctly excludes `.env` files. No `.env` file (other than `.env.example`) is committed to the repository.
- **Evidence:**
  ```gitignore
  # Environment files #
  #####################
  .env
  ```
  Git `ls-files` confirms no `.env` file is tracked.
- **Impact:** None — this is a positive finding. The `.env.example` file only contains placeholder values.
- **Remediation:** No action needed. This is correctly configured.

---

## Summary Table

| # | Severity | Category | File | Title |
|---|----------|----------|------|-------|
| 1 | Critical | Credential Exposure | `AbstractJDBCDao.java:24-25` | Hardcoded DB credentials in DAO |
| 2 | Critical | Credential Exposure | `application.properties:4-5` | Hardcoded DB credentials in properties |
| 3 | Critical | Credential Exposure | `liquibase.properties:3-4` | Hardcoded DB credentials in Liquibase config |
| 4 | Critical | Authentication | `pom.xml`, `web.xml` | Complete absence of authentication |
| 5 | Critical | Authentication | `UserDaoImpl.java:39`, schema | No password hashing — plaintext storage |
| 6 | Critical | Deserialization/RCE | `InvoiceSystemAdapter.java:38` | JMS ObjectMessage deserialization (RCE) |
| 7 | High | Credential Exposure | `004_Insert_Dummy_Users.xml:12-15` | Plaintext passwords in seed data |
| 8 | High | Credential Exposure | `User.java:109` | Password in toString() leaks to logs |
| 9 | High | Data Exposure | `UserResourceMapper.java:30` | Password returned in GET API response |
| 10 | High | Authorization | All endpoints | No RBAC / access control |
| 11 | High | Input Validation | Multiple endpoints | Missing @RequestBody on POST endpoints |
| 12 | High | Input Validation | All Resource DTOs | No Bean Validation constraints |
| 13 | High | Error Handling | Entire codebase | No global exception handler |
| 14 | High | Authorization | All entity endpoints | IDOR — sequential IDs, no auth |
| 15 | High | Platform Security | `pom.xml:14` | End-of-life Java 7 target |
| 16 | High | Supply Chain | `pom.xml:12,17,24` | Open-ended Maven version ranges |
| 17 | Medium | Dependencies | `pom.xml:35` | ActiveMQ 5.10.0 known CVEs |
| 18 | Medium | Transport | `EnterpriseApplication.java:26` | No TLS/HTTPS |
| 19 | Medium | Transport | Entire codebase | No security headers |
| 20 | Medium | Transport | Entire codebase | No CORS configuration |
| 21 | Medium | Transport | Entire codebase | No CSRF protection |
| 22 | Medium | Configuration | `application.properties:14` | Unauthenticated JMS broker |
| 23 | Medium | Information Disclosure | `application.properties:9` | Hibernate show_sql=true |
| 24 | Medium | Error Handling | `AbstractJDBCDao.java:35-44` | Null connection propagation |
| 25 | Medium | Input Validation | All Resource DTOs | Mass assignment risk |
| 26 | High | Injection | `ApplicationInfoDaoImpl.java:42` | Statement instead of PreparedStatement |
| 27 | Low | CI/CD | `.travis.yml:4` | Travis CI using EOL JDK |
| 28 | Low | Build Security | `pom.xml:286-293` | Shade plugin strips JAR signatures |
| 29 | Low | Dependencies | `pom.xml` (various) | Outdated deps with known CVEs |
| 30 | Info | Configuration | `.gitignore:41` | .env correctly excluded (positive) |

---

## Recommendations (Prioritized)

1. **[P0] Add Authentication & Authorization:** Integrate Spring Security immediately. Configure authentication (JWT or session-based) and role-based access control for all endpoints. This single change addresses Findings #4, #10, #14, and #21.

2. **[P0] Implement Password Hashing:** Use bcrypt (via Spring Security's `BCryptPasswordEncoder`) for all password storage. Increase the `password` column size. Remove password from API responses and `toString()`. Addresses Findings #5, #7, #8, #9.

3. **[P0] Externalize All Credentials:** Remove all hardcoded credentials from source code. Use environment variables, Spring profiles, or a secrets manager. Addresses Findings #1, #2, #3.

4. **[P0] Eliminate JMS ObjectMessage Deserialization:** Replace `ObjectMessage` with `TextMessage` + JSON serialization in `InvoiceSystemAdapter`. Upgrade ActiveMQ to 5.18+. Addresses Findings #6, #17.

5. **[P1] Pin Dependency Versions:** Replace all open-ended version ranges with exact versions. Set up Dependabot or Renovate for controlled updates. Addresses Finding #16.

6. **[P1] Upgrade Java Version:** Migrate from Java 7 to Java 17 or 21 LTS. Addresses Finding #15.

7. **[P1] Add Input Validation:** Add `@RequestBody` to all POST endpoints. Add Bean Validation constraints to all DTOs. Implement a `@ControllerAdvice` global exception handler. Addresses Findings #11, #12, #13, #25.

8. **[P1] Enable TLS/HTTPS:** Configure Jetty with SSL or deploy behind a TLS-terminating reverse proxy. Add security response headers. Addresses Findings #18, #19.

9. **[P2] Update All Dependencies:** Upgrade all dependencies to current stable versions. Remove unused database drivers (H2, HSQLDB, Derby if only MySQL is used). Run OWASP Dependency-Check in CI. Addresses Finding #29.

10. **[P2] Modernize CI/CD:** Update `.travis.yml` to a supported JDK. Add security scanning (OWASP Dependency-Check, SpotBugs, FindSecBugs) to the build pipeline. Addresses Finding #27.
