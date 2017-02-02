package com.atomist.rug.kind.scala

import com.atomist.rug.kind.core.ProjectMutableView
import com.atomist.source.{EmptyArtifactSource, SimpleFileBasedArtifactSource, StringFileArtifact}
import org.scalatest.{FlatSpec, Matchers}

class ScalaFileTypeTest extends FlatSpec with Matchers {

  import ScalaFileTypeTest._

  val scalaFileType = new ScalaFileType

  it should "parse hello world" in {
    val scala = scalaFileType.findAllIn(HelloWorldProject)
    scala.isDefined should be (true)
    scala.get.size should be (1)
  }

  it should "parse hello world and write out correctly" in {
    val parsed = scalaFileType.parseToRawNode(HelloWorld).get
    val parsedValue = parsed.value
    withClue(s"Unexpected content: [$parsedValue]") {
      parsedValue should equal(HelloWorld)
    }
  }

}

object ScalaFileTypeTest {

  val HelloWorld =
    s"""
      |object Hello extends App {
      |
      | println(s"Hello world, the time is ${System.currentTimeMillis()}")
      |
      |}
    """.stripMargin

  val HelloWorldSources = SimpleFileBasedArtifactSource(
    StringFileArtifact("src/main/scala/Hello.scala", HelloWorld)
  )

  val HelloWorldProject = new ProjectMutableView(EmptyArtifactSource(), HelloWorldSources)
}
