package coralogix.counter

import zio.Schedule
import zio.duration._
import zio.stream.ZTransducer
import cats.implicits._

import models.Event

object Counter {

  def start =
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
}
