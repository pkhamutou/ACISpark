ThisBuild / description := "Alchemist-Client Interface for Apache Spark"
ThisBuild / organization := "alchemist"
ThisBuild / version := "0.5"
ThisBuild / scalaVersion := "2.11.12"
ThisBuild / autoStartServer := false

lazy val SparkVersion      = "2.3.3"
lazy val SparkTestVersion  = s"${SparkVersion}_0.12.0"
lazy val EnumeratumVersion = "1.5.13"
lazy val ScodecVersion     = "1.11.4"
lazy val CatsCoreVersion   = "1.6.0"
lazy val CatsEffectVersion = "1.3.1"
lazy val ScalaLogging      = "3.9.2"
lazy val ScoptVersion      = "3.7.1"
lazy val ScalaTestVersion  = "3.0.7"

lazy val runSpark = inputKey[Unit]("Run Spark application from sbt shell")

lazy val testSettings = Seq(
  parallelExecution in Test := false,
  fork in Test := true,
  test in assembly := {},
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
)

lazy val itSettings =
  inConfig(IntegrationTest)(Defaults.testSettings ++ org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)

lazy val compilerSettings = Seq(
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8",
    "-Xms512M",
    "-Xmx2048M",
    "-XX:MaxPermSize=2048M",
    "-XX:+CMSClassUnloadingEnabled"
  ),
  scalacOptions ++= Seq(
    "-encoding",
    "utf-8",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Ypartial-unification",
    "-language:higherKinds"
  )
)

lazy val ItTest = "it,test"

lazy val fmtSettings = Seq(
  scalafmtOnCompile := false,
  scalastyleConfig := file("project/scalastyle_config.xml")
)

lazy val `alchemist-core` = (project in file("modules/core"))
  .configs(IntegrationTest)
  .settings(testSettings, compilerSettings, fmtSettings, itSettings)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.spark"           %% "spark-mllib"        % SparkVersion % Provided,
      "com.beachape"               %% "enumeratum"         % EnumeratumVersion,
      "org.scodec"                 %% "scodec-core"        % ScodecVersion,
      "org.typelevel"              %% "cats-core"          % CatsCoreVersion,
      "org.typelevel"              %% "cats-effect"        % CatsEffectVersion,
      "com.typesafe.scala-logging" %% "scala-logging"      % ScalaLogging,
      "org.scalatest"              %% "scalatest"          % ScalaTestVersion % ItTest,
      "com.holdenkarau"            %% "spark-testing-base" % SparkTestVersion % Test
    )
  )

lazy val `alchemist-example` = (project in file("modules/example"))
  .settings(testSettings, compilerSettings, fmtSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-mllib" % SparkVersion % Provided,
      "com.github.scopt" %% "scopt"       % ScoptVersion
    ),
    run in Compile := Defaults
      .runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
      .evaluated,
    runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in (Compile, run)).evaluated
  )
  .settings(
    runSpark := {
      import sbt.complete.Parsers.spaceDelimited

      val path                   = assembly.value.getAbsolutePath
      val args                   = spaceDelimited("<args>").parsed.toList
      val (className, arguments) = args.splitAt(1)

      SparkRunner.runSpark(SparkVersion, scalaVersion.value)(className.head, arguments, path)
    }
  )
  .dependsOn(`alchemist-core`)

lazy val alchemist = (project in file("."))
  .settings(fmtSettings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(`alchemist-core`, `alchemist-example`)
