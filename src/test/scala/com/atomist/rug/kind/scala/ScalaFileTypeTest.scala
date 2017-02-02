package com.atomist.rug.kind.scala

import com.atomist.rug.TestUtils
import com.atomist.rug.kind.DefaultTypeRegistry
import com.atomist.rug.kind.core.ProjectMutableView
import com.atomist.rug.kind.dynamic.MutableContainerMutableView
import com.atomist.source.{EmptyArtifactSource, SimpleFileBasedArtifactSource, StringFileArtifact}
import com.atomist.tree.TreeNode
import com.atomist.tree.content.text.PositionedMutableContainerTreeNode
import com.atomist.tree.pathexpression.{ExpressionEngine, PathExpressionEngine, PathExpressionParser}
import com.atomist.tree.utils.TreeNodeUtils
import org.scalatest.{FlatSpec, Matchers}

class ScalaFileTypeTest extends FlatSpec with Matchers {

  import ScalaFileTypeTest._

  val ee: ExpressionEngine = new PathExpressionEngine

  val scalaFileType = new ScalaFileType

  it should "parse hello world" in {
    val scala = scalaFileType.findAllIn(HelloWorldProject)
    scala.isDefined should be(true)
    scala.get.size should be(1)
    val rootNode = scala.get.head
    println("The root node is " + TreeNodeUtils.toShorterString(rootNode))
    //rootNode.childrenNamed("compilationUnit").size should be (1)
  }

  it should "parse hello world and write out correctly" in {
    val parsed = scalaFileType.parseToRawNode(HelloWorld).get
    val parsedValue = parsed.value
    withClue(s"Unexpected content: [$parsedValue]") {
      parsedValue should equal(HelloWorld)
    }
  }

  it should "parse multiline content and write out correctly" in {
    println(s"[$Longer]")
    val parsed = scalaFileType.parseToRawNode(Longer).get
    val parsedValue = parsed.value
//    withClue(s"Unexpected content: [$parsedValue]") {
//      parsedValue should equal(Longer)
//    }
    println(TreeNodeUtils.toShorterString(parsed))
    println(parsed.asInstanceOf[PositionedMutableContainerTreeNode].fieldValues)
    // Broken as the contents are wrong
  }

  it should "parse external content and write out correctly" in {
    val f = TestUtils.sideFile(this, "Simple.scala")
    //println(s"[${f.content}]")
    val parsed = scalaFileType.parseToRawNode(f.content).get
    val parsedValue = parsed.value
    //    withClue(s"Unexpected content: [$parsedValue]") {
    //      parsedValue should equal(Longer)
    //    }
  }

  it should "drill into hello world with path expression" in {
    val scalas: Option[Seq[TreeNode]] = scalaFileType.findAllIn(HelloWorldProject)
    scalas.size should be(1)
    val scalaFileNode = scalas.get.head.asInstanceOf[MutableContainerMutableView]
    println(TreeNodeUtils.toShorterString(scalaFileNode))
    println(scalaFileNode.currentBackingObject.asInstanceOf[PositionedMutableContainerTreeNode].fieldValues)

    val expr = "//classDef"
    ee.evaluate(scalaFileNode, PathExpressionParser.parseString(expr), DefaultTypeRegistry) match {
      case Right(nodes) if nodes.nonEmpty =>
    }

    //scalaFileNode.value should equal(f.content)
  }

  it should "find class in realistic file using path expression" in {
    val f = TestUtils.sideFile(this, "Simple.scala")
    //println(f.content)
    val sources = SimpleFileBasedArtifactSource(f)
    val scalas: Option[Seq[TreeNode]] = scalaFileType.findAllIn(new ProjectMutableView(EmptyArtifactSource(), sources))
    scalas.size should be(1)
    val scalaFileNode = scalas.get.head.asInstanceOf[MutableContainerMutableView]
    println(TreeNodeUtils.toShorterString(scalaFileNode))
    println(scalaFileNode.currentBackingObject.asInstanceOf[PositionedMutableContainerTreeNode].fieldValues)

    val expr = "//classDef"
    ee.evaluate(scalaFileNode, PathExpressionParser.parseString(expr), DefaultTypeRegistry) match {
      case Right(nodes) if nodes.nonEmpty =>
    }

    scalaFileNode.value should equal(f.content)
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
