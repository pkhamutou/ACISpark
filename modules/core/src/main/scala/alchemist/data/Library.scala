package alchemist.data

final case class Library(id: Library.LibraryId, name: String)

object Library {
  final case class LibraryId(value: Byte)
}
