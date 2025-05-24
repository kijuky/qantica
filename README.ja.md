# qantica

**qantica** は、物理量・次元・単位の型安全な表現と演算を目的とした Scala ライブラリです。

このライブラリのユニークな点は「計算過程そのものを型として保持する」点にあります。これにより、次のようなことが可能になります：

- 同じ次元（単位）でも異なる計算過程を区別できる
- 無次元量の型安全な比較ができる
- 計算過程に基づいた次元の構造化表示ができる
- 簡約化（normalization）を経て「どのように解釈するか（=型キャスト）」を明示的に扱える

---

## 特徴

### ✅ 型による次元表現

次元は `Dim` として型で表現され、`Length`, `Mass`, `Time` などの基本単位が `DimExp`（後述）で構成されます。

```scala
type Velocity = DimExp.Div[DimExp.Base[Length], DimExp.Base[Time]]
type Area     = DimExp.Pow[DimExp.Base[Length], 2.0]
```

### ✅ 計算過程を保持した `Quantity` 型

物理量は `Quantity[DimExp]` として保持され、単なる数値に加えて「どう計算されたか」も型として残ります。

```scala
val rho = Quantity[Density](1000.0)
val v   = Quantity[Velocity](2.0)
val mu  = Quantity[Viscosity](1.0)
val L   = Quantity[Length](0.5)

// 計算過程を型に残したまま演算できる
val re1 = rho * L * v / mu
```

### ✅ 計算順序による型の差異も区別

以下の2つはどちらも「[レイノルズ数](https://ja.wikipedia.org/wiki/%E3%83%AC%E3%82%A4%E3%83%8E%E3%83%AB%E3%82%BA%E6%95%B0)」として値は同じですが、計算過程が異なるため異なる型になります：

```scala
val re1 = rho * L * v / mu
val re2 = rho * v * L / mu

re1 === re2 // OK: 同じ式構造
```

一方、以下は構造が異なるため比較できません：

```scala
val re3 = v * L / (mu / rho)

re1 === re3 // NG: 計算構造が異なるためコンパイルエラー
```

### ✅ 明示的な「解釈」キャスト

計算過程を潰して「これは `KinViscosity` 型として解釈したい」と明示できる API を提供します：

```scala
val nu = (mu / rho).asInterpreted[KinViscosity]
```

このとき、`nu` の型は `Quantity[KinViscosity]` になり、計算過程は消えます。比較はできなくなりますが、読みやすい定義に置き換えることが可能になります。

---

## 表示例

```scala
println(re1.showStructured)
// => 1000.0 [((((M / L^3.0) * L) * (L / T)) / (M / (L * T)))]

println(re1.showSimplified)
// => 1000.0 [(M L^-1.0 T^-1.0) / (M L^-1.0 T^-1.0)]
```

---

## 発見と設計思想

このライブラリは次の観察から始まりました：

1. `a * b / c` という式は「計算過程」を表しているが、それを型に反映する手段がなかった。
2. これを型（構文木）で保持すれば、同じ次元でも構造の異なる式を区別できる。
3. その結果、**無次元量ですら比較に型安全性を持ち込める**。
4. 一方、意味のある簡約（たとえば動粘性係数など）を `asInterpreted[型]` として解釈できる仕組みを併設することで、読みやすさも両立できる。

## 将来的な方向性

- 単位（例えば m, s, kg）の導入
- 単位換算（SI ↔ CGSなど）
- 型レベルでのスケール変換（mm → m など）
- `Teperature` vs `TemperatureDiff` のような拡張量・間隔量の明示的区別
- DSLによる読みやすい定義（`val v = 2.m / 1.s`）

---

## 類似プロジェクト

- [Coulomb](https://github.com/erikerlandson/coulomb): 型レベル次元解析に優れたライブラリ。refined などとも親和性が高い。
- [Squants](https://github.com/garyKeorkunian/squants): DSLと単位表現が非常に豊か。日常的な計測値の扱いに向いている。
- [spire](https://github.com/typelevel/spire): 抽象代数の観点から汎用的な数値演算を提供。

---

## 名前の由来

qantica は「quantity（物理量）」と「algebraic（代数的）」を掛け合わせた造語です。

---

## ライセンス

[MIT](./LICENSE)
