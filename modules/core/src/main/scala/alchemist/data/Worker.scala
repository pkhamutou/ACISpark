package alchemist.data

final case class Worker(
  id: Short,
  hostname: String,
  address: String,
  port: Short,
  groupId: Short
)
