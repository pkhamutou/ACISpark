package alchemist.net.message

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.values.{ByteEnum, ByteEnumEntry}
import scodec.{Attempt, Codec, Err}
import scodec.codecs.byte

sealed abstract class Command(override val value: Byte) extends ByteEnumEntry with UpperSnakecase with Serializable

object Command extends ByteEnum[Command] {

  override val values: scala.collection.immutable.IndexedSeq[Command] = findValues

  final case object Wait extends Command(0)

  // Connection
  final case object Handshake         extends Command(1)
  final case object RequestId         extends Command(2)
  final case object ClientInfo        extends Command(3)
  final case object SendTestString    extends Command(4)
  final case object RequestTestString extends Command(5)
  final case object CloseConnection   extends Command(6)

  // Workers
  final case object RequestWorkers          extends Command(11)
  final case object YieldWorkers            extends Command(12)
  final case object SendAssignedWorkersInfo extends Command(13)
  final case object ListAllWorkers          extends Command(14)
  final case object ListActiveWorkers       extends Command(15)
  final case object ListInactiveWorkers     extends Command(16)
  final case object ListAssignedWorkers     extends Command(17)

  // Libraries
  final case object ListAvailableLibraries extends Command(21)
  final case object LoadLibrary            extends Command(22)
  final case object UnloadLibrary          extends Command(23)

  // Arrays
  final case object MatrixInfo          extends Command(31)
  final case object MatrixLayout        extends Command(32)
  final case object SendIndexedRows     extends Command(33)
  final case object SendMatrixBlocks    extends Command(34)
  final case object RequestIndexedRows  extends Command(35)
  final case object RequestMatrixBlocks extends Command(36)

  // Tasks
  final case object RunTask extends Command(41)

  // Shutting down
  final case object Shutdown extends Command(99)

  private val className: String = Command.getClass.getName.dropRight(1)

  implicit val codec: Codec[Command] = byte.narrow[Command](
    b => Attempt.fromOption(Command.withValueOpt(b), Err(s"$b is not a member of $className enum")),
    _.value
  )

}
