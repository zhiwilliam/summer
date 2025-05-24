ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

lazy val root = (project in file("."))
  .settings(
    name := "zio-like-framework",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"      % "2.13.0",
      "org.typelevel" %% "cats-effect"    % "3.6.1",
      "org.typelevel" %% "cats-mtl"       % "1.5.0",   // MTL 类型类
      "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
    ),
    scalacOptions ++= Seq(
      "-Ymacro-annotations", // 如果你将来要用 macro-paradise
      "-language:higherKinds",
      "-deprecation", "-feature", "-unchecked",
      "-P:kind-projector:underscore-placeholders"
    )
  )
