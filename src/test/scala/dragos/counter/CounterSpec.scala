package dragos.counter

import zio.blocking.Blocking
import zio.console.Console
import zio.stream.ZStream
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Chunk, ZIO}

import models.Event

object CounterSpec extends DefaultRunnableSpec {
  val rawInput = """{ "event_type": "baz", "data": "amet", "timestamp": 1638457033 }
                |{ "����5S5B"�
                |W�M�t�
                |{ "G\���W"�
                |""".stripMargin

  val listOfMsgs = List(
    """{ "event_type": "baz", "data": "amet", "timestamp": 1638457033 }""",
    """{ "����5S5B"�""",
    """W�M�t�""",
    """{ "G\���W"�"""
  )

  override def spec = suite("Counter")(
    testM("should split lines"){
      val inStream = ZStream.fromEffect(ZIO.succeed(rawInput))
      assertM(split(inStream).runCollect)(equalTo(Chunk(listOfMsgs)))
        .provideLayer(Blocking.live)
    },
    testM("should not fail when invalid data is received"){
      val inStream = ZStream.fromIterable(listOfMsgs.map(Line))
      val expected = Event(eventType = "baz", data = "amet", timestamp = 1638457033)

      assertM(deserialize(inStream).runCollect)(equalTo(Chunk(expected)))
        .provideLayer(Blocking.live ++ Console.live)
    }
  )
}
