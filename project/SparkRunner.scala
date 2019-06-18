import scala.sys.process.{ Process, ProcessLogger }

object SparkRunner {

  private val versionPattern = raw"\d+\.\d+\.\d+".r

  def runSpark(
    appSparkVersion: String,
    appScalaVersion: String
  )(className: String, args: List[String], assemblyJarPath: String): Unit = {

    val (submitSparkVersion, submitScalaVersion) = parseSparkSubmitVersion()

    println(s"spark-submit Spark version: $submitSparkVersion")
    println(s"spark-submit Scala version: $submitScalaVersion")

    println(s"alchemist Spark version: $appSparkVersion")
    println(s"alchemist Scala version: $appScalaVersion")

    if (appSparkVersion != submitSparkVersion) {
      println(
        "[WARN] spark-submit Spark version is different from alchemist one. This may lead to binary incompatibility!"
      )
    }

    val c = s"spark-submit --master local[1] --class $className $assemblyJarPath ${args.mkString(" ")}"
    println(s"Executing '$c'")
    Process(c).!
  }

  private def parseSparkSubmitVersion(): (String, String) = {

    val lb = scala.collection.mutable.ListBuffer.empty[String]
    val pl = ProcessLogger.apply(line => if (line.contains("version")) lb.append(line))

    val _ = Process("spark-submit --version").!!(pl)

    val (submitSparkVersion, submitScalaVersion) = lb.toList.flatMap(versionPattern.findAllIn(_).toList).take(2) match {
      case sparkVersion :: scalaVersion :: Nil => (sparkVersion.trim, scalaVersion.trim)
      case _                                   => sys.error("was not able to parse spark-submit output")
    }

    (submitSparkVersion, submitScalaVersion)
  }
}
