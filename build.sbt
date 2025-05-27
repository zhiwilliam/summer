ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "mlt-layer"
  )
ThisBuild / libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % "0.23.30",
  "org.http4s" %% "http4s-dsl"          % "0.23.30",
  "org.http4s" %% "http4s-circe"        % "0.23.30",
  "io.circe"   %% "circe-generic"       % "0.14.13",
  "org.tpolecat" %% "doobie-core"       % "1.0.0-RC9",
  "org.tpolecat" %% "doobie-postgres"   % "1.0.0-RC9",
  "org.tpolecat" %% "doobie-hikari"     % "1.0.0-RC9",
  "com.github.jwt-scala" %% "jwt-circe" % "10.0.4",
  "org.typelevel" %% "cats-effect"      % "3.6.1",
  "org.typelevel" %% "log4cats-slf4j"   % "2.7.0",
  "com.github.jwt-scala" %% "jwt-circe" % "10.0.4",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
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

