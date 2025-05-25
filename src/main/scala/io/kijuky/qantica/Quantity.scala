package io.kijuky.qantica

case class Quantity[E <: DimExp](value: Double):
  def ===[F <: DimExp](other: Quantity[F])(using
    ev: DimEq[E] =:= DimEq[F]
  ): Boolean =
    value == other.value

  def showStructured(using s: ShowStructured[E]): String =
    s"$value [${s.show}]"

  def showSimplified(using s: ShowSimplified[E]): String =
    s"$value [${s.show}]"

  inline def showSimplified2: String =
    s"$value [${ShowSimplified2.simplify[E]}]"

  inline def showSimplified3: String =
    s"$value [${ShowSimplified3.simplify[E]}]"

  def *[V <: DimExp](that: Quantity[V]): Quantity[DimExp.Mul[E, V]] =
    Quantity(this.value * that.value)

  def /[V <: DimExp](that: Quantity[V]): Quantity[DimExp.Div[E, V]] =
    Quantity(this.value / that.value)

trait ConvertIfCompatible[A <: DimExp, B <: DimExp] {
  def convert(q: Quantity[A]): Quantity[B]
}

object ConvertIfCompatible {
  given [A <: DimExp, B <: DimExp](using
    ev: NormalizedAsIntersection[A] =:= NormalizedAsIntersection[B]
  ): ConvertIfCompatible[A, B] with {
    def convert(q: Quantity[A]): Quantity[B] = Quantity[B](q.value)
  }
}

extension [A <: DimExp](q: Quantity[A])
  def asInterpreted[B <: DimExp](using
    c: ConvertIfCompatible[A, B]
  ): Quantity[B] = c.convert(q)

import scala.compiletime.ops.double.*

// 次元と指数を表す型
case class DimPower[D <: Dim, N <: Double]()

// DimMapはDimPowerのTuple（必ず同じ次元はまとめられている前提）
type DimMap = Tuple

// 型レベルの指数符号反転
type NegateT[M <: DimMap] <: DimMap = M match
  case EmptyTuple             => EmptyTuple
  case DimPower[d, n] *: tail => DimPower[d, n * -1.0] *: NegateT[tail]

// 型レベルの指数スケーリング
type ScaleT[M <: DimMap, N <: Double] <: DimMap = M match
  case EmptyTuple             => EmptyTuple
  case DimPower[d, n] *: tail => DimPower[d, n * N] *: ScaleT[tail, N]

// DimPowerをDimMapにマージ。指数の足し算（存在しなければ追加）
type MergeOneT[D <: Dim, P <: DimPower[D, ?], M <: DimMap] <: DimMap =
  (P, M) match
    case (DimPower[d, n], EmptyTuple)             => Tuple1[DimPower[d, n]]
    case (DimPower[D, n], DimPower[D, m] *: tail) =>
      // 同じ次元なら指数を足す
      DimPower[D, n + m] *: tail
    case (DimPower[d, n], head *: tail) =>
      head *: MergeOneT[d, DimPower[d, n], tail]

// DimMap同士をマージ。MergeOneTを使う
type MergeT[A <: DimMap, B <: DimMap] <: DimMap = A match
  case EmptyTuple              => B
  case DimPower[d, n] *: atail => MergeOneT[d, DimPower[d, n], MergeT[atail, B]]

type NormalizedT[E <: DimExp] <: Tuple = E match
  case DimExp.Base[d]   => Tuple1[DimPower[d, 1.0]]
  case DimExp.Mul[a, b] => MergeT[NormalizedT[a], NormalizedT[b]]
  case DimExp.Div[a, b] => MergeT[NormalizedT[a], NegateT[NormalizedT[b]]]
  case DimExp.Pow[e, n] => ScaleT[NormalizedT[e], n]

type ToIntersection[T <: Tuple] <: Any = T match
  case EmptyTuple               => Any
  case DimPower[_, 0.0] *: tail => ToIntersection[tail] // ← 0.0 の要素を除外
  case h *: t                   => h & ToIntersection[t]

type NormalizedAsIntersection[E <: DimExp] = ToIntersection[NormalizedT[E]]
