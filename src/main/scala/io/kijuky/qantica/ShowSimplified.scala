package io.kijuky.qantica

import scala.compiletime.{erasedValue, constValue}

trait ShowSimplified[E <: DimExp] {
  def show: String
}
object ShowSimplified {

  inline given showSimplified[E <: DimExp]: ShowSimplified[E] =
    new ShowSimplified[E] {
      def show: String = simplify[E]
    }

  inline def simplify[E <: DimExp]: String = inline erasedValue[E] match
    case _: DimExp.Div[a, b] =>
      val num = flatten[a]
      val den = flatten[b]
      s"(${format(num)}) / (${format(den)})"
    case _: E =>
      val flat = flatten[E]
      format(flat)

  inline def flatten[E <: DimExp]: Map[String, Double] =
    inline erasedValue[E] match
      case _: DimExp.Base[d]   => Map(showDim[d] -> 1.0)
      case _: DimExp.Mul[a, b] => merge(flatten[a], flatten[b], _ + _)
      case _: DimExp.Div[a, b] => merge(flatten[a], flatten[b], _ - _)
      case _: DimExp.Pow[a, n] =>
        flatten[a].view.mapValues(_ * constValue[n]).toMap

  inline def merge(
    m1: Map[String, Double],
    m2: Map[String, Double],
    op: (Double, Double) => Double
  ): Map[String, Double] =
    (m1.keySet ++ m2.keySet)
      .map { k =>
        val v = op(m1.getOrElse(k, 0.0), m2.getOrElse(k, 0.0))
        k -> v
      }
      .filter(_._2 != 0)
      .toMap

  inline def format(m: Map[String, Double]): String =
    if m.isEmpty then ""
    else
      m.toList
        .map {
          case (k, 1.0) => k
          case (k, e)   => s"$k^$e"
        }
        .mkString(" ")

  inline def showDim[D <: Dim]: String = inline erasedValue[D] match
    case _: Dim.Mass        => "M"
    case _: Dim.Length      => "L"
    case _: Dim.Time        => "T"
    case _: Dim.Temperature => "Θ"
}

object ShowSimplified2 {
  import scala.compiletime.{erasedValue, constValue}

  inline def simplify[E <: DimExp]: String =
    val (num, den) = flattenMulDiv[E]
    s"(${format(num)}) / (${format(den)})"

  /** Recursively flattens nested Ynit.Mul/Ynit.Div into (numerator,
    * denominator) maps
    */
  inline def flattenMulDiv[E <: DimExp]
    : (Map[String, Double], Map[String, Double]) =
    inline erasedValue[E] match
      case _: DimExp.Mul[a, b] =>
        val (na, da) = flattenMulDiv[a]
        val (nb, db) = flattenMulDiv[b]
        (merge(na, nb, _ + _), merge(da, db, _ + _))
      case _: DimExp.Div[a, b] =>
        val (na, da) = flattenMulDiv[a]
        val (nb, db) = flattenMulDiv[b]
        (merge(na, db, _ + _), merge(da, nb, _ + _)) // a/b = (na/db) / (da/nb)
      case _: DimExp.Base[d] =>
        (Map(showDim[d] -> 1.0), Map.empty)
      case _: DimExp.Pow[a, n] =>
        val base = flattenMulDiv[a]
        val factor = constValue[n]
        (
          base._1.view.mapValues(_ * factor).toMap,
          base._2.view.mapValues(_ * factor).toMap
        )
      case _: E =>
        (Map.empty, Map.empty)

  def merge[A](
    m1: Map[A, Double],
    m2: Map[A, Double],
    f: (Double, Double) => Double
  ): Map[A, Double] =
    (m1.keySet ++ m2.keySet).map { k =>
      k -> f(m1.getOrElse(k, 0.0), m2.getOrElse(k, 0.0))
    }.toMap

  def format(m: Map[String, Double]): String =
    m.toList
      .filter(_._2 != 0.0)
      .map { case (dim, pow) =>
        if (pow == 1.0) dim else s"$dim^$pow"
      }
      .mkString(" ")

  inline def showDim[D <: Dim]: String = inline erasedValue[D] match
    case _: Dim.Mass        => "M"
    case _: Dim.Length      => "L"
    case _: Dim.Time        => "T"
    case _: Dim.Temperature => "Θ"
}

object ShowSimplified3 {
  import scala.compiletime.{erasedValue, constValue}

  inline def simplify[E <: DimExp]: String =
    val (num, den) = extractNumDen0[E]
    if den.isEmpty then format(num)
    else s"(${format(num)}) / (${format(den)})"

  // 分子分母を抽出する関数（表示用）
  inline def extractNumDen0[E <: DimExp]
    : (Map[String, Double], Map[String, Double]) =
    inline erasedValue[E] match
      case _: DimExp.Div[a, b] =>
        val (n1, d1) = extractNumDen[a]
        val d2 = flatten[b]
        (n1, merge(d1, d2, _ + _))
      case _: DimExp.Mul[a, b] =>
        val (n1, d1) = extractNumDen[a]
        val n2 = flatten[b]
        (merge(n1, n2, _ + _), d1) // a * b
      case _: DimExp.Pow[a, n] =>
        val (n0, d0) = extractNumDen[a]
        val f = constValue[n]
        (n0.view.mapValues(_ * f).toMap, d0.view.mapValues(_ * f).toMap)
      case _: DimExp.Base[d] =>
        (Map(showDim[d] -> 1.0), Map.empty)

  // 分子分母を抽出する関数（表示用）
  inline def extractNumDen[E <: DimExp]
    : (Map[String, Double], Map[String, Double]) =
    inline erasedValue[E] match
      case _: DimExp.Div[a, b] =>
        val (n1, d1) = extractNumDen[a]
        val d2 = flatten[b]
        (n1, merge(d1, d2, _ + _))
      case _: DimExp.Mul[a, b] =>
        val (n1, d1) = extractNumDen[a]
        val n2 = flatten[b]
        (merge(n1, n2, _ + _), d1) // a * b
      case _: DimExp.Pow[a, n] =>
        val (n0, d0) = extractNumDen[a]
        val f = constValue[n]
        (n0.view.mapValues(_ * f).toMap, d0.view.mapValues(_ * f).toMap)
      case _: DimExp.Base[d] =>
        (Map(showDim[d] -> 1.0), Map.empty)

  inline def flatten[E <: DimExp]: Map[String, Double] =
    inline erasedValue[E] match
      case _: DimExp.Base[d]   => Map(showDim[d] -> 1.0)
      case _: DimExp.Mul[a, b] => merge(flatten[a], flatten[b], _ + _)
      case _: DimExp.Div[a, b] => merge(flatten[a], flatten[b], _ - _)
      case _: DimExp.Pow[a, n] =>
        flatten[a].view.mapValues(_ * constValue[n]).toMap

  def merge[A](
    m1: Map[A, Double],
    m2: Map[A, Double],
    f: (Double, Double) => Double
  ): Map[A, Double] =
    (m1.keySet ++ m2.keySet).map { k =>
      k -> f(m1.getOrElse(k, 0.0), m2.getOrElse(k, 0.0))
    }.toMap

  def format(m: Map[String, Double]): String =
    m.toList
      .filter(_._2 != 0.0)
      .map { case (dim, pow) =>
        if (pow == 1.0) dim else s"$dim^$pow"
      }
      .mkString(" ")

  inline def showDim[D <: Dim]: String = inline erasedValue[D] match
    case _: Dim.Mass        => "M"
    case _: Dim.Length      => "L"
    case _: Dim.Time        => "T"
    case _: Dim.Temperature => "Θ"
}
