package dragos.counter

import io.circe.generic.extras._

object models {
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec
  final case class Event(eventType: String, data: String, timestamp: Long)
}
