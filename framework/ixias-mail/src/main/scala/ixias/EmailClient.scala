/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.mail

import scala.concurrent.{ Future, ExecutionContext }
import com.google.inject.AbstractModule
import com.google.inject.name.Names

import ixias.util.Logger
import org.slf4j.LoggerFactory

// Module declaration
//~~~~~~~~~~~~~~~~~~~~~
class MailerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[EmailClient])
      .annotatedWith(Names.named("smtp"))
      .to(classOf[EmailClientViaSMTP])
    bind(classOf[EmailClient])
      .annotatedWith(Names.named("twillio"))
      .to(classOf[EmailClientViaTwillio])
  }
}

// Email Sender
//~~~~~~~~~~~~~~
trait EmailClient {

  /** The logger */
  protected lazy val logger =
    new Logger(LoggerFactory.getLogger(this.getClass.getName))

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Send an email with the provided data.
   */
  def send(to: UserEmail, tpl: EmailTemplate[_])
    (implicit ctx: ExecutionContext): Future[String] =
    tpl.from match {
      case Some(from) => send(to, from, tpl)
      case None       => Future.failed(new NoSuchElementException("The from adrress is empty."))
    }

  /**
   * Send an email with the provided data.
   */
  def send(to: UserEmail, from: UserEmail, tpl: EmailTemplate[_])
    (implicit ctx: ExecutionContext): Future[String]
}
