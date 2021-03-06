package com.atomist.rug.kind.yml

import java.io.StringReader

import com.atomist.rug.kind.core.{LazyFileArtifactBackedMutableView, ProjectMutableView}
import com.atomist.rug.spi.{ExportFunction, ExportFunctionParameterDescription, TerminalView, Typed}
import com.atomist.source.FileArtifact
import com.atomist.util.Utils.StringImprovements
import org.yaml.snakeyaml.Yaml

/**
  * Keys use dot (.) notation
  *
  * @param originalBackingObject original FileArtifact
  * @param parent
  */
// TODO largely incomplete
class YmlMutableView(
                      originalBackingObject: FileArtifact,
                      parent: ProjectMutableView)
  extends LazyFileArtifactBackedMutableView(originalBackingObject, parent)
    with TerminalView[FileArtifact] {

  private var model = new YmlModel(originalBackingObject.content)

  override protected def currentContent: String = model.yml

  @ExportFunction(readOnly = true, description = "Return the value of the given key")
  def valueOf(@ExportFunctionParameterDescription(name = "name",
    description = "The YAML key whose content you want")
              name: String): Object =
    model.valueOf(name).getOrElse("")

  /**
    * Does nothing if it doesn't exist.
    */
  @ExportFunction(readOnly = false, description = "Update the value of a given key")
  def updateKey(
                 @ExportFunctionParameterDescription(name = "name",
                   description = "Name of the key to update")
                 name: String,
                 @ExportFunctionParameterDescription(name = "value",
                   description = "New value for the key")
                 value: String): Unit = {
    val oldValue = model.dump(name).map(oldval => {
      model.setKey(name, value)
      val dumped = model.dump(name).get
      update(name, oldval, dumped)
    })
  }

  // Model is updated
  private def update(key: String, oldValue: String, newValue: String): Unit = {
    //because the yaml library seems to like \n's
    val updatedYml = content.toUnix.replace(oldValue, newValue)
    this.model = new YmlModel(updatedYml)
  }
}

private[yml] class YmlModel(val yml: String) {

  import scala.collection.JavaConverters._

  val y = new Yaml()

  val map: scala.collection.mutable.Map[String, Object] = y.loadAll(new StringReader(yml)).asScala.headOption match {
    case Some(map: java.util.Map[String @unchecked, Object @unchecked]) => map.asScala
    case _ => throw new IllegalStateException(s"Unrecognized result parsing yml '$yml'")
  }

  def valueOf(name: String): Option[Object] = map.get(name)

  def setKey(name: String, value: String): Unit = map.put(name, value)

  def dump(key: String): Option[String] = valueOf(key).map(n => y.dump(n))
}
