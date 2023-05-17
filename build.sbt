lazy val root = (project in file("."))
  .settings(
    name := "trade-in",

    version := "0.1.0",

    scalaVersion := "2.12.15",

    libraryDependencies ++= Seq (
      "org.ergoplatform" %% "ergo-appkit" % "5.0.1",
    ),

    assembly / assemblyJarName := s"${name.value}-v${version.value}.jar",
    assembly / assemblyOutputPath := file(s"./${name.value}-v${version.value}.jar/")
  )
