package com.atomist.rug.kind.grammar

import com.atomist.project.ProjectOperationArguments
import com.atomist.rug.kind.core.{FileArtifactBackedMutableView, FileMutableView, ProjectMutableView}
import com.atomist.rug.kind.dynamic.{ContextlessViewFinder, MutableContainerMutableView, MutableTreeNodeUpdater}
import com.atomist.rug.parser.Selected
import com.atomist.rug.runtime.rugdsl.DefaultEvaluator
import com.atomist.rug.spi.{MutableView, ReflectivelyTypedType, Type}
import com.atomist.source.{ArtifactSource, FileArtifact}
import com.atomist.tree.TreeNode
import com.atomist.tree.content.text.MutableContainerTreeNode
import com.atomist.tree.content.text.grammar.MatchListener
import scala.collection.JavaConverters._

/**
  * Convenient superclass for types that parse file content and can
  * be resolved from files and projects
  */
abstract class TypeUnderFile extends Type(DefaultEvaluator)
  with ReflectivelyTypedType
  with ContextlessViewFinder {

  final override def resolvesFromNodeTypes = Set("Project", "File")

  /**
    * Is this file of interest to this type? Typically will involve an extension check
    *
    * @param f file to test
    * @return whether we should try to parse the file with our parser
    */
  def isOfType(f: FileArtifact): Boolean

  override def viewManifest: Manifest[_] = manifest[MutableContainerMutableView]


  override protected def findAllIn(rugAs: ArtifactSource,
                                   selected: Selected,
                                   context: TreeNode,
                                   poa: ProjectOperationArguments,
                                   identifierMap: Map[String, Object]): Option[Seq[TreeNode]] = {
    context match {
      case pmv: ProjectMutableView =>
        Some(pmv
          .files
          .asScala
          .filter(f => isOfType(f.currentBackingObject))
          .flatMap(f => toView(f))
        )
      case f: FileMutableView if isOfType(f.currentBackingObject) =>
        Some(toView(f).toSeq)
      case _ => None
    }
  }

  private def toView(f: FileArtifactBackedMutableView): Option[MutableView[_]] = {
    val rawNode = contentToRawNode(f.content)
    rawNode.map(n => {
      val mtn = createView(n, f)
      // Ensure the file is updated based on any changes to the underlying AST at any level
      mtn match {
        case m: MutableContainerMutableView =>
          f.registerUpdater(new MutableTreeNodeUpdater(m.currentBackingObject))
        case _ =>
      }
      mtn
    })
  }

  /**
    * Subclasses can override this if they want to customize the top level node created:
    * for example, to add verbs that can be used instead of drilling into path expressions.
    *
    * @return new mutable view
    */
  protected def createView(n: MutableContainerTreeNode, f: FileArtifactBackedMutableView): MutableView[_] = {
    new MutableContainerMutableView(n, f)
  }

  /**
    * Return a parsed node. Useful to validate content, for example in tests.
    *
    * @param content content to parse
    * @return
    */
  def contentToRawNode(content: String, ml: Option[MatchListener] = None): Option[MutableContainerTreeNode]

}
