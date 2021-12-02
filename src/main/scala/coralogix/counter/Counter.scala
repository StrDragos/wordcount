package coralogix.counter

import zio.Schedule
import zio.duration._
import zio.stream.ZTransducer
import cats.implicits._

import coralogix.models.Event

object Counter {

  def groupEvents(events: List[Event]) =
    events.groupBy(_.eventType)

  def countWords(events: List[Event]) =
    events.groupBy(_.data).map{
      case d -> e => (d, e.size)
    }

  def start =
    read()
      .via(split)
      .via(flatten)
      .via(deserialize)
      .aggregateAsyncWithin(
        ZTransducer.foldLeft(List.empty[Event])((l, e) => e :: l),
        Schedule.fixed(5.second)
      ).scan(Map.empty[String, Map[String, Int]])(
      (s, events) =>{
        val newGroup = groupEvents(events).map{
          case k -> e => (k, countWords(e))
        }
        newGroup.combine(s)
      }
    )
}
