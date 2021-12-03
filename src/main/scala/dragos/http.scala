package dragos

import zio.stream.ZStream

import dragos.counter.Counter
import zhttp.http._
import io.circe.syntax._

object http {
  val counterRoute = HttpApp.collect{
    case Method.GET -> Root / "count" =>
      val stream = for {
        counts <- Counter.start().map(v => v.asJson.toString().getBytes(HTTP_CHARSET))
        out <- ZStream.fromIterable(counts)
      } yield out

      Response.http(
        status = Status.OK,
        headers = List(Header.contentLength(Long.MaxValue), Header.contentTypeJson),
        content = HttpData.fromStream(stream)
      )
  }

}
