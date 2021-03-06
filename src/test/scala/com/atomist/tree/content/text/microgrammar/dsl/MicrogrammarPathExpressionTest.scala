package com.atomist.tree.content.text.microgrammar.dsl

import com.atomist.project.archive.DefaultAtomistConfig
import com.atomist.rug.kind.DefaultTypeRegistry
import com.atomist.rug.kind.core.ProjectMutableView
import com.atomist.rug.spi.UsageSpecificTypeRegistry
import com.atomist.source.{SimpleFileBasedArtifactSource, StringFileArtifact}
import com.atomist.tree.TreeNode
import com.atomist.tree.content.text.microgrammar._
import com.atomist.tree.pathexpression.{PathExpressionEngine, PathExpressionParser}
import org.scalatest.{FlatSpec, Matchers}


class OptionalFieldMicrogrammarTest extends FlatSpec with Matchers {

  it should "Let give 0 matches when I access something that might exist but does not" in {

    val microgrammar =
      new MatcherMicrogrammar(
        Literal("a ") ~ Regex("[a-z]+", Some("blah")) ~ Optional(Literal("yo", Some("myWord"))) ~ Literal(".")
        , "bananagrammar")
    val pathExpression = "/File()/bananagrammar()/myWord()"

    val result = exercisePathExpression(microgrammar, pathExpression, input)

    result.size should be(0)
  }

  it should "Let give 0 matches when I access something deep that might exist but does not" in {

    val microgrammar =
      new MatcherMicrogrammar(
        Literal("a ") ~ Regex("[a-z]+", Some("blah")) ~ Optional(Literal("yo") ~ Wrap(Rep(Regex("[a-z]+", Some("carrot"))), "banana")) ~ Literal(".")
        , "bananagrammar")
    val pathExpression = "/File()/bananagrammar()/banana()/carrot()"

    val result = exercisePathExpression(microgrammar, pathExpression, input)

    result.size should be(0)
  }

  it should "Fail when I name a node that can't possibly exist" in pendingUntilFixed {

    val microgrammar =
      new MatcherMicrogrammar(
        Literal("a ") ~ Regex("[a-z]+", Some("blah"))
        , "bananagrammar")
    val pathExpression = "/File()/bananagrammar()/dadvan()"

    val message = exerciseFailingPathExpression(microgrammar, pathExpression, input)
    message should be("what should it be")
  }


  def exercisePathExpression(microgrammar: Microgrammar, pathExpressionString: String, input: String): List[TreeNode] = {

    val result = exercisePathExpressionInternal(microgrammar, pathExpressionString, input)

    result match {
      case Left(a) => fail(a)
      case Right(b) => b
    }
  }

  private def exercisePathExpressionInternal(microgrammar: Microgrammar, pathExpressionString: String, input: String) = {

    /* Construct a root node */
    val as = SimpleFileBasedArtifactSource(StringFileArtifact("banana.txt", input))
    val pmv = new ProjectMutableView(as /* cheating */ , as, DefaultAtomistConfig)

    /* Parse the path expression */
    val pathExpression = PathExpressionParser.parseString(pathExpressionString)

    /* Install the microgrammar */
    val typeRegistryWithMicrogrammar =
      new UsageSpecificTypeRegistry(DefaultTypeRegistry,
        Seq(new MicrogrammarTypeProvider(microgrammar)))

    new PathExpressionEngine().evaluate(pmv, pathExpression, typeRegistryWithMicrogrammar)
  }

  def exerciseFailingPathExpression(microgrammar: Microgrammar, pathExpressionString: String, input: String): String = {
    val result = exercisePathExpressionInternal(microgrammar, pathExpressionString, input)

    result match {
      case Left(a) => a
      case Right(b) => fail("This was supposed to fail")
    }
  }


  val input: String =
    """There was a banana. It crossed the street. A car ran over it.
      |No banana for you.
      |""".stripMargin

  it should "match an unnamed literal" in {

    val microgrammar = new MatcherMicrogrammar(Literal("banana"), "bananagrammar")
    val pathExpression = "/File()/bananagrammar()"

    val result = exercisePathExpression(microgrammar, pathExpression, input)

    result.size should be(2)
  }

  it should "match a named node" in {

    val microgrammar =
      new MatcherMicrogrammar(
        Literal("a ") ~ Regex("[a-z]+", Some("blah")) ~ Optional(Literal("yo", Some("myWord"))) ~ Literal(".")
        , "bananagrammar")
    val pathExpression = "/File()/bananagrammar()/blah"

    val result = exercisePathExpression(microgrammar, pathExpression, input)

    result.size should be(1)
  }
}