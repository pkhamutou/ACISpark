package alchemist.data

final case class RowInfo(start: Long, end: Long, step: Long)

object RowInfo {
  def single(a: Long): RowInfo = RowInfo(a, a, 1)
}
