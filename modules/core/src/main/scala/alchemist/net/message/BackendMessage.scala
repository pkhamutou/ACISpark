package alchemist.net.message

import scodec.Decoder

import alchemist.net.message.backend._

trait BackendMessage extends Serializable

object BackendMessage {

  def decode(c: Command): Decoder[BackendMessage] = c match {
    case Command.Handshake => HandshakeOk.decoder

    case Command.ListAllWorkers      => ListWorkers.decoder
    case Command.ListInactiveWorkers => ListWorkers.decoder
    case Command.ListActiveWorkers   => ListWorkers.decoder
    case Command.ListAssignedWorkers => ListWorkers.decoder
    case Command.RequestWorkers      => ListWorkers.decoder
    case Command.SendTestString      => SingleString.decoder
    case Command.LoadLibrary         => GetLibraryId.decoder
    case Command.RunTask             => alchemist.net.message.backend.RunTask.decoder
    case Command.MatrixInfo          => alchemist.net.message.backend.GetMatrixHandle.decoder
    case Command.SendMatrixBlocks    => alchemist.net.message.backend.SendIndexedRow.decoder
    case Command.RequestMatrixBlocks => alchemist.net.message.backend.RequestMatrixBlock.decoder

    case c => throw new NotImplementedError(s"No BackendMessage.decoder implemented for $c")
  }
}
