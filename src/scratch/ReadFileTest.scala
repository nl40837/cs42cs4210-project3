package scratch

import scalation._
import scalation.mathstat.MatrixD

object test extends App:
  val matrix = MatrixD.load("test.csv",0,',')
  print(matrix)
end test

