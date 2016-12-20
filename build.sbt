name := "DataMiningContest"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "com.github.scopt" %% "scopt" % "3.4.0",
  "com.typesafe.play" %% "play-json" % "2.5.4",
  "org.apache.spark" %% "spark-core" % "2.0.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.0.2" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.0.2" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.0.2" % "provided",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "org.scalatest" %% "scalatest" % "3.0.1"
)

resolvers ++= Seq(
  "Dl Repo" at "https://dl.bintray.com/typesafe/ivy-releases/",
  "Dl maven Repo" at "https://dl.bintray.com/typesafe/maven-releases/",
  "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",

  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("plugin")
)