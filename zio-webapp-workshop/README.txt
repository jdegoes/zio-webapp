# Build Web Servers with ZIO 2.0

One of the main use cases for the Scala programming language is building web applications, which power mobile apps and browser apps. The next-generation ZIO library for building modern applications presents many opportunities for rapidly constructing scalable, resilient web apps. Yet knowing how to properly structure and create a ZIO web app, complete with configuration, logging, metrics, monitoring, and persistence, can be daunting.

In this course, developers will use ZIO and best-in-breed libraries to rapidly construct web applications that are scalable; easy to test, refactor, and maintain; and which never leak resources or deadlock. Over the course of the workshop, developers will get a chance to build their very own web app, following along with the instructor, and by the time the course ends, developers will have newfound confidence to build web apps with ZIO according to industry best practices.

### Who Should Attend

Scala developers who wish to build modern web apps on the ZIO ecosystem.

### Prerequisites

Knowledge of Scala is required, and basic knowledge of ZIO is recommended.

### Topics

 - Web services & microservices
 - Large uploads & downloads
 - Persistence
 - Authentication & authorization
 - Logging
 - Configuration
 - Metrics & monitoring
 - Deployment

# Usage

## From the UI

1. Download the repository as a [zip archive](https://github.com/jdegoes/zio-webapp/archive/master.zip).
2. Unzip the archive, usually by double-clicking on the file.
3. Configure the source code files in the IDE or text editor of your choice.

## From the Command Line

1. Open up a terminal window.

2. Clone the repository.

    ```bash
    git clone https://github.com/jdegoes/zio-webapp
    ```
5. Launch project provided `sbt`.

    ```bash
    cd zio-webapp; ./sbt
    ```
6. Enter continuous compilation mode.

    ```bash
    sbt:zio-webapp> ~ test:compile
    ```

Hint: You might get the following error when starting sbt:

> [error] 	typesafe-ivy-releases: unable to get resource for com.geirsson#sbt-scalafmt;1.6.0-RC4: res=https://repo.typesafe.com/typesafe/ivy-releases/com.geirsson/sbt-scalafmt/1.6.0-RC4/jars/sbt-scalafmt.jar: javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested targe

It's because you have an outdated Java version, missing some newer certificates. Install a newer Java version, e.g. using [Jabba](https://github.com/shyiko/jabba), a Java version manager. See [Stackoverflow](https://stackoverflow.com/a/58669704/1885392) for more details about the error.

# Legal

Copyright&copy; 2021 John A. De Goes. All rights reserved.

