package alchemist

import cats.effect.IO

import org.scalatest.{Matchers, WordSpec}

class ItTest extends WordSpec with Matchers {

  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  "it" should {
    "work" in {
      val prg = AlchemistSession.make[IO]("localhost", 24960).use { session =>
        for {
          _ <- session.listAllWorkers().map(println)
          _ <- session.listInactiveWorkers().map(println)
          _ <- session.listActiveWorkers().map(println)
          _ <- session.listAssignedWorkers().map(println)
          _ <- session.requestWorkers(2).map(s => println(s"Requested workers: $s"))
          _ <- session.listRequestedWorkers().map(s => println(s"Listed workers: $s"))
          _ <- session.listAllWorkers().map(println)
          _ <- session.listInactiveWorkers().map(println)
          _ <- session.listActiveWorkers().map(println)
          _ <- session.listAssignedWorkers().map(println)
          testString = "This is a test string from a Spark application"
          _ <- session.sendTestString(testString).map(println)
        } yield ()
      }

      prg.unsafeRunSync()
    }
  }
}
