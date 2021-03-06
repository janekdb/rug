import {Project,File,CSharpFile} from '@atomist/rug/model/Core'
import {ProjectEditor} from '@atomist/rug/operations/ProjectEditor'
import {PathExpression,TreeNode,TypeProvider} from '@atomist/rug/tree/PathExpression'
import {PathExpressionEngine} from '@atomist/rug/tree/PathExpression'
import {Match} from '@atomist/rug/tree/PathExpression'
import {Parameter} from '@atomist/rug/operations/RugOperation'

class AddImport implements ProjectEditor {
    name: string = "Constructed"
    description: string = "Uses single microgrammar"
    parameters: Parameter[] = [
        {name: "packageName", description: "The package name", displayName: "Java Package", pattern: "^.*$", maxLength: 100}
    ]

    edit(project: Project, {packageName } : {packageName: string}) {
      let eng: PathExpressionEngine = project.context().pathExpressionEngine()

      let count = 0
      eng.with<CSharpFile>(project, "//File()/CSharpFile()", n => {
        n.addUsing(packageName)
        count++
      })

      if (count == 0)
       throw new Error("No C# files found. Sad.")
    }
}

export let editor = new AddImport()