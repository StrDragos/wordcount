package coralogix

import zio.ZIO
import zio.blocking.Blocking
import zio.console.putStrLn
import zio.process.Command
import zio.stream.ZStream

import coralogix.counter.models.Event
import io.circe.parser

package object counter {
  type Words = Map[String, Int]
  type Types = String

  final case class Line(value: String)

  private[counter] def read() =
    Command("src/main/resources/blackbox.macosx")
      .linesStream

  private[counter] def split[R](stream: ZStream[Blocking, Throwable, String]) =
    stream.map(_.split('\n').toList)

  private[counter] def flatten[R](stream: ZStream[Blocking, Throwable, List[String]]) =
    stream.flatMap(l => ZStream.fromIterator(l.iterator.map(Line)))

  private[counter] def deserialize[R](stream: ZStream[Blocking, Throwable, Line]) = for {
      in <- stream
      deserialized = parser.decode[Event](in.value)
      eff = deserialized.fold(
        err => putStrLn(s"Failed to deserialize message $in, err ${err}") *> ZIO.none,
        v => ZIO.some(v)
      )
      out <- ZStream.fromEffect(eff).filter(_.isDefined).map(_.get)
    } yield out

  def groupEvents(events: List[Event]) =
    events.groupBy(_.eventType)

  def countWords(events: List[Event]) =
    valueMap(events.groupBy(_.data))(e => e.size)

  def processEvents(events: List[Event]) =
    valueMap(groupEvents(events))(countWords)

  def valueMap[K, V, B](value: Map[K, V])(f: V =>  B) = value.map{
      case k -> v =>  (k, f(v))
    }
}
