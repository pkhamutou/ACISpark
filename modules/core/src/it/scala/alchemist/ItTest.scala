package alchemist

import cats.effect.IO

import org.scalatest.{Matchers, WordSpec}

class ItTest extends WordSpec with Matchers {

  "it" should {
    "work" in {
      val prg = AlchemistSession.make[IO]("localhost", 24960).use { session =>

        IO.delay(session.handshake())
      }

      prg.unsafeRunSync()
    }
  }
}
