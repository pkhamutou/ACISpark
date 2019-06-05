package alchemist

import cats.effect.IO
import org.scalatest.{ FunSuite, Matchers }

class AlchemistSessionTest extends FunSuite with Matchers {

  test("AlchemistSession must be created and closed after calling .use") {
    import java.net.ServerSocket

    val port = 24960

    val ss = new ServerSocket(port)


    AlchemistSession.make[IO]("localhost", port).use(as => IO.pure("created")).unsafeRunSync()

    ss.close()
  }
}
