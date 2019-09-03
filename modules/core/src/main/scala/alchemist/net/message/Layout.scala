package alchemist.net.message

import enumeratum.EnumEntry
import enumeratum.EnumEntry.Uppercase
import enumeratum.values.{ ByteEnum, ByteEnumEntry }

private[alchemist] sealed abstract class Layout(override val value: Byte)
    extends ByteEnumEntry
    with Uppercase
    with Slashcase
    with Serializable

private[alchemist] object Layout extends ByteEnum[Layout] {

  override val values: scala.collection.immutable.IndexedSeq[Layout] = findValues

  final case object MC_MR     extends Layout(0)
  final case object MC_STAR   extends Layout(1)
  final case object MD_STAR   extends Layout(2)
  final case object MR_MC     extends Layout(3)
  final case object MR_STAR   extends Layout(4)
  final case object STAR_MC   extends Layout(5)
  final case object STAR_MD   extends Layout(6)
  final case object STAR_MR   extends Layout(7)
  final case object STAR_STAR extends Layout(8)
  final case object STAR_VC   extends Layout(9)
  final case object STAR_VR   extends Layout(10)
  final case object VC_STAR   extends Layout(11)
  final case object VR_STAR   extends Layout(12)
  final case object CIRC_CIRC extends Layout(13)
}

trait Slashcase extends EnumEntry {
  override def entryName: String = stableEntryName

  private[this] lazy val stableEntryName: String = super.entryName.replaceAll("_", "/")
}
