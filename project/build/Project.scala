import sbt._
import com.twitter.sbt._

class Project(info: ProjectInfo) extends StandardProject(info) {
  override def disableCrossPaths = false

  override def managedStyle = ManagedStyle.Maven

  val nettyRepo = "repository.jboss.org" at "http://repository.jboss.org/nexus/content/groups/public/"

  val netty = "org.jboss.netty" % "netty" % "3.2.2.Final" withSources()
  val util = "com.twitter" %% "util" % "1.2-SNAPSHOT"

  val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test" withSources()
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources()

  // TODO: consolidate all of this stuff & contribute back to
  // standard-project.
  val thriftCompilerPath = "/Users/marius/pkg/thrift-0.5.0/compiler/cpp/thrift"
  override def compileThriftAction(lang: String) = task {
    import Process._
    outputPath.asFile.mkdirs()
    val tasks = thriftSources.getPaths.map { path =>
      execTask { "%s --gen %s -o %s %s".format(
        thriftCompilerPath, lang, outputPath.absolutePath, path) }
    }
    if (tasks.isEmpty) None else tasks.reduceLeft { _ && _ }.run
  }

  // thrift generation.
  def testThriftSources = (testSourcePath / "thrift" ##) ** "*.thrift"
  val testOutputPath = outputPath / "test-gen"
  def compileTestThriftAction(lang: String) = task {
    import Process._
    testOutputPath.asFile.mkdirs()
    val tasks = testThriftSources.getPaths.map { path =>
      execTask { "%s --gen %s -o %s %s".format(
        thriftCompilerPath, lang, testOutputPath.absolutePath, path) }
    }
    if (tasks.isEmpty) None else tasks.reduceLeft { _ && _ }.run
  }
  override def testSourceRoots = super.testSourceRoots +++ (testOutputPath / "gen-java" ##)

  lazy val compileTestThriftJava = compileTestThriftAction("java") describedAs("Compile (test) thrift into java")
  override def testCompileAction = super.testCompileAction dependsOn(compileTestThriftJava)

  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.6.1"

  // Temporary?
  val thrift = "thrift" % "libthrift" % "0.2.0"
}
