package alchemist

import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix}
import scala.math.max
import org.apache.spark.sql.SparkSession

class ArrayHandle(val id: ArrayID = ArrayID(0), val name: String = "", val numRows: Long = 0, val numCols: Long = 0,
                  val sparse: Byte = 0, val layout: Byte = 0, val numPartitions: Short = 0,
                  val workerAssignments: Map[Short, Long] = Map.empty[Short, Long]) {

  def getID: ArrayID = id

  def getName: String = name

  def getNumRows: Long = numRows

  def getNumCols: Long = numCols

  def getDimensions = (numRows, numCols)

  def getSparse: Byte = sparse

  def getNumPartitions: Short = numPartitions

  def getWorkerAssignments: Map[Short, Long] = workerAssignments

  def getIndexedRowMatrix: IndexedRowMatrix = AlchemistSession.getIndexedRowMatrix(this)

  def toString(displayLayout: Boolean = false): this.type = {

    println(s"Name:                  $name")
    println(s"ID:                    ${id.value}")
    println(" ")
    println(s"Number of rows:        $numRows")
    println(s"Number of columns:     $numCols")
    println(" ")
    println(s"Sparse:                $sparse")
    println(s"Number of partitions:  $numPartitions")
    if (displayLayout) {
      print(" ")
      print(s"Layout:")
      workerAssignments foreach { case(k, v) => println(s"    ${k} ${v}")}
    }

    this
  }
}