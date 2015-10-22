lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .settings(
    name := """employees-scala-play-scalikejdbc""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      "sonatype releases" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play"    %% "anorm" % anormVersion,
      "com.h2database"       %  "h2"                            % h2Version,
      "org.json4s"           %% "json4s-ext"                    % "3.2.11",
      "org.json4s"           %% "json4s-jackson"                % "3.2.11",
      "com.github.tototoshi" %% "play-json4s-native"            % "0.4.0",
      "org.flywaydb"         %% "flyway-play"                   % "2.0.1",
      specs2 % "test",
      jdbc,
      ws
    ),
    checksums := Nil,
    routesGenerator := InjectedRoutesGenerator,
      scalikejdbcSettings
  )

lazy val anormVersion = "2.4.0"
lazy val scalikejdbcPlayVersion = "2.4.+"
lazy val h2Version = "1.4.+"
