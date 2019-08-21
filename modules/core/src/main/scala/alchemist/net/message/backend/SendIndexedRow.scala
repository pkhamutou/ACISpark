package alchemist.net.message.backend

import scodec.Decoder

import alchemist.net.message.BackendMessage

final case class SendIndexedRow(value: Long) extends BackendMessage

object SendIndexedRow {
  val decoder: Decoder[SendIndexedRow] = scodec.codecs.long(64).as[SendIndexedRow].asDecoder
}
