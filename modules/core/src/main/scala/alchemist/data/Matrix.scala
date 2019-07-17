package alchemist.data

import alchemist.net.message.Layout

final case class Matrix(
  id: Matrix.MatrixId,
  matrixName: String,
  numOfRows: Long,
  numOfColumns: Long,
  sparse: Byte,
  layout: Layout,
  grid: Matrix.ProcessGrid
)

object Matrix {
  final case class MatrixId(value: Short)

  final case class ProcessGrid(numOfRows: Short, numOfColumns: Short, array: Map[Short, Array[Short]]) {
    override def toString: String =
      s"ProcessGrid($numOfRows,$numOfColumns,${array.mapValues(_.mkString("Array(", ",", ")"))})"
  }

}
