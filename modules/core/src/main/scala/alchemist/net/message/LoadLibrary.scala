package alchemist.net.message

import scodec.Encoder
import scodec.codecs._

import alchemist.net.codecs.alchemistStringCodec

final case class LoadLibrary(name: String, path: String)

object LoadLibrary {

  implicit val encoder: Encoder[LoadLibrary] =
    "load_library" | {
      ("name" | alchemistStringCodec) ::
      ("path" | alchemistStringCodec)
    }.as[LoadLibrary].encodeOnly
}
