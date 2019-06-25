package alchemist

import cats.effect.IO

import org.scalatest.{Matchers, WordSpec}

class ItTest extends WordSpec with Matchers {

  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  "it" should {
    "work" in {
      val prg = AlchemistSession.make[IO]("localhost", 24960).use { session =>
        IO.unit
      }

      prg.unsafeRunSync()
    }
  }
}
