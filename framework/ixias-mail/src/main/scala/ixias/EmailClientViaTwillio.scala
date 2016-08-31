/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.mail

import java.util.ArrayList
import javax.inject.Singleton
import scala.util.{ Success, Failure }
import scala.concurrent.{ Future, ExecutionContext }

import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.instance.Message

// Send an email(SMS) via Twillio REST API
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
@Singleton
class EmailClientViaTwillio extends EmailClient with EmailConfig {

  /**
   * Send an email with the provided data.
   */
  override def send[P, T <: EmailTemplate[P]](to: UserEmail, tpl: T, params: P)
    (implicit ctx: ExecutionContext): Future[String] =
    tpl.from match {
      case Some(from) => send(to, from, tpl, params)
      case None       => for {
        from <- Future.fromTry(getTwillioFrom())
        body <- send(to, UserEmail(from), tpl, params)
      } yield body
    }

  /**
   * Send an email with the provided data.
   */
  override def send[P, T <: EmailTemplate[P]](to: UserEmail, from: UserEmail, tpl: T, params: P)
    (implicit ctx: ExecutionContext): Future[String] =
    Future.fromTry(for {
      sid   <- getTwillioSid()
      token <- getTwillioAuthToken()
    } yield {
      val client  = new TwilioRestClient(sid, token)
      val mParams = new ArrayList[NameValuePair]()
      mParams.add(new BasicNameValuePair("To",   to.address))
      mParams.add(new BasicNameValuePair("From", from.address))
      mParams.add(new BasicNameValuePair("Body", tpl.getBodySMSText(to, from, params).get))
      val message: Message = client.getAccount().getMessageFactory().create(mParams)
      message.getBody()
    }) andThen {
      case Success(_)  =>  logger.info("[SUCCESS] to=" + to.address)
      case Failure(ex) => logger.error("[FAILURE] to=" + to.address, ex)
    } recover {
      case _: Throwable if tpl.silent => ""
    }
}
