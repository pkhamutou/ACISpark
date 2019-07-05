package alchemist

import cats.effect.IO

import org.scalatest.{ Matchers, WordSpec }

import alchemist.library.Param

class ItTest extends WordSpec with Matchers {

  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  "it" should {
    "work" in {
      val args: List[Param] = List(
        Param[Byte]("in_byte", 9),
        Param("in_char", 'y'),
        Param[Short]("in_short", 9876),
        Param[Int]("in_int", 987654321),
        Param("in_long", 98765432123456789L),
        Param("in_float", 77.77777777f),
        Param("in_double", 88.88888888888888888d),
        Param("in_string", "test string")
      )
      args.foreach(println)
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
          _   <- session.sendTestString(testString).map(println)
          lib <- session.loadLibrary("TestLib", "/usr/local/TestLib/target/testlib.so")
          _ = println(lib)
          rargs <- session.runTask(lib, "greet", args)
          _ = println(rargs)
        } yield ()
      }

      prg.unsafeRunSync()
    }
  }
}
