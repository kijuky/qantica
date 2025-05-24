package io.kijuky.qantica

// 次元式

sealed trait DimExp
object DimExp:
  case class Base[D <: Dim]() extends DimExp
  case class Mul[A <: DimExp, B <: DimExp]() extends DimExp
  case class Div[A <: DimExp, B <: DimExp]() extends DimExp
  case class Pow[A <: DimExp, N <: Double]()(using val n: ValueOf[N])
      extends DimExp
