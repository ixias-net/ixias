/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend.memcached

import java.io.Closeable
import scala.util.Try
import scala.concurrent.{ Future, Promise, ExecutionContext }
import scala.concurrent.duration.Duration

import net.spy.memcached.{ AddrUtil, CachedData, ConnectionFactoryBuilder, MemcachedClient }
import net.spy.memcached.internal.{
  GetFuture,       GetCompletionListener,
  OperationFuture, OperationCompletionListener
}
import net.spy.memcached.transcoders.Transcoder

/**
 * The client to access a memcached cluster.
 */
trait Memcached extends Closeable {

  /** Fetches a value from the cache store. */
  def get[T](key: String)(implicit codec: Codec[T]): Future[Option[T]]

  /** Sets a (key, value) in the cache store, overwriting an existing value. */
  def set[T](key: String, value: T, expiry: Duration)(implicit codec: Codec[T]): Future[Unit]

  /** Sets a (key, value) in the cache store only if the key is not already used. */
  def add[T](key: String, value: T, expiry: Duration)(implicit codec: Codec[T]): Future[Boolean]

  /** Deletes a key from the cache store. */
  def delete(key: String): Future[Boolean]

  /** Shuts the client down, releasing its IO thread and connections. */
  def close(): Unit
}

object Memcached {
  def apply(config: Configuration)(implicit ec: ExecutionContext): Memcached =
    new SpyMemcached(config)(ec)
}

/**
 * The `Memcached` implementation on top of spymemcached.
 *
 * The value is always handed to spymemcached as a raw byte array: the encoding
 * is fully owned by `Codec`, so that a decoding failure surfaces as a failed
 * `Future` instead of being swallowed by a transcoder.
 */
private[memcached] class SpyMemcached(config: Configuration)(implicit ec: ExecutionContext)
    extends Memcached {

  /** Expiry values above this are interpreted by memcached as a unix timestamp. */
  private val MAX_RELATIVE_EXPIRY_SECONDS = 60 * 60 * 24 * 30

  private val client: MemcachedClient = {
    val factory = new ConnectionFactoryBuilder()
      .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
      .setOpTimeout(config.operationTimeout.toMillis)
      .build()
    new MemcachedClient(factory, AddrUtil.getAddresses(config.addresses))
  }

  /**
   * Prepends the configured prefix to the given key.
   *
   * The `-` separator is the layout `io.monix:shade` used, and is kept so that
   * entries written by the previous implementation are still addressed.
   */
  private def withPrefix(key: String): String =
    config.keysPrefix.filter(_.nonEmpty).fold(key)(_ + "-" + key)

  /**
   * Converts a scala duration to the expiry memcached expects.
   * `0` means "never expires", and anything from 30 days on has to be
   * sent as an absolute unix timestamp rather than a relative offset.
   */
  private def toExpirySeconds(expiry: Duration): Int =
    if (!expiry.isFinite) 0
    else {
      val seconds = expiry.toSeconds
      if (seconds < MAX_RELATIVE_EXPIRY_SECONDS) seconds.toInt
      else ((System.currentTimeMillis() / 1000) + seconds).toInt
    }

  private def fromGet[A](future: GetFuture[A]): Future[A] = {
    val promise = Promise[A]()
    future.addListener(new GetCompletionListener {
      def onComplete(ignored: GetFuture[_]): Unit = promise.complete(Try(future.get()))
    })
    promise.future
  }

  private def fromOperation[A](future: OperationFuture[A]): Future[A] = {
    val promise = Promise[A]()
    future.addListener(new OperationCompletionListener {
      def onComplete(ignored: OperationFuture[_]): Unit = promise.complete(Try(future.get()))
    })
    promise.future
  }

  // --[ Methods ]--------------------------------------------------------------
  def get[T](key: String)(implicit codec: Codec[T]): Future[Option[T]] =
    fromGet(client.asyncGet(withPrefix(key), ByteArrayTranscoder))
      .map(data => Option(data).map(codec.deserialize))

  def set[T](key: String, value: T, expiry: Duration)(implicit codec: Codec[T]): Future[Unit] =
    fromOperation(client.set(
      withPrefix(key), toExpirySeconds(expiry), codec.serialize(value), ByteArrayTranscoder))
      .map(_ => ())

  def add[T](key: String, value: T, expiry: Duration)(implicit codec: Codec[T]): Future[Boolean] =
    fromOperation(client.add(
      withPrefix(key), toExpirySeconds(expiry), codec.serialize(value), ByteArrayTranscoder))
      .map(_.booleanValue)

  def delete(key: String): Future[Boolean] =
    fromOperation(client.delete(withPrefix(key))).map(_.booleanValue)

  def close(): Unit = client.shutdown()
}

/** Hands the byte array over to memcached untouched. */
private[memcached] object ByteArrayTranscoder extends Transcoder[Array[Byte]] {
  def asyncDecode(data: CachedData):    Boolean     = false
  def encode(value: Array[Byte]):       CachedData  = new CachedData(0, value, getMaxSize)
  def decode(data: CachedData):         Array[Byte] = data.getData
  def getMaxSize:                       Int         = CachedData.MAX_SIZE
}
