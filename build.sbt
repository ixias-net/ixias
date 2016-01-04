/*
 * This file is part of the ixias service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

organization := "net.ixias"
name         := "ixias-core"
scalaVersion := "2.11.7"

// build mode
val branch  = "git branch".lines_!.find{_.head == '*'}.map{_.drop(2)}.getOrElse("")
val release = (branch == "master")

// setting for resolvers
resolvers += "Atlassian Releases"   at "https://maven.atlassian.com/public/"
resolvers += "scalaz Release"       at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Release" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  // --[ OSS Libraries ]------------------------------------
  "joda-time"           % "joda-time"            % "2.9.1",
  "org.joda"            % "joda-convert"         % "1.7",
  "org.scala-lang"      % "scala-reflect"        % scalaVersion.value,
  "org.scalaz"         %% "scalaz-core"          % "7.1.3",
  "com.typesafe"        % "config"               % "1.3.0",
  "com.typesafe.slick" %% "slick"                % "3.0.2",
  "com.zaxxer"          % "HikariCP"             % "2.4.1",
  "com.bionicspirit"   %% "shade"                % "1.7.1",
  "mysql"               % "mysql-connector-java" % "latest.integration",

  // --[ UnitTest ]-----------------------------------------
  "ch.qos.logback"      % "logback-classic"      % "1.0.9" % Test,
  "org.specs2"         %% "specs2-core"          % "3.6.4" % Test,
  "org.specs2"         %% "specs2-matcher-extra" % "3.6.4" % Test
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

// Release
import ReleaseTransformations._
publishTo := {
  val path = if (release) "releases" else "snapshots"
  Some("Nextbeat snapshots" at "s3://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/" + path)
}
publishArtifact in (Compile, packageDoc) := false // disable publishing the main API jar
publishArtifact in (Compile, packageSrc) := false // disable publishing the main sources jar
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
