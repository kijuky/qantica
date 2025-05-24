# qantica

**qantica** is a type-safe dimensional analysis library for Scala, focused on representing physical quantities, dimensions, and units at the type level.

What makes this library unique is that it **preserves the structure of the calculation as a type**. This enables:

- Differentiating between the same dimension with different computational history
- Type-safe comparison of even dimensionless quantities
- Structured display of units and derived dimensions
- Explicit type casting from a computed form to a simplified interpretation

---

## Features

### ✅ Type-Level Dimension Representation

Dimensions are expressed as types like `Length`, `Mass`, `Time`, and composed into dimensional expressions (`DimExp`).

```scala
type Velocity = DimExp.Div[DimExp.Base[Length], DimExp.Base[Time]]
type Area     = DimExp.Pow[DimExp.Base[Length], 2.0]
```

### ✅ Quantities Preserve Computational History

A `Quantity[DimExp]` not only stores the value but also the entire computation trail as a type.

```scala
val rho = Quantity[Density](1000.0)
val v   = Quantity[Velocity](2.0)
val mu  = Quantity[Viscosity](1.0)
val L   = Quantity[Length](0.5)

val re1 = rho * L * v / mu // structure is preserved in the type
```

### ✅ Comparison Reflects Calculation Order

The following are considered equal, because they share the same computation structure:

```scala
val re1 = rho * L * v / mu
val re2 = rho * v * L / mu

re1 === re2 // OK
```

But these are **not** equal due to different computation trees:

```scala
val re3 = v * L / (mu / rho)

re1 === re3 // Compile-time error
```

### ✅ Explicit Casting to an Interpreted Form

You can explicitly convert a computed quantity into a known interpretation, flattening its structure:

```scala
val nu = (mu / rho).asInterpreted[KinViscosity]
```

This makes the type easier to read and use, but eliminates the original computation history.

---

## Output Examples

```scala
println(re1.showStructured)
// => 1000.0 [((((M / L^3.0) * L) * (L / T)) / (M / (L * T)))]

println(re1.showSimplified)
// => 1000.0 [(M L^-1.0 T^-1.0) / (M L^-1.0 T^-1.0)]
```

---

## Design Insight

This library was born from a key insight:
1. An expression like `a * b / c` represents a computational path.
2. If we preserve that path in the type, we can differentiate structures—even if they simplify to the same dimension.
3. This enables **type-safe equality checks** even for dimensionless values.
4. For practical usage, we also allow an explicit `asInterpreted[T]` to switch from computed to recognized physical dimensions.

## Future Plans

- Introduce units (e.g., m, s, kg)
- Support for unit conversion (SI ↔ CGS)
- Type-level scaling (e.g., mm → m)
- Explicit distinction between quantities and differences (e.g., `Temperature` vs `TemperatureDiff`)
- DSL for intuitive quantity construction (`val v = 2.m / 1.s`)

---

## Related Projects

- [Coulomb](https://github.com/erikerlandson/coulomb): Type-level dimensional analysis with support for refined.
- [Squants](https://github.com/garyKeorkunian/squants): Rich DSL and unit support, great for practical modeling.
- [spire](https://github.com/typelevel/spire): Generalized algebraic abstractions and number types.

---

## Name

**qantica** is a portmanteau of _quantity_ and _algebraic_.

---

## License

MIT
