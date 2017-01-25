package com.atomist.rug.runtime.js

import com.atomist.event.SystemEventHandler
import com.atomist.param.Tag
import com.atomist.rug.runtime.js.interop._
import com.atomist.source.ArtifactSource
import jdk.nashorn.api.scripting.ScriptObjectMirror

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Finds and evaluates handlers in a Rug archive.
  */
object JavaScriptHandlerFinder {

  /**
    * Find handler operations in the given Rug archive
    *
    * @param rugAs   archive to look into
    * @return a sequence of instantiated operations backed by JavaScript
    *
    */

  def findEventHandlers(rugAs: ArtifactSource,
                        ctx: JavaScriptHandlerContext): Seq[SystemEventHandler] = {
    handlersFromVars(rugAs, new JavaScriptContext(rugAs), ctx)
  }

  private def handlersFromVars(rugAs: ArtifactSource, jsc: JavaScriptContext, ctx: JavaScriptHandlerContext): Seq[SystemEventHandler] = {
    jsc.vars.foldLeft(Seq[SystemEventHandler]())((acc: Seq[SystemEventHandler], jsVar) => {
      val obj = jsVar.scriptObjectMirror
      if (obj.hasMember("name") && obj.hasMember("description") && obj.hasMember("handle") && obj.hasMember("expression")) {
        val name = obj.getMember("name").asInstanceOf[String]
        val description = obj.getMember("description").asInstanceOf[String]
        val handle = obj.getMember("handle").asInstanceOf[ScriptObjectMirror]
        val expression: String = obj.getMember("expression") match {
          case x: String => x
          case o: ScriptObjectMirror => o.getMember("expression").asInstanceOf[String]
          case _ => null
        }
        val tags = readTagsFromMetadata(obj)
        acc :+ new JavaScriptEventHandler(expression, handle, obj, rugAs, ctx, name, description, tags)
      } else {
        acc
      }
    })
  }

  protected def readTagsFromMetadata(someVar: ScriptObjectMirror): Seq[Tag] = {
    Try {
      someVar.getMember("tags") match {
        case som: ScriptObjectMirror =>
          val stringValues = som.values().asScala collect {
            case s: String => s
          }
          stringValues.map(s => Tag(s, s)).toSeq
        case _ => Nil
      }
    }.getOrElse(Nil)
  }
}
