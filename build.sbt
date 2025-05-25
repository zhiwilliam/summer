ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.4"

lazy val root = (project in file("."))
  .settings(
    name := "mlt-layer"
  )
ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.6.1",
  "org.typelevel" %% "cats-mtl"    % "1.5.0",
  "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % "provided"
)

ThisBuild / scalacOptions ++= Seq(
  "-Yretain-trees",
  "-language:experimental.macros",
  "-deprecation",
  "-feature",
  "-experimental",
  "-Xcheck-macros",
  "-source:future", // 启用 Scala 3.6 的未来特性（可选）
  "-Xprint:parser"  // 打印宏展开后的代码
)

