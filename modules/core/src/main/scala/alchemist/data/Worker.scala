package alchemist.data

final case class Worker(
  id: Worker.WorkerId,
  hostname: String,
  address: String,
  port: Short,
  groupId: Short
)

object Worker {
  final case class WorkerId(value: Short)
}
