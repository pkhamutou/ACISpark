package alchemist.net.message

import scodec.Encoder
import scodec.codecs._

trait FrontendMessage[A] {
  def encoder: Encoder[A]
}

object FrontendMessage {

  private def lengthPrefixed[A](encoder: Encoder[A]): Encoder[A] =
    Encoder[A] { a: A =>
      for {
        payload <- encoder.encode(a)
        length  <- int32.encode((payload.size / 8).toInt)
      } yield length ++ payload
    }

  def prefixed[A](header: Header)(implicit enc: Encoder[A]): FrontendMessage[A] = new FrontendMessage[A] {
    override def encoder: Encoder[A] = Encoder[A] { a: A =>
      for {
        header  <- Header.codec.encode(header)
        payload <- lengthPrefixed(enc).encode(a)
      } yield header ++ payload
    }
  }
}
