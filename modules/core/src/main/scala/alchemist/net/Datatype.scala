package alchemist.net

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.values.{ ByteEnum, ByteEnumEntry }

private[alchemist] sealed abstract class Datatype(override val value: Byte) extends ByteEnumEntry with UpperSnakecase

private[alchemist] object Datatype extends ByteEnum[Datatype] {

  override val values: scala.collection.immutable.IndexedSeq[Datatype] = findValues

  final case object None extends Datatype(0)

  final case object Byte  extends Datatype(33)
  final case object Short extends Datatype(34)
  final case object Int   extends Datatype(35)
  final case object Long  extends Datatype(36)

  final case object Float  extends Datatype(15)
  final case object Double extends Datatype(16)

  final case object Char   extends Datatype(1)
  final case object String extends Datatype(46)

  final case object CommandCode extends Datatype(48)
  final case object LibraryId   extends Datatype(49)
  final case object GroupId     extends Datatype(50)
  final case object WorkerId    extends Datatype(51)
  final case object WorkerInfo  extends Datatype(52)
  final case object MatrixId    extends Datatype(53)
  final case object MatrixInfo  extends Datatype(54)
  final case object MatrixBlock extends Datatype(55)
  final case object IndexedRow  extends Datatype(56)

  final case object Parameter extends Datatype(100)
}
