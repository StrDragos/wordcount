package coralogix

import zio.{Schedule, ZIO}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.process.Command
import zio.stream.{ZStream, ZTransducer}
import zio.duration._
import zio.console._

import io.circe.{Error, parser}
import coralogix.models._

object Counter {
  def read(): ZStream[Blocking, Throwable, String] = {
    Command("src/main/resources/blackbox.macosx")
      .linesStream
  }
  def split(stream: ZStream[Blocking, Throwable, String]): ZStream[Blocking, Throwable, List[String]] =
    stream.map(_.split('\n').toList)


  def flatten(stream: ZStream[Blocking, Throwable, List[String]]): ZStream[Blocking, Throwable, String] =
    stream.flatMap(l => ZStream.fromIterator(l.iterator))

  def deserialize(stream: ZStream[Blocking, Throwable, String]): ZStream[Blocking with Console, Throwable, Event] = {
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
}
