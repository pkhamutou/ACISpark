package alchemist.net.message.backend

import scodec._
import scodec.codecs._

import alchemist.data.Worker
import alchemist.net.codecs.{alchemistShortCodec, alchemistWorkerCodec}
import alchemist.net.message.BackendMessage

final case class ListWorkers(numWorkers: Short, workers: List[Worker]) extends BackendMessage

object ListWorkers {

  val decoder: Decoder[ListWorkers] = {
    "list_all_workers" |
      ("num_workers" | alchemistShortCodec).flatPrepend { numWorkers =>
        ("workers" | listOfN[Worker](provide(numWorkers), alchemistWorkerCodec)).hlist
      }
    }.as[ListWorkers].decodeOnly

}
