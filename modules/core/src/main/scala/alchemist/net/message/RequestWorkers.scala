package alchemist.net.message

import scodec._
import scodec.codecs._

import alchemist.net.codecs.alchemistShortCodec

final case class RequestWorkers(numWorkers: Short)

object RequestWorkers {

  implicit val encoder: Encoder[RequestWorkers] =
    ("request_workers" | ("num_workers" | alchemistShortCodec)).as[RequestWorkers].encodeOnly

}
