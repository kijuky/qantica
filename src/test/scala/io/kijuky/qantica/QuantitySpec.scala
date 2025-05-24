package io.kijuky.qantica

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*

class QuantitySpec extends AnyFunSpec {
  describe("Quantity") {
    // 基本次元
    type Length = DimExp.Base[Dim.Length]
    type Time = DimExp.Base[Dim.Time]
    type Mass = DimExp.Base[Dim.Mass]

    // 複合次元
    type Velocity = DimExp.Div[Length, Time]
    type Density = DimExp.Div[Mass, DimExp.Pow[Length, 3.0]]
    type Viscosity = DimExp.Div[Mass, DimExp.Mul[Length, Time]]
    type KinViscosity = DimExp.Div[DimExp.Pow[Length, 2.0], Time]

    // 元の物理量を用意
    val v: Quantity[Velocity] = Quantity(2.0)
    val L: Quantity[Length] = Quantity(0.5)
    val rho: Quantity[Density] = Quantity(1000.0)
    val mu: Quantity[Viscosity] = Quantity(1.0)
    val nu = mu / rho // 動粘性係数

    it("should preserve structure in type") {
      val re1 = rho * L * v / mu
      val re2 = rho * v * L / mu

      assert(re1 === re2)
    }

    it("should not allow comparing different computation histories") {
      val re1 = rho * L * v / mu
      val re2 = v * L / nu

      "assert(re1 === re2)" shouldNot compile
    }

    it("should allow .asInterpreted") {
      val nu = (mu / rho).asInterpreted[KinViscosity]

      summon[NormalizedAsIntersection[
        DimExp.Div[Viscosity, Density]
      ] =:= NormalizedAsIntersection[KinViscosity]]
    }

    it("should show structured/simplified strings") {
      val re = v * L / nu
      val structured = re.showStructured

      assert(
        structured === "1000.0 [(((L / T) * L) / ((M / (L * T)) / (M / L^3.0)))]"
      )
    }

    it("should show simplified strings") {
      val re = v * L / nu
      val simplified = re.showSimplified

      assert(simplified === "1000.0 [(L^2.0 T^-1.0) / (L^2.0 T^-1.0)]")
    }

    it("should not allow comparing different dimensions") {
      val re = rho * L * v / mu
      val gr
        : Quantity[DimExp.Div[DimExp.Pow[Length, 3.0], DimExp.Pow[Time, 2.0]]] =
        Quantity(1000.0)

      "assert(re1 === gr)" shouldNot compile
    }
  }
}
