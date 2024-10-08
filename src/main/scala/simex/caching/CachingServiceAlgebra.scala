package simex.caching

import io.github.thediscprog.simexmessaging.messaging.Simex

trait CachingServiceAlgebra[F[_]] {

  def saveMessage(key: String, message: Simex): F[Unit]

  def getMessage(key: String): F[Option[Simex]]

  def deleteMessage(key: String): F[Unit]

}
