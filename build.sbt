/*
 *  This file is part of the ixias service.
 *
 *  For the full copyright and license information,
 *  please view the LICENSE file that was distributed with this source code.
 */

name         := """net-ixias"""
version      := "1.0"
scalaVersion := "2.11.7"

resolvers := ("Atlassian Releases"             at "https://maven.atlassian.com/public/") +: resolvers.value
resolvers += "scalaz-bintray"                  at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.scala-lang"      % "scala-reflect"        % scalaVersion.value,
  "org.scalaz"         %% "scalaz-core"          % "7.1.3",
  "com.typesafe"        % "config"               % "1.3.0",
  "com.typesafe.slick" %% "slick"                % "3.0.2",
  "com.zaxxer"          % "HikariCP"             % "2.4.1",
  "mysql"               % "mysql-connector-java" % "latest.integration",
  "joda-time"           % "joda-time"            % "2.8",
  "org.joda"            % "joda-convert"         % "1.7",
  "ch.qos.logback"      % "logback-classic"      % "1.0.9",
  "org.specs2"         %% "specs2-core"          % "3.6.4" % "test",
  "org.specs2"         %% "specs2-matcher-extra" % "3.6.4" % "test"
)

scalacOptions ++= Seq(
  "-deprecation",            // Emit warning and location for usages of deprecated APIs.
  "-feature",                // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings",        // Fail the compilation if there are any warnings.
  "-Xlint",                  // Enable recommended additional warnings.
  "-Ywarn-adapted-args",     // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code",        // Warn when dead code is identified.
  "-Ywarn-inaccessible",     // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen"     // Warn when numerics are widened.
)
