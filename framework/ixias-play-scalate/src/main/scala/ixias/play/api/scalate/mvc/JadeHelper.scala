/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.scalate.mvc

import play.twirl.api.Html
import play.api.{ Environment, Configuration }

import org.fusesource.scalate.{ TemplateEngine, InvalidSyntaxException }
import org.fusesource.scalate.util.FileResourceLoader
import org.fusesource.scalate.layout.DefaultLayoutStrategy

// Jade 取り扱い処理
//~~~~~~~~~~~~~~~~~~~~
object JadeHelper {

  // -- [ Properties ]----------------------------------------------------------
  lazy val templateEngine = (env: Environment, conf: Configuration) => {
    val engine = new TemplateEngine
    engine.resourceLoader     = new FileResourceLoader(getTemplateDir(env, conf))
    engine.workingDirectory   = env.getFile("target/jade")
    engine.classpath          = engine.workingDirectory.toString + "/classes"
    engine.combinedClassPath  = true
    engine.classLoader        = env.classLoader
    engine.importStatements ++= getImportStatements(conf)
    engine
  }

  // -- [ Methods ]-------------------------------------------------------------
  /**
   * Build a template component to render HTML.
   */
  def apply(template: String, layout: Option[String] = None) = Template(template, layout)

  /**
   * The root directory for template.
   */
  def getTemplateDir(env: Environment, conf: Configuration): Option[java.io.File] =
    conf.get[Option[String]]("jade.template.path") match {
      case Some(path) => Some(env.getFile(path))
      case _          => Some(env.getFile("app/views"))
    }

  /**
   * The declaration for `import` statement in template.
   */
  def getImportStatements(conf: Configuration): Seq[String] =
    conf.get[Seq[String]]("jade.import")

  // -- [ Internal class ]------------------------------------------------------
  /**
   * The definition of template component.
   */
  sealed case class Template(template: String, layout: Option[String]) {
    /**
     * Renders a HTML body-text with parameters.
     */
    def render(params: Map[String, Any] = Map.empty)(implicit env: Environment, conf: Configuration): Html = {
      val engine = templateEngine(env, conf)
      try {
        if (layout.isDefined)
          engine.layoutStrategy = new DefaultLayoutStrategy(engine, layout.get)
        Html(engine.layout(template, params))
      } catch {
        case ex: InvalidSyntaxException => throw new JadeCompilationException(
          ex.brief,
          engine.resourceLoader.load(ex.template),
          ex.pos.line,
          ex.pos.column,
          ex.brief,
          ex
        )
      }
    }
  }
}
