/*
 * This file is part of the ixias service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

organization := "net.ixias"
name         := "ixias-play2-auth"
scalaVersion := "2.11.7"

// build mode
val branch  = "git branch".lines_!.find{_.head == '*'}.map{_.drop(2)}.getOrElse("")
val release = (branch == "master")

// setting for resolvers
resolvers += "scalaz Releases"  at "https://dl.bintray.com/scalaz/releases"
resolvers += "sonatype Release" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "IxiaS Snapshots"  at "http://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/snapshots"
resolvers += "IxiaS Releases"   at "http://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/releases"

// required libraries
libraryDependencies ++= Seq(cache,
  // --[ Libraries ]--------------
  "net.ixias"       %% "ixias-core"           % (if (release) "1.0.5" else "1.0.6-SNAPSHOT"),
  // --[ Libraries for Test ]-----
  "ch.qos.logback"   % "logback-classic"      % "1.0.9" % Test,
  "org.specs2"      %% "specs2-core"          % "3.6.4" % Test,
  "org.specs2"      %% "specs2-matcher-extra" % "3.6.4" % Test
)

// scala compile options
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

// setting for project
lazy val root = (project in file(".")).enablePlugins(PlayScala)
unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "scala"

// setting for publisher
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
