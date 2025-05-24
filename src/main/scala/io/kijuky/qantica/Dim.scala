package io.kijuky.qantica

// 次元

sealed trait Dim
object Dim {
  sealed trait Mass extends Dim
  sealed trait Length extends Dim
  sealed trait Time extends Dim
  sealed trait Temperature extends Dim
}
