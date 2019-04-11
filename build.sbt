
name := "spark-athena"

version := "1.1"

organization := "com.b2w.ml"

scalaVersion := "2.11.12"

resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "B2W Private" at "http://nexus.b2w/repository/maven-private"
)

val sparkVersion = "2.4.0"
val awsSdkVersion = "1.11.534"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

  "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion % "provided",
  "com.amazonaws" % "aws-java-sdk-iam" % awsSdkVersion % "provided",
  "com.syncron.amazonaws" % "simba-athena-jdbc-driver" % "2.0.2",

  "org.apache.logging.log4j" % "log4j-core" % "2.11.2" % "provided",

  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

// Configurações para execução
run in Compile :=
  Defaults
    .runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
    .evaluated

runMain in Compile :=
  Defaults
    .runMainTask(fullClasspath in Compile, runner in(Compile, run))
    .evaluated

val nexusB2w = "nexus.b2w"

val username = sys.env.getOrElse("DEPLOY_USER", "")
val password = sys.env.getOrElse("DEPLOY_PASSWORD", "")

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
credentials += Credentials("Sonatype Nexus Repository Manager", nexusB2w, username, password)

publishTo := {
  if (version.value == "SNAPSHOT")
    Some("snapshots" at "http://" + nexusB2w + "/content/repositories/snapshots")
  else
    Some("releases"  at "http://" + nexusB2w + "/content/repositories/releases")
}

publishMavenStyle := true

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}