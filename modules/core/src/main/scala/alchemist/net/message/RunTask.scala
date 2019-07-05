package alchemist.net.message

import scodec.Codec

import alchemist.data.Library
import alchemist.library.Param
import alchemist.net.codecs.alchemistLibraryIdCodec
import alchemist.net.codecs.alchemistStringCodec

case class RunTask(
  libraryId: Library.LibraryId,
  methodName: String,
  args: List[Param]
)

object RunTask {
  import scodec.codecs.list
  implicit val encoder: Codec[RunTask] =
    (alchemistLibraryIdCodec :: alchemistStringCodec :: list(Param.codec)).as[RunTask]
}
