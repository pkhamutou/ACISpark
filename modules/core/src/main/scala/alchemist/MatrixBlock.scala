package alchemist

final case class MatrixBlock(
  data: Array[Double],
  rows: Array[Long],
  columns: Array[Long]
)
