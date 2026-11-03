# Changes applied after implementation review

- Added tenant-qualified user lookup and updated JWT authentication filter to resolve the user using email + tenant_id from the signed token.
- Added explicit conflict and not-found exceptions and corrected HTTP error responses.
- Removed the hard-coded JWT secret fallback. Configure `JWT_SECRET` before running the application.
- Disabled the H2 console by default; it is available only with the `dev` profile.
- Prevented the JWT filter from being registered twice (servlet container and Spring Security chain).
- Added README documentation covering architecture, isolation, JWT payload, filter flow, endpoint roles, run steps and submission checklist.
- Added service, controller, JWT and TenantContext unit tests.
- Added JaCoCo report generation to Maven (`target/site/jacoco/index.html`).

## Important verification note
The Maven wrapper could not download Maven in the build environment because the Maven Central download was unavailable. Therefore the project has not been build- or test-verified here. Run `mvnw.cmd clean test` on Windows (or `./mvnw clean test` on macOS/Linux), inspect the JaCoCo report, and add further tests until overall coverage is confirmed at 80% or higher. The required five-minute explanation video is a separate recording and is not included in this source ZIP.
