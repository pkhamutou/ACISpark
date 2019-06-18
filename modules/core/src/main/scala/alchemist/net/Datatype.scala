package alchemist.net

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.values.{ ByteEnum, ByteEnumEntry }

private[alchemist] sealed abstract class Datatype(override val value: Byte, val label: String)
    extends ByteEnumEntry
    with UpperSnakecase

private[alchemist] object Datatype extends ByteEnum[Datatype] {

  override val values: scala.collection.immutable.IndexedSeq[Datatype] = findValues

  final case object None extends Datatype(0, "NONE")

  final case object Byte  extends Datatype(33, "BYTE")
  final case object Short extends Datatype(34, "SHORT")
  final case object Int   extends Datatype(35, "INT")
  final case object Long  extends Datatype(36, "LONG")

  final case object Float  extends Datatype(15, "FLOAT")
  final case object Double extends Datatype(16, "DOUBLE")

  final case object Char   extends Datatype(1, "CHAR")
  final case object String extends Datatype(46, "STRING")

  final case object CommandCode extends Datatype(48, "COMMAND CODE")
  final case object LibraryID   extends Datatype(49, "LIBRARY ID")
  final case object GroupID     extends Datatype(50, "GROUP ID")
  final case object WorkerID    extends Datatype(51, "WORKER ID")
  final case object WorkerInfo  extends Datatype(52, "WORKER INFO")
  final case object MatrixID    extends Datatype(53, "MATRIX ID")
  final case object MatrixInfo  extends Datatype(54, "MATRIX INFO")
  final case object MatrixBlock extends Datatype(55, "MATRIX BLOCK")
  final case object IndexedRow  extends Datatype(56, "INDEXED ROW")

  final case object Parameter extends Datatype(100, "PARAMETER")
}
