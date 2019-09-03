package alchemist.net.message.backend

import scodec.Decoder

import alchemist.data.Library
import alchemist.net.codecs.alchemistLibraryIdCodec
import alchemist.net.message.BackendMessage

final case class GetLibraryId(value: Library.LibraryId) extends BackendMessage

object GetLibraryId {
  val decoder: Decoder[GetLibraryId] = alchemistLibraryIdCodec.as[GetLibraryId].decodeOnly
}

