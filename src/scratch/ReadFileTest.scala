package scratch

import scalation._
import scalation.mathstat._
import scalation.modeling._
import scalation.simulation._

object test extends App:
  //val matrix = MatrixD.load("test.csv",0,',')
  //print(matrix)

  val fileName = "test.csv"
  val data = MatrixD.load (fileName)
  val ord  = 19

  //val (t, y) = (data(?, 0) * 60.0, data(?, 1))                 // (time, vehicle count)
  val (t, y) = (data(?, 0), data(?, 1))
  new Plot (t, y, null, "traffic data")
  val mod = PolyRegression (t, y, ord, null, Regression.hp)
  mod.train ()
  val (yp, qof) = mod.test ()
  println (mod.report (qof))
  new Plot (t, y, yp, "traffic: actual vs. predicted")

  def lambdaf (tt: Double): Double = mod.predict (tt)

  val pp = new NH_PoissonProcess (t.dim-1, lambdaf)
  println("test of gen:")
  print((pp.gen)(0))
  val flw  = pp.flow (1.0).toDouble
  new Plot (t, y, flw, "NH_PoissonProcess cars per 1 min.")

  val ft = new TestFit (y.dim)
  ft.diagnose (y, flw)
  println (Fit.fitMap (ft.fit))


end test

