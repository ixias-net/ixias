/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.mail

import java.io.File

// User's email data
//~~~~~~~~~~~~~~~~~~~~
case class UserEmail(
  val address:   String,                // E-mail address, of phone number (SMS)
  val firstName: Option[String] = None, // The user's first name
  val lastName:  Option[String] = None, // The user's last name
  val isSetName: Boolean        = false // The flag to indicate whether the E-mail address should be include personal name.
) {
  def name = Option(Seq(lastName, firstName).collect({ case Some(v) => v }).mkString(" "))
}

// Email's Attachment
//~~~~~~~~~~~~~~~~~~~~
sealed trait Attachment

case class AttachmentFile(
  val name:        String,
  val file:        File,
  val description: Option[String] = None,
  val disposition: Option[String] = None
) extends Attachment

case class AttachmentData(
  val name:        String,
  val data:        Array[Byte],
  val mimetype:    String,
  val description: Option[String] = None,
  val disposition: Option[String] = None
) extends Attachment

trait EmailTemplate[P] extends EmailConfig {

  /** The E-mail's subject */
  val subject:     String

  /** The sender data */
  val from:        Option[UserEmail]     = None

  /** Add recipients CC to the email using the specified address */
  val cc:          Seq[UserEmail]        = Seq.empty

  /** Add recipients BCC to the email using the specified address */
  val bcc:         Seq[UserEmail]        = Seq.empty

  /** The Email's headers */
  val headers:     Seq[(String, String)] = Seq.empty

  /** The Email's attachment files */
  val attachments: Seq[Attachment]       = Seq.empty

  /** The Email's provided parameters */
  val params:      P

  /** The flag to indicate whether email-client ignore errors */
  val silent:      Boolean               = false

  // --[ Properties ]-----------------------------------------------------------
  /** The charset */
  lazy val charset:       String         = getCharset()

  /** The bounce address */
  lazy val bounceAddress: Option[String] = getBounceAddress()

  // --[ Methods ]-----------------------------------------------------------
  /**
   * Build a E-mail's body (PlainText) with using provided data.
   */
  def getBodyText(to: UserEmail, from: UserEmail): Option[String] = None

  /**
   * Build a E-mail's body (HTML) with using provided data.
   */
  def getBodyHtml(to: UserEmail, from: UserEmail): Option[String] = None

  /**
   * Build a SMS's short message body (PlainText) with using provided data.
   */
  def getBodySMSText(to: UserEmail, from: UserEmail): Option[String] = None
}
