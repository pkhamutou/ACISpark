package alchemist

import cats.effect.{ ExitCode, IO, IOApp }

object ConnectionTest extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    AlchemistSession
      .make[IO]("localhost", 24960)
      .use { session =>
        IO.unit
      }
      .map(_ => ExitCode.Success)
}
