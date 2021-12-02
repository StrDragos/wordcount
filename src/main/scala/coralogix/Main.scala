package coralogix

import zio._
import zio.console._

object Main extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
      Counter.read()
        .via(Counter.split)
        .via(Counter.flatten)
        .via(Counter.deserialize)
        .tap(r => putStrLn(s"Result received ${r}"))
        .runDrain
        .exitCode
  }
  //        .tap(s => putStrLn(s))
//        .runDrain
//        .exitCode
}
