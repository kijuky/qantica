package io.kijuky.qantica

trait ShowStructured[E <: DimExp] {
  def show: String
}
object ShowStructured {

  import scala.compiletime.{erasedValue, constValue}

  inline given showStructured[E <: DimExp]: ShowStructured[E] =
    new ShowStructured[E] {
      def show: String = structured[E]
    }

  inline def structured[E <: DimExp]: String = inline erasedValue[E] match
    case _: DimExp.Base[d] =>
      showDim[d]
    case _: DimExp.Mul[a, b] =>
      s"(${structured[a]} * ${structured[b]})"
    case _: DimExp.Div[a, b] =>
      s"(${structured[a]} / ${structured[b]})"
    case _: DimExp.Pow[a, n] =>
      s"${structured[a]}^${constValue[n]}"

  inline def showDim[D <: Dim]: String = inline erasedValue[D] match
    case _: Dim.Mass        => "M"
    case _: Dim.Length      => "L"
    case _: Dim.Time        => "T"
    case _: Dim.Temperature => "Θ"
}
