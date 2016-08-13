/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.twirl.api.Html
import play.api.{ Play, Environment, Configuration, PlayException }
import com.google.inject.Inject

import org.fusesource.scalate.{ TemplateEngine, InvalidSyntaxException }
import org.fusesource.scalate.util.FileResourceLoader
import org.fusesource.scalate.layout.DefaultLayoutStrategy

// Jade TemplateのFactory定義
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object Jade extends Jade {
  def apply(template: String, layout: Option[String] = None) =
    new Template(template, layout)
}

// Jade Template 定義
//~~~~~~~~~~~~~~~~~~~~
trait Jade {
  import scala.collection.JavaConversions._

  /** テンプレート格納ディレクトリ */
  def getTemplateDir(env: Environment, conf: Configuration): Option[java.io.File] =
    conf.getString("jade.template.path") match {
      case Some(path) => Some(env.getFile(path))
      case _          => Some(env.getFile("app/views"))
    }

  /** テンプレートで常にimport宣言をする場合の定義 */
  def getImportStatements(conf: Configuration): Seq[String] =
    conf.getStringList("jade.import") match {
      case Some(list) => list.toSeq
      case _          => Seq.empty[String]
    }

  /** エンジンの設定 */
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

  /** テンプレート定義 */
  class Template(
    val template: String,
    val layout:   Option[String]
  ) {
    def render(params: Map[String, Any] = Map.empty)
      (implicit env: Environment, conf: Configuration): Html = {
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

  /** コンパイルエラー発生時の例外 */
  final class JadeCompilationException(
    override val sourceName: String,
    override val input:      String,
    override val line:       Integer,
    override val position:   Integer,
    message: String,
    cause:   Throwable
  ) extends PlayException.ExceptionSource("scalate compilation exception", message, cause)
}
