package alchemist.net.message

import scodec.Decoder

import alchemist.net.message.backend.{ HandshakeOk, ListWorkers }

trait BackendMessage

object BackendMessage {

  def decode(c: Command): Decoder[BackendMessage] = c match {
    case Command.Handshake => HandshakeOk.decoder

    case Command.ListAllWorkers      => ListWorkers.decoder
    case Command.ListInactiveWorkers => ListWorkers.decoder
    case Command.ListActiveWorkers   => ListWorkers.decoder
    case Command.ListAssignedWorkers => ListWorkers.decoder
    case Command.RequestWorkers      => ListWorkers.decoder

    case c => throw new NotImplementedError(s"No BackendMessage.decoder implemented for $c")
  }
}
