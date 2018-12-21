/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
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
  override def send(to: UserEmail, tpl: EmailTemplate[_])
    (implicit ctx: ExecutionContext): Future[String] =
    for {
      from    <- Future.fromTry(getTwillioFrom())
      message <- send(to, UserEmail(from), tpl)
    } yield message

  /**
   * Send an email with the provided data.
   */
  override def send(to: UserEmail, from: UserEmail, tpl: EmailTemplate[_])
    (implicit ctx: ExecutionContext): Future[String] =
    Future.fromTry(for {
      sid   <- getTwillioSid()
      token <- getTwillioAuthToken()
    } yield {
      val client  = new TwilioRestClient(sid, token)
      val mParams = new ArrayList[NameValuePair]()
      mParams.add(new BasicNameValuePair("To",   to.address))
      mParams.add(new BasicNameValuePair("From", from.address))
      mParams.add(new BasicNameValuePair("Body", tpl.getBodySMSText(to, from).get))
      val message: Message = client.getAccount().getMessageFactory().create(mParams)
      message.getBody()
    }) andThen {
      case Success(_)  =>  logger.info("[SUCCESS] to=" + to.address)
      case Failure(ex) => logger.error("[FAILURE] to=" + to.address, ex)
    } recover {
      case _: Throwable if tpl.silent => ""
    }
}
