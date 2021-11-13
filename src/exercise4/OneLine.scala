package exercise4

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 2.0
 *  @date    Sun Sep 26 15:00:24 EDT 2021
 *  @see     LICENSE (MIT style license file).
 *
 *  @title   Example Model: OneLine for Process-Interaction Simulation
 */

//import scalation.random.{Exponential, Uniform}
//import scalation.random.RandomSeeds.N_STREAMS
import scalation.simulation.process._
import scalation.random._
import scalation.random.RandomSeeds.N_STREAMS

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
object RunOneLine extends App:
  new OneLineModel()
end RunOneLine


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `OneLineModel` class defines a simple process-interaction model of a OneLine
 *  where service is provided by one or more Servers.
 *  Caveat: must add 'from' and 'to' components before transport!!
 *  @param name       the name of the simulation model
 *  @param reps       the number of independent replications to run
 *  @param animating  whether to animate the model
 *  @param aniRatio   the ratio of simulation speed vs. animation speed
 *  @param nStop      the number arrivals before stopping
 *  @param stream     the base random number stream (0 to 999)
 */
class OneLineModel (name: String = "OneLine", reps: Int = 1, animating: Boolean = true,
                 aniRatio: Double = 100.0, nStop: Int = 100000, stream: Int = 3)
  extends Model (name, reps, animating, aniRatio):

  //--------------------------------------------------
  // Initialize Model Constants

  val lambda   = 20.0                                // customer arrival rate (per hour)
  val mu       = 12.0                                // customer service rate (per hour)
  val nServers = 2                                  // the number of OneLine Servers (servers)
  val HOUR = 60
  val MINUTE = 1

  //--------------------------------------------------
  // Create Random Variables (RVs)

  val iArrivalRV = Exponential (HOUR / lambda, stream)
  val serviceRV  = Exponential (HOUR / mu, (stream + 1) % N_STREAMS)
  val moveRV     = Uniform (0.04 * MINUTE, 0.06 * MINUTE, (stream + 2) % N_STREAMS)

  //--------------------------------------------------
  // Create Model Components

  val entry     = Source ("entry", this, () => Customer (), 0, nStop, iArrivalRV, (100, 290))
  val serverQ   = WaitQueue ("serverQ", (330, 290))
  val server    = Resource ("server", serverQ, nServers, serviceRV, (350, 285))
  val door      = Sink ("door", (600, 290))
  val toServerQ = Transport ("toserverQ", entry, serverQ, moveRV)
  val toDoor    = Transport ("toDoor", server, door, moveRV)

  addComponent (entry, serverQ, server, door, toServerQ, toDoor)

  //--------------------------------------------------
  // Specify Scripts for each Type of Simulation Actor

  case class Customer () extends SimActor ("c", this):

    def act (): Unit =
      toServerQ.jump ()
      if server.busy then serverQ.waitIn () else serverQ.noWait ()
      server.utilize ()
      server.release ()
      toDoor.jump ()
      door.leave ()
    end act

  end Customer

  simulate ()
  waitFinished ()
  Model.shutdown ()

end OneLineModel


