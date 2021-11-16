cs4210-project3

Group Members: Kartchner.Micah (programming and debugging), Key.Terry (programming and debugging), Loparev.Nikolai(programming, debugging and logic documentation) CS4210 Modelling & Simulation Professor: John A. Miller 20.Sep.2021 Project 2: Event Scheduling Simulation Examples

Overview: Project utilizes Scala and ScalaTION to simulate real-life service and industrial process. Built in IntelliJ IDEA.

Logic:

Explanations covered in comments in methods.

17.3.20 Exercises

4. Implement a Process-Interaction Simulation and analyze the waiting times and standard deviation of
the waiting times for Wendy’s and MacDonald’s. Let λ = 20 hr^−1 and μ = 12 hr^−1.

Wendy's:

Wait times:

Standard deviation:

McDonald's:

Wait times:

Standard deviation:

6. Implement a Machine Shop simulation using Process-Interaction and determine which policy is better.

Blocking simulation is a bonus.

8. Create a Process-Interaction Simulation of US 101 (Bayshore Freeway) at the Stanford exits. Data
is recorded every five minutes at each of the sensors from Willow Road to Oregon Expressway giving
288 data points per day per sensor. Collect data for a portion the year 2021 for these sensors. Use
it to calibrate and validate your models. Place Sources at the beginning of all road segments and
on-ramps. Model the traffic inflow to the model using a Non-Homogeneous Poisson Process (NHPP)
for each source that fits the data at that location. Place Sinks at the end of all road segments
and off-ramps. Place Junctions at all road sensors. Use Routes for the road segments between
sensors. There are typically four lanes Northbound and four lanes Southbound. Finally, use the data
provided by Caltrans PeMS (traffic flow and speed) to to measure the accuracy (sMAPE) of your
simulation model. See https://getd.libs.uga.edu/pdfs/peng_hao_201908_phd.pdf and https:
//dot.ca.gov/programs/traffic-operations/mpr/pems-source.
