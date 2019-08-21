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
) {

  def getColumnAssignments(workerId: Short): Array[Long] =
    if (numOfColumns == 1)
      Array(0, 0, 1)
    else if (layout == Layout.MC_MR)
      Array(grid.array(workerId)(0), numOfColumns - 1, grid.numOfColumns)
    else if (layout == Layout.MR_MC)
      Array(grid.array(workerId)(1), numOfColumns - 1, grid.numOfRows)
    else if (layout == Layout.VC_STAR)
      Array(grid.array(workerId)(0), numOfColumns - 1, 1)
    else if (layout == Layout.VR_STAR)
      Array(grid.array(workerId)(0), numOfColumns - 1, 1)
    else ???

  def getRowAssignments(workerId: Short): Array[Long] =
    if (numOfRows == 1)
      Array(0, 0, 1)
    else {
      if (layout == Layout.MC_MR)
        Array(grid.array(workerId)(0), numOfRows - 1, grid.numOfRows)
      else if (layout == Layout.MR_MC)
        Array(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns)
      else if (layout == Layout.VC_STAR)
        Array(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns * grid.numOfRows)
      else if (layout == Layout.VR_STAR)
        Array(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns * grid.numOfRows)
      else ???
    }
}

object Matrix {
  final case class MatrixId(value: Short)

  final case class ProcessGrid(numOfRows: Short, numOfColumns: Short, array: Map[Short, Array[Short]]) {
    override def toString: String =
      s"ProcessGrid($numOfRows,$numOfColumns,${array.mapValues(_.mkString("Array(", ",", ")"))})"
  }

}
