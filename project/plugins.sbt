/*
 *  This file is part of the nextbeat services.
 *
 *  For the full copyright and license information,
 *  please view the LICENSE file that was distributed with this source code.
 */

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository.
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.6.0")
