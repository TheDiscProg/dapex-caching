package simex.caching

import cats.Applicative
import cats.syntax.all._
import com.hazelcast.map.IMap
import io.circe.parser._
import io.circe.syntax._
import io.github.thediscprog.simexmessaging.messaging.Simex
import org.typelevel.log4cats.Logger

import java.util.concurrent.TimeUnit

case class HazelcastCachingService[F[_]: Applicative: Logger](
    map: IMap[String, String],
    ttl: Long
) extends CachingServiceAlgebra[F] {

  override def saveMessage(key: String, message: Simex): F[Unit] =
    for {
      _ <- Logger[F].info(s"Hzcast Saving Token: $key")
      _ = map.put(key, message.asJson.noSpaces, ttl, TimeUnit.SECONDS)
    } yield ()

  override def getMessage(key: String): F[Option[Simex]] =
    Logger[F].info(s"Hzcast Getting by token: $key") *>
      getSimexMessageFromCache(key)

  private def getSimexMessageFromCache(
      key: String
  ): F[Option[Simex]] =
    if (map.containsKey(key)) {
      val jsonStr = map.get(key)
      decode[Simex](jsonStr) match {
        case Left(e) =>
          Logger[F].warn(s"Hzcast decoding problem: ${e.getMessage}") *>
            (None: Option[Simex]).pure[F]
        case Right(value) => (Some(value): Option[Simex]).pure[F]
      }
    } else {
      Logger[F].warn(s"Hzcast Key [$key] not found in map: ${map.getName}") *>
        (None: Option[Simex]).pure[F]
    }

  override def deleteMessage(key: String): F[Unit] =
    if (map.containsKey(key)) {
      for {
        _ <- Logger[F].info(s"Hzcast Deleting by token: $key")
        _ = map.delete(key)
      } yield ()
    } else {
      ().pure[F]
    }
}
