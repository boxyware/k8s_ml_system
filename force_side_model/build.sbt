name := "spark_force_side_model"
organization := "com.boxyware"
version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.2.0" % "provided",
  "ml.combust.mleap" %% "mleap-spark" % "0.13.0"
)

// META-INF discarding
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}

enablePlugins(DockerPlugin)

dockerfile in docker := {
  val jarFile: File = assembly.value
  val jarTarget = "${SPARK_HOME}/work/force.jar"

  new Dockerfile {
    from("boxyware/spark-shell")
    workDir("${SPARK_HOME}/work")
    add(file("src/main/resources/entrypoint.sh"), "/opt/")
    add(jarFile, jarTarget)
    entryPoint(s"/opt/entrypoint.sh")
  }
}

imageNames in docker := Seq(
  ImageName("boxyware/force-side-model")
)