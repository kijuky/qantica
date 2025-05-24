package io.kijuky.qantica

// Eval 型関数で式を展開して、最終的な型として正規化された構造に帰着させる
// Mul => with で連結、Div => 分子 / 分母の構造に保持

type DimEq[E <: DimExp] <: DimExp = E match
  case DimExp.Base[d]   => DimExp.Base[d]
  case DimExp.Mul[a, b] => DimEq[a] & DimEq[b] // `&` = `with`
  // case Ynit.Div[a, Ynit.Div[b, c]] => Eval[Ynit.Div[Eval[Ynit.Mul[a, c]], Eval[b]]] // a / (b / c) = (a * c) / b
  // case Ynit.Div[Ynit.Div[a, b], c] => Eval[Ynit.Div[Eval[a], Eval[Ynit.Mul[b, c]]]] // (a / b) / c = a / (b * c)
  case DimExp.Div[a, b] => DimExp.Div[DimEq[a], DimEq[b]]
  case DimExp.Pow[e, n] => DimExp.Pow[DimEq[e], n]
