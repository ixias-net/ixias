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
resolvers += "Sonatype snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "scalaz-bintray"      at "http://dl.bintray.com/scalaz/releases"

// The Github repository.
resolvers += Resolver.url("GitHub", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.sonatypeRepo("releases")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")
