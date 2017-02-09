package com.atomist.rug.kind.scala

import com.atomist.project.edit.SuccessfulModification
import com.atomist.rug.kind.grammar.AbstractTypeUnderFileTest

/**
  * Tests for realistic Scala scenarios
  */
class ScalaFileTypeUsageTest extends AbstractTypeUnderFileTest {

  import ScalaFileTypeTest._

  override val typeBeingTested = new ScalaFileType

  it should "change exception catch ???" is pending

  it should "change a.equals(b)" in {
//    val tn = typeBeingTested.fileToRawNode(UsesDotEquals).get
//    println(TreeNodeUtils.toShorterString(tn, TreeNodeUtils.NameAndContentStringifier))

    modify("EqualsToSymbol.ts", UsesDotEqualsSources) match {
      case sm: SuccessfulModification =>
        val theFile = sm.result.findFile(UsesDotEquals.path).get
        //println(theFile.content)
        theFile.content.contains("==") should be (true)
        theFile.content.contains("equals") should be (false)
        println(theFile.content)
      case wtf => fail(s"Expected SuccessfulModification, not $wtf")
    }
  }

  it should "upgrade ScalaTest assertions" in {
    modify("UpgradeScalaTestAssertions.ts", ScalaTestSources) match {
      case sm: SuccessfulModification =>
        val theFile = sm.result.findFile(OldStyleScalaTest.path).get
        //println(theFile.content)
        theFile.content.contains("===") should be (true)
      case wtf => fail(s"Expected SuccessfulModification, not $wtf")
    }
  }

}

