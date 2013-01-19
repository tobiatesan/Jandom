name := "Jandom"

version := "0.1.1"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scalala" % "scalala_2.9.2" % "1.0.0.RC3-SNAPSHOT",
  "org.scalanlp" %% "breeze-math" % "0.2-SNAPSHOT"
)

resolvers ++= Seq(
   "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies <+= scalaVersion { "org.scala-lang" % "scala-swing" % _ }

fork  := true
