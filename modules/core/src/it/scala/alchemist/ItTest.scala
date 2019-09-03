package alchemist

import cats.effect.{Concurrent, ContextShift, Effect, IO, Timer}

import cats.Traverse
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration.DurationInt

import org.apache.spark.rdd.RDD

import alchemist.library.Param

class ItTest extends WordSpec with Matchers with DataFrameSuiteBase {

  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.global)

  "it" should {
    "work" in {

      import org.apache.spark.mllib.linalg.DenseVector
      import org.apache.spark.mllib.linalg.distributed.{ IndexedRow, IndexedRowMatrix }

      def getMatrix(rows: Long, cols: Long): IndexedRowMatrix = {
        val r = new scala.util.Random(1000L)

        new IndexedRowMatrix(
          spark
            .range(rows)
            .rdd
            .map(row => IndexedRow(row, new DenseVector(Array.fill(cols.toInt)(r.nextDouble()))))
            .repartition(4)
        )
      }

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

      def printRows(irows: RDD[IndexedRow]): Unit = {
        irows.mapPartitionsWithIndex { case (i, rows) =>
          val p = rows.toList
          println(s"PARTITION: $i $p")
          List().toIterator
        }.count()
      }

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
          _   = println(rargs)
          _   = println("Start sending matrix ...")
          _   = println("\n\n\n\n\n\n\n\n")
          indexRows = getMatrix(20, 5)
          matrix <- session.getMatrixHandle(indexRows)
          _ = println(matrix)
          hs <- session.sendIndexedRowMatrix(matrix, indexRows)
          _ = println(hs)
          _ = println("--------------- MATRIX WAS SENT -------------------")
          r <- session.getIndexRowMatrix(spark, matrix).flatMap(rmx => IO.pure((indexRows.rows, rmx)))
          _ = println("\n comparison \n")
          _ = println(r._1.count)
          _ = println(r._2.count)
          _ = println("\n\n")
          _ = r._1.foreach(println)
          _ = println()
          _ = r._2.foreach(println)
        } yield ()
      }

      prg.unsafeRunSync()
    }
  }
}
/*

IndexedRow(8,[0.4457367367074283,0.6008140654988429,0.550376169584217,0.6580583901495688,0.9744965039734514])
IndexedRow(8,[0.4457367367074283,0.6008140654988429,0.550376169584217,0.6580583901495688,0.9744965039734514])
 */
