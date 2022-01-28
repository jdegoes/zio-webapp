/**
 * ARCHITECTURE
 *
 * Best-practice architecture is not one-size-fits-all, but rather, emerges from
 * the confluence of application domain, business domain, programming paradigm,
 * language, and technology requirements and limitations.
 *
 * If you're building a ZIO 2.0 web application according to best practices,
 * then you will probably benefit by adopting ZIO-specific architecture.
 *
 * In this section, you will learn about this architecture by working a small
 * scale, but realistic example.
 */
package webapp.workshop

import zio._
import zhttp.http._

object Arch {
  //
  // BACKGROUND
  //

  /**
   * The Pet Store application is a fictional web application that sells pets.
   * In addition to the "todo app", it's considered a canonical example of a
   * minimum viable application. Some variation of the pet store has been
   * created in many different programming languages and frameworks.
   *
   * Older versions of the pet store were designed prior to modern cloud-based
   * architectures, and some featured server-side session management, server-
   * side generation of HTML, and other less-favored approaches to web app
   * development.
   *
   * For the exercises in this section, study the Java Pet Store 2.0 application
   * (created in 2007), and collect business requirements.
   */

  // https://www.oracle.com/technical-resources/articles/javaee/pet-store-application.html

  /**
   * SERVICES
   *
   * In architecting our own version of the pet store application, we will rely
   * heavily on the ZIO concept of "services". Distinguished from data, which is
   * modeled using functional programming (algebraic data types), services are
   * inherently object-oriented, and are represented in ZIO with traits, whose
   * implementations are concrete providers of the functionality described by
   * those services. Canonical ZIO services are traits whose methods all return
   * ZIO effects.
   *
   * The design of services can benefit greatly from classic OOP design
   * methodology, including SOLID. However, it is not a science, and there is no
   * substitute for having experience with designing multiple systems over many
   * years with object-oriented techniques.
   *
   * Services are imperative DSLs, layers in the onion architecture that allow
   * us to express higher-level concerns without having to worry about
   * lower-level concerns.
   *
   * General guidelines to deconstructing applications into services include the
   * following:
   *
   *   - Each service should be atomic, the smallest unit of functionality
   *     required to provide the functionality of the service (but not so small
   *     that any practical application concern would always have to use the
   *     service together with another one in order to solve a problem).
   *
   *   - Each service should not depend on domain models from other services.
   *     While sometimes useful, "common domain models", used across multiple
   *     services, reduce code duplication but at the cost of tangling. Instead,
   *     generally prefer services to have their own domain models, even if this
   *     results in duplication across similar data types. For example, an
   *     authentication service should have its own notion of what a user is,
   *     catered to the needs of authentication, and it should not need to know
   *     that in another part of the application, users have lists of friends.
   *
   *   - Services should be non-leaky. It should not be necessary to bypass a
   *     service in order to solve some problem in that domain. Services can and
   *     do grow over time, so they can accomodate new functionality.
   *
   *   - In deciding what constitutes a service, it is helpful to consider the
   *     requirements of all methods of the service. If they all require the
   *     same infrastructure, the same state, the same dependencies, then they
   *     probably do belong to one logical service unit. On the other hand, if
   *     they fall into clusters, it can be a sign to further decompose.
   *
   *   - In ZIO, service implementations are classes, which take their
   *     dependencies via constructors. Each constructor will have a layer,
   *     which describes how to build the service (including initialization and
   *     finalization).
   *
   *   - ZIO service implementations should only depend on service definitions
   *     (traits), never on concrete services. The exception for this is
   *     "OS-level" services, which represent leaf services, providing the
   *     functionality at the outer edge of the "onion".
   *
   *   - Service definitions should be separated and located in a different
   *     compilation unit from service implementations, ideally such that
   *     service implementations do not depend on the implementations of other
   *     services. Typically this means each implementation lives in its own
   *     compilation project, although you could combine all service definitions
   *     into a single compilation unit (however, there is a danger they will
   *     start to become coupled, typically by sharing domain models).
   *
   *   - If a service can only ever have one possible implementation, and if
   *     there is no need for the service to have a test or mock implementation
   *     in order to test it (because its dependencies themselves have test or
   *     mock implementations), then it's a good sign you are working at the
   *     center of the onion. In this case, it is a mistake to create a service,
   *     because it adds extra ceremony without benefit. Instead, express your
   *     business logic directly in terms of the high-level services that it
   *     requires. If you need classes to organize the business logic or to
   *     create a functional domain, then feel free to use them. However, for a
   *     web app, your logic can go inside route handlers, if sufficiently
   *     simple and high-level.
   */

  /**
   * EXERCISE
   *
   * Come up with a list of services for the "Pet Store" application. For every
   * service, ask yourself the following question:
   *
   *   - Can you implement this service directly, possibly in terms of "leaf
   *     services"? If the gap seems too large, or if you would be writing code
   *     that mixes different levels of abstraction, then consider which
   *     services you could create, which would make the task of implementing
   *     this service easier.
   *
   *   - Can you make split this service into multiple services, or is the
   *     functionality exposed by the service truly atomic?
   */

  /**
   * EXERCISE
   *
   * Sketch out traits for each of the services that you came up with. For every
   * trait, create the type of a layer that reflects the dependencies you would
   * expect the production implemetnation of that service to require.
   */

  /**
   * EXERCISE
   *
   * Think carefully about configuration for services that interact with the
   * outside world, or which otherwise have parameters that might need to be
   * refined by the business or by ops. In general, it is not a good idea to
   * hard-wire constants into your application. Below, define some of the
   * configuration data type you expect to be used by your application.
   */

  /**
   * EXERCISE
   *
   * Design both the web API for your application, together with its types: in
   * particular, use the `R` environment of your `Http` to carry the inner-most
   * services that will be required by your business logic, which will itself
   * live inside your `Http` definitions.
   */
  lazy val petStoreApp = TODO
}
