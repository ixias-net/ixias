/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.mail

import scala.util.{ Try, Success, Failure }
import scala.concurrent.{ Future, ExecutionContext }
import org.apache.commons.mail.{ HtmlEmail, MultiPartEmail, EmailAttachment, DefaultAuthenticator }

// Send an email via SMTP protocol
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
@javax.inject.Singleton
class EmailClientViaSMTP extends EmailClient with EmailConfig {

  /**
   * Send an email with the provided data.
   */
  def send(to: UserEmail, from: UserEmail, tpl: EmailTemplate[_])
    (implicit ctx: ExecutionContext): Future[String] =
  {
    val email = createEmail(to, from, tpl)

    // Sets SMTP options.
    email.setSmtpPort(getSmtpPort())
    email.setSSLOnConnect(getSmtpSSL())
    email.setStartTLSEnabled(getSmtpTLS())
    if (getSmtpSSL()) {
       email.setSslSmtpPort(getSmtpPort().toString)
    }
    tpl.headers foreach { v => email.addHeader(v._1, v._2) }
    for (u <- getSmtpUser(); p <- getSmtpPassword()) yield
      email.setAuthenticator(new DefaultAuthenticator(u, p))

    // Sneds a email.
    (for {
      host    <- Future.fromTry(getSmtpHost())
      message <- Future.fromTry(Try(email.send()))
    } yield message) andThen {
      case Success(_)  =>  logger.info("[SUCCESS] to=" + to.address)
      case Failure(ex) => logger.error("[FAILURE] to=" + to.address, ex)
    } recover {
      case _: Throwable if tpl.silent => ""
    }
  }

  /**
   * Create an appropriate email object based on the content type.
   */
  private def createEmail(to: UserEmail, from: UserEmail, tpl: EmailTemplate[_]): MultiPartEmail = {
    val bodyHtmlOpt = tpl.getBodyHtml(to, from).filter(_.trim.nonEmpty)
    val bodyTextOpt = tpl.getBodyText(to, from).filter(_.trim.nonEmpty)
    val email       = bodyHtmlOpt.isDefined match {
      case true  =>
        val email = new HtmlEmail()
        email.setCharset(tpl.charset)
        email.setHtmlMsg(bodyHtmlOpt.get)
        bodyTextOpt map email.setTextMsg
        email
      case false =>
        val email = new MultiPartEmail()
        email.setCharset(tpl.charset)
        bodyTextOpt map email.setMsg
        email
    }

    // Subject: / To: / From:
    email.setSubject(tpl.subject)
    email.addTo(to.address,     if (to.isSetName)     to.name.getOrElse(null) else null)
    email.setFrom(from.address, if (from.isSetName) from.name.getOrElse(null) else null)

    // optional params
    tpl.bounceAddress map email.setBounceAddress
    tpl.headers     foreach { v => email.addHeader(v._1, v._2) }
    tpl.attachments foreach {
      case attachmentData: AttachmentData =>
        val description = attachmentData.description.getOrElse(attachmentData.name)
        val disposition = attachmentData.disposition.getOrElse(EmailAttachment.ATTACHMENT)
        val dataSource  = new javax.mail.util.ByteArrayDataSource(attachmentData.data, attachmentData.mimetype)
        email.attach(dataSource, attachmentData.name, description, disposition)
      case attachmentFile: AttachmentFile =>
        val description = attachmentFile.description.getOrElse(attachmentFile.name)
        val disposition = attachmentFile.disposition.getOrElse(EmailAttachment.ATTACHMENT)
        val emailAttachment = new EmailAttachment()
        emailAttachment.setName(attachmentFile.name)
        emailAttachment.setPath(attachmentFile.file.getPath)
        emailAttachment.setDescription(description)
        emailAttachment.setDisposition(disposition)
        email.attach(emailAttachment)
    }
    email
  }
}
