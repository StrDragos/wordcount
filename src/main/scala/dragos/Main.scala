package dragos

import zio._
import zio.console._

import dragos.counter.Counter
import zhttp.service.Server

object Main extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    (for {
      _ <- putStrLn("Server started")
      s <-  Server.start(8080, http.counterRoute)
     } yield s).exitCode
  }.provideLayer(ZEnv.live ++ Counter.layer)
}
