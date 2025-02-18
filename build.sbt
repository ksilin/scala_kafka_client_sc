// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "example.com",
    organizationName := "ksilin",
    maintainer       := "konstantin.silin@gmail.com",
    version          := "0.0.1",
    startYear        := Some(2024),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "2.13.14",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _) => MergeStrategy.discard
      case _                            => MergeStrategy.first
    },
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-Ywarn-unused:imports",
      // "-Xfatal-warnings",
    ),
    scalafmtOnCompile := true,
    dynverSeparator   := "_", // the default `+` is not compatible with docker tags
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven",
      "ksqlDb" at "https://ksqldb-maven.s3.amazonaws.com/maven",
      Resolver.bintrayRepo("wolfendale", "maven"),
      Resolver.mavenLocal,
      "jitpack" at "https://jitpack.io"
    ),
    Test / fork := true, // required for setting env vars
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val scala_kafka_client_sc =
  project
    .in(file("."))
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.clients,
        library.kafka,
        library.protoSerde,
        library.betterFiles,
        library.config,
        library.srEncryptionAws,
        library.airframeLog,
        library.logback,
        "org.apache.httpcomponents" % "httpcore" % "4.4.13",
        library.scalatest           % Test
      ),
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val kafka       = "3.7.1"
      val confluent   = "7.6.1"
      val betterFiles = "3.9.2"
      val airframeLog = "24.7.1"
      val config      = "1.4.2"
      val gson        = "2.11.0"
      val circeKafka  = "3.3.1"
      val circe       = "0.14.9"
      val picoCli     = "4.7.6"
      val logback     = "1.5.6"
      val scalatest   = "3.2.19"
    }

    val clients     = "org.apache.kafka"      % "kafka-clients"             % Version.kafka
    val kafka       = "org.apache.kafka"     %% "kafka"                     % Version.kafka
    val protoSerde  = "io.confluent"          % "kafka-protobuf-serializer" % Version.confluent
    val betterFiles = "com.github.pathikrit" %% "better-files"              % Version.betterFiles
    val airframeLog = "org.wvlet.airframe"   %% "airframe-log"              % Version.airframeLog
    val config      = "com.typesafe"          % "config"                    % Version.config
    val srEncryptionAws =
      "io.confluent" % "kafka-schema-registry-client-encryption-aws" % Version.confluent
    val logback   = "ch.qos.logback" % "logback-core" % Version.logback
    val scalatest = "org.scalatest" %% "scalatest"    % Version.scalatest
  }

Compile / PB.targets := Seq(
  // scalapb.gen() -> (Compile / sourceManaged).value / "scalapb",
  PB.gens.java                        -> (sourceManaged in Compile).value,
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
)
