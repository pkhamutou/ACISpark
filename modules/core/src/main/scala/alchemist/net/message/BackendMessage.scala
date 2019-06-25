package alchemist.net.message

import scodec.Decoder

import alchemist.net.message.backend.{ HandshakeOk, ListAllWorkers }

trait BackendMessage

object BackendMessage {

  def decode(c: Command): Decoder[BackendMessage] = c match {
    case Command.Handshake      => HandshakeOk.decoder
    case Command.ListAllWorkers => ListAllWorkers.decoder

    case a => {
      println(a)
      throw new Exception("oh")
    }
  }
}
