/*
 *  This file is part of the nextbeat services.
 *
 *  For the full copyright and license information,
 *  please view the LICENSE file that was distributed with this source code.
 */

// The Typesafe repository.
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play"  % "sbt-plugin"         % "2.6.0-RC2")

addSbtPlugin("com.scalapenos"     % "sbt-prompt"         % "0.2.1")

addSbtPlugin("com.github.gseitz"  % "sbt-release"        % "1.0.0")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.6.0")
