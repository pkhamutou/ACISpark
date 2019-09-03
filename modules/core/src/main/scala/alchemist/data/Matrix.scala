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

  def getColumnAssignments(workerId: Short): ColumnInfo =
    if (numOfColumns == 1)
      ColumnInfo(0, 0, 1)
    else if (layout == Layout.MC_MR)
      ColumnInfo(grid.array(workerId)(0), numOfColumns - 1, grid.numOfColumns)
    else if (layout == Layout.MR_MC)
      ColumnInfo(grid.array(workerId)(1), numOfColumns - 1, grid.numOfRows)
    else if (layout == Layout.VC_STAR)
      ColumnInfo(grid.array(workerId)(0), numOfColumns - 1, 1)
    else if (layout == Layout.VR_STAR)
      ColumnInfo(grid.array(workerId)(0), numOfColumns - 1, 1)
    else ???

  def getRowAssignments(workerId: Short): RowInfo =
    if (numOfRows == 1)
      RowInfo(0, 0, 1)
    else {
      if (layout == Layout.MC_MR)
        RowInfo(grid.array(workerId)(0), numOfRows - 1, grid.numOfRows)
      else if (layout == Layout.MR_MC)
        RowInfo(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns)
      else if (layout == Layout.VC_STAR)
        RowInfo(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns * grid.numOfRows)
      else if (layout == Layout.VR_STAR)
        RowInfo(grid.array(workerId)(1), numOfRows - 1, grid.numOfColumns * grid.numOfRows)
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
