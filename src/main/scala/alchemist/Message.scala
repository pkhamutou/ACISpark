package alchemist

//import java.util._
import java.io._
import java.nio.charset.Charset
import java.nio.{ByteBuffer, ByteOrder}
import java.util.{Arrays, Collections}

object Commands {
  val commands = Map[String, Byte]("WAIT" -> 0, "HANDSHAKE" -> 1, "REQUEST_ID" -> 2, "CLIENT_INFO" -> 3,
    "SEND_TEST_STRING" -> 4, "REQUEST_TEST_STRING" -> 5, "REQUEST_WORKERS" -> 6, "YIELD_WORKERS" -> 7,
    "SEND_ASSIGNED_WORKERS_INFO" -> 8, "LIST_ALL_WORKERS" -> 9, "LIST_ACTIVE_WORKERS" -> 10,
    "LIST_INACTIVE_WORKERS" -> 11, "LIST_ASSIGNED_WORKERS" -> 12, "LOAD_LIBRARY" -> 13, "RUN_TASK" -> 14,
    "UNLOAD_LIBRARY" -> 15, "MATRIX_INFO" -> 16, "MATRIX_LAYOUT" -> 17,
    "SEND_MATRIX_BLOCKS" -> 18, "REQUEST_MATRIX_BLOCKS" -> 19, "SHUT_DOWN" -> 20)

  def getName(v: Byte): String = commands.find(_._2 == v).map(_._1).get

  def getCode(v: String): Byte = commands(v)
}

object Datatypes {
  val datatypes = Map[String, Byte]("NONE" -> 0, "BYTE" -> 18, "SHORT" -> 34, "INT" -> 35,
    "LONG" -> 36, "FLOAT" -> 15, "DOUBLE" -> 16, "CHAR" -> 5, "STRING" -> 47, "COMMAND_CODE" -> 47)

  def getName(v: Byte): String = datatypes.find(_._2 == v).map(_._1).get

  def getCode(v: String): Byte = datatypes(v)
}

class Message() {

  val headerLength: Byte = 9
  var maxBodyLength: Int = 10000000

  val messageBuffer = ByteBuffer.allocate(headerLength + maxBodyLength).order(ByteOrder.BIG_ENDIAN)
  val tempBuffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)

  var clientID: Short = 0
  var sessionID: Short = 0
  var commandCode: Byte = Commands.getCode("WAIT")
  var bodyLength: Int = 0

  // For writing data
  var currentDatatype: Byte = Datatypes.getCode("NONE")
  var currentDatatypeCount: Int = 0
  var currentDatatypeCountMax: Int = 0
  var currentDatatypeCountPos: Int = headerLength + 1

  var readPos: Int = headerLength                // for reading data
  var writePos: Int = headerLength               // for writing data

  def reset(): this.type = {

    commandCode = Commands.getCode("WAIT")
    bodyLength = 0

    currentDatatype = Datatypes.getCode("NONE")
    currentDatatypeCount = 0
    currentDatatypeCountMax = 0
    currentDatatypeCountPos = headerLength + 1

    readPos = headerLength
    writePos = headerLength

    this
  }

  // Reading data
  def readClientID(): Short = {
    ByteBuffer.wrap(messageBuffer.array.slice(0, 2)).order(ByteOrder.BIG_ENDIAN).getShort
  }

  def readSessionID(): Short = {
    ByteBuffer.wrap(messageBuffer.array.slice(2, 4)).order(ByteOrder.BIG_ENDIAN).getShort
  }

  def readCommandCode(): Byte = {
    messageBuffer.get(4)
  }

  def readBodyLength(): Int = {
    ByteBuffer.wrap(messageBuffer.array.slice(5, 9)).order(ByteOrder.BIG_ENDIAN).getInt
  }

  def readHeader(): this.type = {
    clientID = readClientID
    sessionID = readSessionID
    commandCode = readCommandCode
    bodyLength = readBodyLength
    readPos = headerLength
    writePos = headerLength

    this
  }

  def readNextDatatype(): this.type = {
    currentDatatype = messageBuffer.get(readPos)
    currentDatatypeCountMax = ByteBuffer.wrap(messageBuffer.array.slice(readPos + 1, readPos+5)).order(ByteOrder.BIG_ENDIAN).getInt
    currentDatatypeCount = 0
    readPos += 5

    this
  }

  def getCurrentDatatype(): Byte = {
    currentDatatype
  }

  def getCurrentDatatypeName(): String = {
    Datatypes.getName(currentDatatype)
  }

  def getCurrentDatatypeCount(): Int = {
    currentDatatypeCountMax
  }

  def readByte(): Byte = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 1
    currentDatatypeCount += 1
    messageBuffer.get(readPos - 1)
  }

  def readChar(): Int = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 2
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 2, readPos)).order(ByteOrder.BIG_ENDIAN).getChar
  }

  def readShort(): Short = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 2
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 2, readPos)).order(ByteOrder.BIG_ENDIAN).getShort
  }

  def readInt(): Int = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 4
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 4, readPos)).order(ByteOrder.BIG_ENDIAN).getInt
  }

  def readLong(): Long = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 8
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 8, readPos)).order(ByteOrder.BIG_ENDIAN).getLong
  }

  def readFloat(): Float = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 4
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 4, readPos)).order(ByteOrder.BIG_ENDIAN).getFloat
  }

  def readDouble(): Double = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 8
    currentDatatypeCount += 1
    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 8, readPos)).order(ByteOrder.BIG_ENDIAN).getDouble
  }

  def readDouble(num: Int): Array[Double] = {
    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 8*num
    currentDatatypeCount += num
    val vec = new Array[Double](num)

    ByteBuffer.wrap(messageBuffer.array.slice(readPos - 8*num, readPos))
        .order(ByteOrder.BIG_ENDIAN)
        .asDoubleBuffer()
        .get(vec)

    vec
  }

  def readString(): String = {

    if (readPos == headerLength | currentDatatypeCount == currentDatatypeCountMax) {
      readNextDatatype
    }

    readPos += 2*currentDatatypeCountMax
    currentDatatypeCount = currentDatatypeCountMax
    new String(ByteBuffer.wrap(messageBuffer.array.slice(readPos - 2*currentDatatypeCountMax, readPos)).order(ByteOrder.BIG_ENDIAN).array(), "utf-16")
  }

  // Writing data
  def start(client_id: Short, session_id: Short, s: String): this.type = {

    reset
    tempBuffer.clear
    tempBuffer.putShort(client_id)

    var bbArray = tempBuffer.array
    for (i <- 0 until 2) {
      messageBuffer.put(i, bbArray(i))
    }

    tempBuffer.clear
    tempBuffer.putShort(client_id)

    bbArray = tempBuffer.array
    for (i <- 0 until 2) {
      messageBuffer.put(i + 2, bbArray(i))
    }

    messageBuffer.put(4, Commands.getCode(s))

    this
  }

  def addHeader(header: Array[Byte]): this.type = {

    for (i <- 0 until 9)
      messageBuffer.put(i, header(i))
    readHeader()

    this
  }

  def addPacket(packet: Array[Byte], length: Int): this.type = {

    for (i <- 0 until length)
      messageBuffer.put(writePos + i, packet(i))

    writePos += length

    this
  }

  def putByte(value: Byte, pos: Int): this.type = {

    messageBuffer.put(pos, value)

    this
  }

  def writeByte(value: Byte): this.type = {

    checkDatatype("BYTE").putByte(value, writePos)
    writePos += 1

    this
  }

  def putChar(value: Char, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putChar(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 2)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeChar(value: Char): this.type = {

    checkDatatype("CHAR").putChar(value, writePos)
    writePos += 2

    this
  }

  def putShort(value: Short, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putShort(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 2)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeShort(value: Short): this.type = {

    checkDatatype("SHORT").putShort(value, writePos)
    writePos += 2

    this
  }

  def putInt(value: Int, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putInt(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 4)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeInt(value: Int): this.type = {

    checkDatatype("INT").putInt(value, writePos)
    writePos += 4

    this
  }

  def putLong(value: Long, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putLong(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 8)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeLong(value: Long): this.type = {

    checkDatatype("LONG").putLong(value, writePos)
    writePos += 8

    this
  }

  def putFloat(value: Float, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putFloat(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 4)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeFloat(value: Float): this.type = {

    checkDatatype("FLOAT").putFloat(value, writePos)
    writePos += 4

    this
  }

  def putDouble(value: Double, pos: Int): this.type = {

    tempBuffer.clear
    tempBuffer.putDouble(value)

    val bbArray = tempBuffer.array
    for (i <- 0 until 8)
      messageBuffer.put(pos + i, bbArray(i))

    this
  }

  def writeDouble(value: Double): this.type = {

    checkDatatype("DOUBLE").putDouble(value, writePos)
    writePos += 8

    this
  }

  def putString(value: String, pos: Int): this.type = {

    val stringBuffer = value.getBytes("utf-16")

    for (i <- 2 until stringBuffer.size) {
      messageBuffer.put(pos + i - 2, stringBuffer(i))
    }

    this
  }

  def writeString(value: String): this.type = {

    checkDatatype("STRING").putString(value, writePos)
    currentDatatypeCount = value.length
    writePos += 2*currentDatatypeCount
    currentDatatype = 0

    this
  }

  // ========================================================================================

  def checkDatatype(t: String): this.type = {

    if (currentDatatype != Datatypes.getCode(t)) {
      currentDatatype = Datatypes.getCode(t)

      putInt(currentDatatypeCount, currentDatatypeCountPos)
      putByte(currentDatatype, writePos)

      currentDatatypeCount = 1
      currentDatatypeCountPos = writePos + 1
      writePos += 5
    }
    else {
      currentDatatypeCount += 1
    }

    this
  }

  def updateBodyLength: this.type = {

    bodyLength = writePos - headerLength

    tempBuffer.clear
    tempBuffer.putInt(bodyLength)

    val bbArray = tempBuffer.array
    for (i <- 0 until 4) {
      messageBuffer.put(i + 5, bbArray(i))
    }

    this
  }

  def updateDatatypeCount: this.type = {

    putInt(currentDatatypeCount, currentDatatypeCountPos)

    this
  }

  def finish(): Array[Byte] = {
    updateBodyLength
    updateDatatypeCount

    messageBuffer.array.slice(0, headerLength + bodyLength)
  }

//  def flush: this.type = {
//    updateBodyLength.updateDatatype
//
//    if (messageBuffer.hasArray) {
//      val array = messageBuffer.array.slice(0, headerLength + bodyLength)
//      Collections.reverse(Arrays.asList(array))
//      output.write(array)
//    }
//    else {
//      System.out.println("Ooops")
//    }
//
//    output.flush
//
//    this
//  }

  // ========================================================================================

  def print: this.type = {

    val space: String = "                                              "
    var data: String = ""

    val tt = messageBuffer.array

    val tempCommandCode = tt(0)
    val tempBodyLength: Int = ByteBuffer.wrap(tt.slice(1, 5)).order(ByteOrder.BIG_ENDIAN).getInt

    System.out.println()
    System.out.println(s"$space ==============================================")
    System.out.println(s"$space Command code:         $tempCommandCode (${Commands.getName(tempCommandCode)})")
    System.out.println(s"$space Message body length:  $tempBodyLength")
    System.out.println(s"$space ----------------------------------------------")
    System.out.println(" ")

    var i: Int = headerLength

    while (i < tempBodyLength) {

      val dataArrayType: String = Datatypes.getName(tt(i))
      val dataArrayLength: Int = ByteBuffer.wrap(tt.slice(i + 1, i + 5)).order(ByteOrder.BIG_ENDIAN).getInt

      println(s"$space Datatype (length):    $dataArrayType ($dataArrayLength)")

      data = ""
      i += 5

      if (dataArrayType == "STRING") {
        val stringBuffer = tt.slice(i, i + 2*dataArrayLength)

        data = " " + new String(stringBuffer, "utf-16")
        i += 2*dataArrayLength
      }
      else {
        var dataTypeLength: Int = 0
        dataArrayType match {
          case "BYTE" => dataTypeLength = 1
          case "CHAR" => dataTypeLength = 2
          case "SHORT" => dataTypeLength = 2
          case "INT" => dataTypeLength = 4
          case "LONG" => dataTypeLength = 8
          case "FLOAT" => dataTypeLength = 4
          case "DOUBLE" => dataTypeLength = 8
        }

        var j = 0
        for (j <- 0 until dataArrayLength) {
          dataArrayType match {
            case "BYTE" => data = data.concat(s" ${tt(i)}")
            case "CHAR" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getChar}")
            case "SHORT" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getShort}")
            case "INT" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getInt}")
            case "LONG" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getLong}")
            case "FLOAT" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getFloat}")
            case "DOUBLE" => data = data.concat(s" ${ByteBuffer.wrap(tt.slice(i, i + dataTypeLength)).order(ByteOrder.BIG_ENDIAN).getDouble}")
          }

          i += dataTypeLength
        }
      }
      System.out.println(s"$space Data:                $data")
      System.out.println(" ")
    }

    System.out.println(s"$space ==============================================")

    this
  }
}