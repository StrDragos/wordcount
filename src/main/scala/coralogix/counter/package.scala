package coralogix

import zio.ZIO
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}
import zio.process.Command
import zio.stream.ZStream

import coralogix.counter.models.Event
import io.circe.parser

package object counter {
  type Words = Map[String, Int]
  type Types = String

  private[counter] def read(): ZStream[Blocking, Throwable, String] =
    Command("src/main/resources/blackbox.macosx")
      .linesStream

  private[counter] def split(stream: ZStream[Blocking, Throwable, String]): ZStream[Blocking, Throwable, List[String]] =
    stream.map(_.split('\n').toList)

  private[counter] def flatten(stream: ZStream[Blocking, Throwable, List[String]]): ZStream[Blocking, Throwable, String] =
    stream.flatMap(l => ZStream.fromIterator(l.iterator))

  private[counter] def deserialize(stream: ZStream[Blocking, Throwable, String]): ZStream[Blocking with Console, Throwable, Event] = {
    for {
      in <- stream
      deserialized = parser.decode[Event](in)
      eff = deserialized.fold(
        err => putStrLn(s"Failed to deserialize message $in, err ${err}") *> ZIO.none,
        v => ZIO.some(v)
      )
      out <- ZStream.fromEffect(eff).filter(_.isDefined).map(_.get)
    } yield out
  }

  def groupEvents(events: List[Event]) =
    events.groupBy(_.eventType)

  def countWords(events: List[Event]) =
    events.groupBy(_.data).map{
      case d -> e => (d, e.size)
    }

  def processEvents(events: List[Event]) =
    groupEvents(events).map{
      case k -> e => (k, countWords(e))
    }
}
