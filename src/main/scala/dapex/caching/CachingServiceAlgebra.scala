package dapex.caching

import dapex.messaging.DapexMessage

trait CachingServiceAlgebra[F[_]] {

  def saveMessage(key: String, dapexMessage: DapexMessage): F[Unit]

  def getMessage(key: String): F[Option[DapexMessage]]

}
