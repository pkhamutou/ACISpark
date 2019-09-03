package alchemist.data

final case class MatrixBlock(
  row: RowInfo,
  column: ColumnInfo,
  data: Vector[Double]
)

object MatrixBlock {

  def apply(rows: Array[Long], columns: Array[Long], data: Array[Double]): MatrixBlock =
    MatrixBlock(RowInfo(rows(0), rows(1), rows(2)), ColumnInfo(columns(0), columns(1), columns(2)), data.toVector)
}
