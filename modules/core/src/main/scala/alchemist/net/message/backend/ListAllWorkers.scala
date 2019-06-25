package alchemist.net.message.backend

import scodec._
import scodec.codecs._

import alchemist.data.Worker
import alchemist.net.codecs.{alchemistShortCodec, alchemistWorkerCodec}
import alchemist.net.message.BackendMessage

final case class ListAllWorkers(numWorkers: Short, workers: List[Worker]) extends BackendMessage

object ListAllWorkers {

  val decoder: Decoder[ListAllWorkers] = {
    "list_all_workers" |
      ("num_workers" | alchemistShortCodec).flatPrepend { numWorkers =>
        ("workers" | listOfN[Worker](provide(numWorkers), alchemistWorkerCodec)).hlist
      }
    }.as[ListAllWorkers].decodeOnly

}
