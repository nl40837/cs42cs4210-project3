
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 2.0
 *  @date    Sun Sep 26 15:00:24 EDT 2021
 *  @see     LICENSE (MIT style license file).
 *
 *  @title   Example Model: Traffic for Process-Interaction Simulation
 */

package scratch

// One-Shot

import scala.collection.mutable.ListBuffer
import scalation.simulation.process._
import scalation.random.{Bernoulli, Sharp, Uniform}
import scalation.random.RandomSeeds.N_STREAMS

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `runTraffic` function is used to launch the `TrafficModel` class.
 *  > runMain scalation.simulation.process.example_1.runTraffic
 */
// @main def runTraffic (): Unit = new TrafficModel ()
object RunTraffic extends App:
  new TrafficModel()
end RunTraffic


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `TrafficModel` class simulates an intersection with four traffic lights
 *  `Gates` and four roads.  Each road consists of two routes with one in each
 *  direction.  Each `Route` has two lanes (`Transport`s).
 *  @param name       the name of the simulation model
 *  @param reps       the number of independent replications to run
 *  @param animating  whether to animate the model
 *  @param aniRatio   the ratio of simulation speed vs. animation speed
 *  @param nStop      the number arrivals before stopping
 *  @param stream     the base random number stream (0 to 999)
 */
class TrafficModel (name: String = "Traffic", reps: Int = 1, animating: Boolean = true,
                 aniRatio: Double = 4.0, nStop: Int = 10, stream: Int = 0)
      extends Model (name, reps, animating, aniRatio):

    //--------------------------------------------------
    // Initialize Model Constants

    val iaTime  = (4000.0, 6000.0)                    // (lower, upper) on inter-arrival time
    val onTime  = 8000.0                              // on (green-light) time for North-South traffic
    val offTime = 6000.0                              // off (red-light) time for North-South traffic
    val mvTime  = (2900.0, 3100.0)                    // (lower, upper) on move time

    //--------------------------------------------------
    // Create Random Variables (RVs)

    val iArrivalRV = Uniform (iaTime, stream)
    val onTimeRV   = Sharp (onTime, (stream + 1) % N_STREAMS)
    val offTimeRV  = Sharp (offTime, (stream + 2) % N_STREAMS)
    val moveRV     = Uniform (mvTime, (stream + 3) % N_STREAMS)
    val laneRV     = Bernoulli ((stream + 4) % N_STREAMS)

    //--------------------------------------------------
    // Create Model Components
    val southSource = Source("south_source", this, () => Car(), 0, nStop, iArrivalRV, (850,650))
    //embarcadero rd entrance
    val southRampSource = Source("emercadero_entrance", this, () => Car(), 1, nStop, iArrivalRV, (900,600))
    val northSink = Sink("north_sink", (150,10))
    val sEntJunction = Junction("s_ent_j", this, Sharp(0.0), (800,600))
    val northRoad = Route("n_road", 4, southSource, sEntJunction, moveRV)
    val northRampRoad = Transport("nr_road", southRampSource, sEntJunction, moveRV)
    val toNSink = Route("t_nsink", 4, sEntJunction, northSink, moveRV)


    val northSource = Source("north_source", this, () => Car(), 2, nStop, iArrivalRV, (10,10))
    val southSink = Sink("south_sink", (750,700))
    //emarcadero rd exit
    val southRampSink = Sink("emercadero_exit", (660,700))
    val nExJunction = Junction("n_ex_j", this, Sharp(0.0), (700,650))
    val southRampRoad = Transport("sr_road", nExJunction, southRampSink, moveRV)
    val southRoad = Route("s_road", 4, northSource, nExJunction, moveRV)
    val toSSink = Route("t_ssink", 4, nExJunction, southSink, moveRV)

    /*val source = Source.group (this, () => Car (), nStop, (800, 250),
                               ("s1N", 0, iArrivalRV, (0, 0)),
                               ("s1E", 1, iArrivalRV, (230, 200)),
                               ("s1S", 2, iArrivalRV, (30, 400)),
                               ("s1W", 3, iArrivalRV, (-200, 230)))

    val queue = WaitQueue.group ((800, 430), ("q1N", (0, 0)),
                                             ("q1E", (50, 20)),
                                             ("q1S", (30, 70)),
                                             ("q1W", (-20, 50)))

    val light = Gate.group (this, nStop, onTimeRV, offTimeRV, (800, 480),
                            ("l1N", queue(0), (0, 0)),                    // traffic from North
                            ("l1E", queue(1), (0, -30)),
                            ("l1S", queue(2), (30, -30)),
                            ("l1W", queue(3), (30, 0)))

    val sink = Sink.group ((830, 250), ("k1N", (0, 0)),
                                       ("k1E", (200, 230)),
                                       ("k1S", (-30, 400)),
                                       ("k1W", (-230, 200)))

    val road = new ListBuffer [Route] ()
    for i <- source.indices do
        road += Route ("ra" + i, 2, source(i), queue(i), moveRV)
        road += Route ("rb" + i, 2, light(i),  sink((i + 2) % 4), moveRV)
    end for*/

    addComponent(northSink, southRampSource, southSource, sEntJunction, northRoad, northRampRoad, toNSink,
      northSource, southSink, southRampSink, nExJunction, southRoad, toSSink, southRampRoad)
    //, southRampRoad, nExJunction, toSSink
    //addComponents (source, queue, light, sink, road.toList)

    //--------------------------------------------------
    // Specify Scripts for each Type of Simulation Actor
    val coin = Bernoulli(0.50)
    case class Car () extends SimActor ("c", this):

        def act (): Unit =
          if subtype == 0 then //from south source
            northRoad.lane(0).move()
            sEntJunction.jump()
            toNSink.lane(0).move()
            northSink.leave()
          end if
          if subtype == 1 then //from south ramp source
            northRampRoad.move()
            sEntJunction.jump()
            toNSink.lane(3).move()
            northSink.leave()
          end if
          if subtype == 2 then
            southRoad.lane(3).move()
            nExJunction.jump()
            if coin.igen == 1 then
              toSSink.lane(3).move()
              southSink.leave()
            else
              southRampRoad.move()
              southRampSink.leave()
            end if
          end if
            /*val i = subtype                         // from North (0), East (1), South (2), West (3)
            val l = laneRV.igen                     // select lane l
            road(i).lane(l).move ()
            if light(i).shut then queue(i).waitIn ()
            road(i + 4).lane(l).move ()
            sink((i + 2) % 4).leave ()*/
            print("")
        end act

    end Car

    simulate ()
    waitFinished ()
    Model.shutdown ()

end TrafficModel

