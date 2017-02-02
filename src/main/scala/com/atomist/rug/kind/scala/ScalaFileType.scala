package com.atomist.rug.kind.scala

import com.atomist.rug.kind.grammar.AntlrRawFileType
import com.atomist.source.FileArtifact
import com.atomist.tree.content.text.grammar.antlr.FromGrammarAstNodeCreationStrategy

class ScalaFileType
  extends AntlrRawFileType(
    topLevelProduction = "compilationUnit",
    nodeCreationStrategy = FromGrammarAstNodeCreationStrategy,
    grammars = "classpath:grammars/antlr/Scala.g4") {

  override def description = "Scala file"

  override def isOfType(f: FileArtifact): Boolean =
    f.name.endsWith(".scala")

}

