import scala.language.implicitConversions

opaque type Position[Buffer] = Int

trait TokenParser[Token, R]

object TextParser {
  given TP as TokenParser[Char, Position[CharSequence]] {}

  given FromCharToken with (T: TokenParser[Char, Position[CharSequence]])
    : Conversion[Char, Position[CharSequence]] = ???
}


object Testcase {
  def main(args: Array[String]): Unit = {
    import TextParser._

    val tp_v: TokenParser[Char, Position[CharSequence]] = TextParser.TP
    val tp_i = summon[TokenParser[Char, Position[CharSequence]]] // error
    val co_i = summon[Conversion[Char, Position[CharSequence]]]  // error
    val co_x : Position[CharSequence] = 'x'                   // error

    {
      given XXX as Conversion[Char, Position[CharSequence]] = co_i
      val co_y : Position[CharSequence] = 'x'
    }
  }
}