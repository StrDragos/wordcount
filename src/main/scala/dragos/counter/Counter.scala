package dragos.counter

import zio._
import zio.console.Console
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.stream._

import cats.implicits._

import dragos.counter.models.Event

trait Counter {
  def start(): ZStream[Any, Throwable, Map[Types, Words]]
}

private case class CounterLive (blocking: Blocking.Service, console: Console.Service, clock: Clock.Service) extends Counter {
  override def start() = {
    read()
      .via(split)
      .via(flatten)
      .via(deserialize)
      .aggregateAsyncWithin(
        ZTransducer.foldLeft(List.empty[Event])((l, e) => e :: l),
        Schedule.fixed(5.second)
      ).scan(Map.empty[Types, Words])(
      (s, events) => processEvents(events).combine(s)
    )
  }.provideLayer(ZLayer.succeed(blocking) ++ ZLayer.succeed(console) ++ ZLayer.succeed(clock))
}

object Counter {
  val layer: URLayer[Has[Blocking.Service] with Has[Console.Service] with Has[Clock.Service], Has[Counter]] =
    (CounterLive(_, _, _)).toLayer

  def start() = ZStream.serviceWithStream[Counter](_.start())
}
