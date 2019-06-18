# IxiaS

IxiaS is a development platform using scala languages.

# Getting Started

Just add IxiaS, a JDBC driver, and a slf4j implementation to your sbt build settings:

``` scala
resolvers ++= Seq(
  "IxiaS Releases" at "http://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/releases"
)

libraryDependencies ++= Seq(
  "net.ixias" %% "ixias"      % "1.1.11",
  "net.ixias" %% "ixias-aws"  % "1.1.11",
  "net.ixias" %% "ixias-play" % "1.1.11",
  "mysql"          % "mysql-connector-java" % "5.1.+",
  "ch.qos.logback" % "logback-classic"      % "1.1.+"
)
```

# NOTE

The quick start document is not ready. coming soon...
