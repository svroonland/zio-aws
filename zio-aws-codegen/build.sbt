val zioVersion = "1.0.1"
val awsVersion = "2.15.19"

sbtPlugin := true
organization := "io.github.vigoo"
scalacOptions := Seq("-Ypartial-unification", "-deprecation")
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-nio" % "1.0.0-RC9",
  "io.circe" %% "circe-yaml" % "0.13.1",
  "software.amazon.awssdk" % "codegen" % awsVersion,
  "software.amazon.awssdk" % "aws-sdk-java" % awsVersion,
  "org.scalameta" %% "scalameta" % "4.3.21",
  "com.lihaoyi" %% "os-lib" % "0.7.1",
  "dev.zio" %% "zio-json" % "0.0.0+70-cb7ff4b9-SNAPSHOT"
)
