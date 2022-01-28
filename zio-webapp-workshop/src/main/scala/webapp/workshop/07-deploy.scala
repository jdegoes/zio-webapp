/**
 * DEPLOYMENT
 *
 * The cloud transformation has completely changed the way applications are
 * packaged and deployed. Instead of shipping a JAR to a Jetty web server,
 * today's applications are packaged up into containers, which are deployed to
 * container runtimes, often managed by container orchestration technologies.
 *
 * The realities of cloud deployment impose certain restrictions on the way that
 * web applications are architectured, developed, configured, and run.
 *
 * In this section, you will learn how to develop web applications with ZIO that
 * are truly cloud-native, designed natively for cloud deployment. Along the
 * way, you will see which ZIO technologies can simplify the process.
 */
package webapp.workshop

import zio.test._

object Deploy {

  /**
   *   1. CONTAINERS
   *
   * Plan to package your application in a container.
   *
   * Docker is widely used for this purpose, but is not the only tool.
   *
   * Kubernetes-compatible container runtimes include the following:
   *
   *   - containerd
   *   - CRI-O
   *   - Docker Engine
   *   - Mirantis Container Runtime
   *
   * For Docker, there is an SBT plug-in that can help you build an image that
   * can run your application:
   *
   * https://www.scala-sbt.org/sbt-native-packager/formats/docker.html
   *
   * Consider using ZIO K8S for programmatic control of Kubernetes.
   */

  /**
   * 2. METRICS
   *
   * Plan to expose metrics from your application to Prometheus, Statsd, or
   * other widely supported monitoring tools.
   *
   * Metrics for web servers should include:
   *
   *   - Total requests, requests per second
   *   - Request durations
   *   - Response status codes
   *   - Failure breakdown by type (fatal & non-fatal)
   *   - Breakdowns of failure, request duration, and response duration by
   *     endpoint
   *
   * Plus standard JVM metrics around threads, garbage collection, memory.
   *
   * Consider using ZIO ZMX for integrations with Prometheus, Stats, DataDog /
   * NewRelic (Coming Soon), etc.
   */

  /**
   * 3. CONFIGURATION
   *
   * Instead of hard-coding data, plan to read configuration that is stored
   * outside your application.
   *
   * In particular, due to the way container runtimes work, plan on a two-
   * pronged approach to configuration:
   *
   *   - Bootstrap configuration - tells app where to find primary config
   *   - Primary configuration - broken down into general & confidential
   *
   * Primary configuration should take advantage of host support for
   * configuration:
   *
   *   - Docker Config
   *   - Kubernetes ConfigMap
   *
   * Authentication configuration, private keys, TLS certificates & keys, etc.,
   * should never be stored with the rest of application configuration. Instead,
   * sensitive details should be isolated and protected using:
   *
   *   - Docker secrets
   *   - Kubernetes secrets
   *
   * Consider using ZIO Config for accessing all configuration.
   */

  /**
   * 4. LOGGING
   *
   * Unless you have very good reasons for doing otherwise (e.g. multiple
   * processes in one container), plan to log to standard output / standard
   * error, to make it easier to deal with log storage using the container
   * runtime.
   *
   * Consider the use of structured logging (e.g. JSON), and for web
   * applications, be sure to capture:
   *
   *   - Path, method, remote IP
   *   - Major request headers (content length)
   *   - Trace / correlation id, session id, etc.
   *   - Response status code
   *   - All failures
   *
   * Consider using ZIO Logging to capture these details and log the output in a
   * structured format that can be digested using appropriate log search and
   * analysis tools (e.g. Elasticsearch, Splunk).
   */

  /**
   * 5. ARCHITECTURE
   *
   * Some architectures make it easier to deploy web apps than others. In
   * particular, plan to adopt a stateless architecture, which makes no
   * assumptions on how many instances of your web app are running or which
   * instance will satisfy which incoming request.
   *
   * All state should be pushed to external services, such as databases, caches,
   * message queues, etc.
   *
   * Consider using ZIO Redis, ZIO Kafka, ZIO SQL, ZIO Quill, and other
   * libraries to push state outside the application.
   */

  /**
   * 6. SECURITY
   *
   * This topic is too broad to be covered here, but a few key things to look
   * out for when building a web app:
   *
   *   - NEVER store or process user passwords (salt + hash, etc.)
   *   - Mandate SSL for all HTTP connections
   *   - Study Cross Site Request Forgery (CSRF) attacks
   *   - Enscure "secrets" are stored appropriately
   *   - Protect against SQL injection, JSON exploits, etc.
   *
   * Consider using ZIO Quill, ZIO SQL, for protection against SQL injection,
   * and ZIO JSON for protection against various DoS exploits.
   */
}
