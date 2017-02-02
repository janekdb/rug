package com.atomist.rug.kind.scala

import com.atomist.rug.TestUtils
import com.atomist.rug.kind.core.ProjectMutableView
import com.atomist.source.{EmptyArtifactSource, SimpleFileBasedArtifactSource, StringFileArtifact}
import org.scalatest.{FlatSpec, Matchers}

class ScalaFileTypeTest extends FlatSpec with Matchers {

  import ScalaFileTypeTest._

  val scalaFileType = new ScalaFileType

  it should "parse hello world" in {
    ???
    val scala = scalaFileType.findAllIn(HelloWorldProject)
    scala.isDefined should be(true)
    scala.get.size should be(1)
  }

  it should "parse hello world and write out correctly" in {
    val parsed = scalaFileType.parseToRawNode(HelloWorld).get
    val parsedValue = parsed.value
    withClue(s"Unexpected content: [$parsedValue]") {
      parsedValue should equal(HelloWorld)
    }
  }

  it should "parse multiline content and write out correctly" in {
    val f = TestUtils.sideFile(this, this.getClass.getSimpleName)
    println(s"[${f.content}]")
    val parsed = scalaFileType.parseToRawNode(f.content).get
    val parsedValue = parsed.value
//    withClue(s"Unexpected content: [$parsedValue]") {
//      parsedValue should equal(Longer)
//    }
  }

}

object ScalaFileTypeTest {

  val HelloWorld = "case class Hello()"

  val Longer =
    """
      |class Hello {
      |}
    """.stripMargin

  val HelloWorldSources = SimpleFileBasedArtifactSource(
    StringFileArtifact("src/main/scala/atomist/Hello.scala", HelloWorld)
  )

  val HelloWorldProject = new ProjectMutableView(EmptyArtifactSource(), HelloWorldSources)
}
