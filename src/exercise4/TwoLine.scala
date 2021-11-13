package exercise4

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 2.0
 *  @date    Sun Sep 26 15:00:24 EDT 2021
 *  @see     LICENSE (MIT style license file).
 *
 *  @title   Example Model: TwoLine for Process-Interaction Simulation
 */

//import scalation.random.{Exponential, Uniform}
//import scalation.random.RandomSeeds.N_STREAMS
import scalation.simulation.process._
import scalation.random._
import scalation.random.RandomSeeds.N_STREAMS

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
object RunTwoLine extends App:
  new TwoLineModel()
end RunTwoLine


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `TwoLineModel` class defines a simple process-interaction model of a TwoLine
 *  where service is provided by one or more Servers.
 *  Caveat: must add 'from' and 'to' components before transport!!
 *  @param name       the name of the simulation model
 *  @param reps       the number of independent replications to run
 *  @param animating  whether to animate the model
 *  @param aniRatio   the ratio of simulation speed vs. animation speed
 *  @param nStop      the number arrivals before stopping
 *  @param stream     the base random number stream (0 to 999)
 */
class TwoLineModel (name: String = "TwoLine", reps: Int = 1, animating: Boolean = true,
                 aniRatio: Double = 100.0, nStop: Int = 100000, stream: Int = 1)
  extends Model (name, reps, animating, aniRatio):

  //--------------------------------------------------
  // Initialize Model Constants

  val lambda   = 20.0                                // customer arrival rate (per hour)
  val mu       = 12.0                                // customer service rate (per hour)
  val nServers = 1                                  // the number of TwoLine Servers (servers)
  val HOUR = 60
  val MINUTE = 1

  //--------------------------------------------------
  // Create Random Variables (RVs)

  val iArrivalRV = Exponential (HOUR / lambda, stream)
  val serviceRV  = Exponential (HOUR / mu, (stream + 1) % N_STREAMS)
  val moveRV     = Uniform (0.04 * MINUTE, 0.06 * MINUTE, (stream + 2) % N_STREAMS)

  //--------------------------------------------------
  // Create Model Components

  val entry     = Source ("entry", this, () => Customer (), 0, nStop, iArrivalRV, (100, 290)) //entity
  val serverQ1  = WaitQueue ("serverQ1", (330, 290))                                          //queues
  val serverQ2  = WaitQueue("serverQ2", (330,330))
  val server1    = Resource ("server1", serverQ1, nServers, serviceRV, (350, 285))            //resources to bind
  val server2   = Resource ("server2", serverQ2, nServers, serviceRV, (350, 325))
  val door1     = Sink ("door1", (600, 290))
  val door2      = Sink ("door2", (600, 280)) //exit
  val toServerQ1 = Transport ("toserverQ1", entry, serverQ1, moveRV)                          //to server1
  val toServerQ2 = Transport ("toserverQ2", entry, serverQ2, moveRV)                          //to server2
  val toDoor1    = Transport ("toDoor1", server1, door1, moveRV)                         //from server1
  val toDoor2 = Transport ("toDoor2", server2, door2, moveRV)                            //from server2


  addComponent (entry, serverQ1, serverQ2, server1, server2, door1, door2, toServerQ1, toServerQ2, toDoor1, toDoor2)

  //--------------------------------------------------
  // Specify Scripts for each Type of Simulation Actor

  val coin = Bernoulli(0.5)

  case class Customer () extends SimActor ("c", this):
    def act (): Unit =
      // choose the least busy line; default to line1


      //if coin.gen == 1 then
      if serverQ1.length < serverQ2.length then
        toServerQ1.jump ()
        if server1.busy then serverQ1.waitIn () else serverQ1.noWait ()
        server1.utilize ()
        server1.release ()
        toDoor1.jump ()
        door1.leave ()
      else
        toServerQ2.jump ()
        if server2.busy then serverQ2.waitIn () else serverQ2.noWait ()
        server2.utilize ()
        server2.release ()
        toDoor2.jump ()
        door2.leave ()
      end if


      /*
      if server1.busy then                  //check if s1 is busy, if not then no wait, else queue in lesser queue
        if server2.busy then                //if server2 busy block
          if serverQ1.length <= serverQ2.length then  //to queue1
            toServerQ1.jump()
            serverQ1.waitIn()
          else
            toServerQ2.jump()
            serverQ2.waitIn()
          end if
        else
          toServerQ2.jump()
          serverQ2.noWait()
          server2.utilize()
          server2.release()
          toDoor2.jump()
        end if
      else
        toServerQ1.jump()
        serverQ1.noWait ()
        server1.utilize ()
        server1.release ()
        toDoor1.jump ()
      end if
      door.leave ()
      */

    end act

  end Customer

  simulate ()
  waitFinished ()
  Model.shutdown ()

end TwoLineModel


