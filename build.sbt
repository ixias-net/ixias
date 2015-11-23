/*
 * This file is part of the ixias service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

organization := "net.ixias"
name         := "ixias-play2-auth"
scalaVersion := "2.11.7"

publishMavenStyle := false
publishTo         := Some("IxiaS Snapshots" at "s3://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/releases")

resolvers := ("Atlassian Releases"             at "https://maven.atlassian.com/public/") +: resolvers.value
resolvers += "scalaz-bintray"                  at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "IxiaS Snapshots"                 at "s3://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/releases"
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "net.ixias" %% "ixias-core" % "1.0.+",
  ws,
  cache,
  specs2 % Test
)

lazy val root = (project in file("."))

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
