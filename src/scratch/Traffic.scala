
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
    val embarRampSource = Source("embarcadero_entrance", this, () => Car(), 1, nStop, iArrivalRV, (900,600))
    val northSink = Sink("north_sink", (150,10))
    val embarEntJunc = Junction("s_ent_j", this, Sharp(0.0), (800,600))
    val northRoad = Route("n_road", 4, southSource, embarEntJunc, moveRV)
    val embarEntRamp = Transport("nr_road", embarRampSource, embarEntJunc, moveRV)

    val northSource = Source("north_source", this, () => Car(), 2, nStop, iArrivalRV, (10,10))
    val southSink = Sink("south_sink", (750,700))
    //embarcadero rd exit
    val embarRampSink = Sink("embarcadero_exit", (660,700))
    val embarExJunc = Junction("n_ex_j", this, Sharp(0.0), (700,650))
    val embarExRamp = Transport("embar_exit", embarExJunc, embarRampSink, moveRV)
    val toSSink = Route("t_ssink", 4, embarExJunc, southSink, moveRV)

    val willowRampSink = Sink("willowrd_sink", (250,10))
    val willowExJunc = Junction("w_ex_j", this, Sharp(0.0), (250,100))
    val willowExRamp = Transport("willowrd_exit", willowExJunc, willowRampSink, moveRV)
    val willowToSink = Route("w_t_s", 4, willowExJunc, northSink, moveRV)

    val willowRampSource = Source("willowrd_src", this, () => Car(), 3, nStop, iArrivalRV, (10,100))
    val willowEntJunc = Junction("w_ent_j", this, Sharp(0.0), (150,150))
    val southRoad = Route("s_road", 4, northSource, willowEntJunc, moveRV)
    val willowEntRamp = Transport("willowrd_ent", willowRampSource, willowEntJunc, moveRV)

    val uniAveNorthRampSink = Sink("uni_n_sink", (550,310))
    val uniAveNorthExJunc = Junction("uni_n_ex_j", this, Sharp(0.0), (580,400))
    val uniAveNorthExRamp = Transport("uni_n_exit", uniAveNorthExJunc, uniAveNorthRampSink, moveRV)
    val northToUniEx = Route("n_t_uni", 4, embarEntJunc, uniAveNorthExJunc, moveRV)
    val uniAveNorthRampSource = Source("uni_n_src", this, () => Car(), 4, nStop, iArrivalRV, (525,290))
    val uniAveNorthEntJunc = Junction("uni_n_ent_j", this, Sharp(0.0), (450, 270))
    val northToUniEnt = Route("uni_ex_t_uni_ent", 4, uniAveNorthExJunc, uniAveNorthEntJunc, moveRV)
    val northToWillow = Route("n_t_w", 4, uniAveNorthEntJunc, willowExJunc, moveRV)
    val uniAveNorthEntRamp = Transport("uni_n_ent", uniAveNorthRampSource, uniAveNorthEntJunc, moveRV)

    val uniAveSouthRampSink = Sink("uni_s_sink", (400,425))
    val uniAveSouthExJunc = Junction("uni_s_ex_j", this, Sharp(0.0), (375,350))
    val uniAveSouthExRamp = Transport("uni_s_exit", uniAveSouthExJunc, uniAveSouthRampSink, moveRV)
    val southToUniEx = Route("s_t_uni_ex", 4, willowEntJunc, uniAveSouthExJunc, moveRV)
    val uniAveSouthEntJunc = Junction("uni_s_ent_j", this, Sharp(0.0), (500, 465))
    val southtoUniEnt = Route("s_t_uni_ent", 4, uniAveSouthExJunc, uniAveSouthEntJunc, moveRV)
    val southToEmbar = Route("s_t_e", 4, uniAveSouthEntJunc, embarExJunc, moveRV)
    val uniAveSouthEntSource = Source("uni_s_src", this, () => Car(), 5, nStop, iArrivalRV, (430,450))
    val uniAveSouthEntRamp = Transport("uni_s_ent", uniAveSouthEntSource, uniAveSouthEntJunc, moveRV)


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

    addComponent(northSink, embarRampSource, southSource, embarEntJunc, northSource, southSink,
      embarRampSink, willowRampSource, willowRampSink, embarExJunc, willowExJunc, willowEntJunc,
      uniAveNorthRampSource, uniAveNorthEntJunc, uniAveSouthRampSink, uniAveSouthExJunc,
      uniAveSouthEntJunc, uniAveSouthEntSource,
      uniAveSouthExRamp, southtoUniEnt, uniAveSouthEntRamp,
      uniAveNorthRampSink, uniAveNorthExJunc, uniAveNorthExRamp, northToUniEx,southToUniEx,
      southRoad, toSSink, willowToSink, willowEntRamp, southToEmbar, northToUniEnt,
      embarExRamp, northRoad, embarEntRamp, uniAveNorthEntRamp, northToWillow, willowExRamp)
    //, southRampRoad, nExJunction, toSSink
    //addComponents (source, queue, light, sink, road.toList)

    //--------------------------------------------------
    // Specify Scripts for each Type of Simulation Actor
    val coin = Bernoulli(0.50)
    case class Car () extends SimActor ("c", this):

        def act (): Unit =
          if subtype == 0 then //from south source
            northRoad.lane(0).move()
            embarEntJunc.jump()
            northToWillow.lane(0).move()

            northSink.leave()
          end if
          if subtype == 1 then //from embarcadero entrance ramp
            embarEntRamp.move()
            embarEntJunc.jump()
            northToWillow.lane(3).move()
            northSink.leave()
          end if
          if subtype == 2 then //from north source
            southRoad.lane(3).move()
            embarExJunc.jump()
            if coin.igen == 1 then
              toSSink.lane(3).move()
              southSink.leave()
            else
              embarExRamp.move()
              embarRampSink.leave()
            end if
          end if
          if subtype == 3 then //from willow entrance ramp
            willowEntRamp.move()
            willowEntJunc.jump()
            southToEmbar.lane(3).move()
            toSSink.lane(3).move()
            southSink.leave()
            print("")
          end if
          if subtype == 4 then //from uni north entrance ramp
            uniAveNorthEntRamp.move()
            uniAveNorthEntJunc.jump()
            northToWillow.lane(3).move()
            willowToSink.lane(3).move()
            northSink.leave()
          end if
          if subtype == 5 then //from uni south entrance ramp
              southSink.leave()
          end if
            /*val i = subtype                         // from North (0), East (1), South (2), West (3)
            val l = laneRV.igen                     // select lane l
            road(i).lane(l).move ()
            if light(i).shut then queue(i).waitIn ()
            road(i + 4).lane(l).move ()
            sink((i + 2) % 4).leave ()*/
        end act

    end Car

    simulate ()
    waitFinished ()
    Model.shutdown ()

end TrafficModel

