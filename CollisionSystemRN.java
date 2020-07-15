import edu.princeton.cs.algs4.MinPQ;

/**
 *  The {@code CollisionSystem} class represents a collection of particles
 *  moving in a box, according to the laws of elastic collision.
 *  This event-based simulation relies on a priority queue.
 *  No rendering is done inside this class.
 */
public class CollisionSystemRN {


    /*TODO B1. preprocessing*/
    private final boolean DUMPWALLS;
    private final int DIM;
    private double t = 0.0; // simulation clock time
    private MinPQ<Event> pq = new MinPQ<Event>(); // the priority queue
    ParticleN[] particles; // the array of particles

    /**
     * Initializes a system with the specified collection of particles.
     * The individual particles will be mutated during the simulation.
     *
     * @param particles the array of particles
     * @param N the number of spatial dimensions
     */
    public CollisionSystemRN (ParticleN[] particles, int N, boolean DUMPWALLS) {
        this.particles = particles.clone();   // defensive copy
        this.DIM = N;
        this.DUMPWALLS = DUMPWALLS;

        /*Initialize PQ with collision events.*/
        for (ParticleN part : particles) {
            assert part.DIM == N : "A particle has the wrong number of dimensions. All particles must be N-dimensional";
            predict(part);
        }
    }

    /** Updates the priority queue with all new events for particle a.*/
    private void predict (ParticleN a) {
        assert a != null : "Can't predict the behavior of a null particle, now, can we?";

        /* Particle-particle collisions.*/
        for (ParticleN part : particles) {
            double dt = a.timeToHit(part);
            if (dt < Double.POSITIVE_INFINITY) {
                pq.insert(new Event(t + dt, a, part, -1));
            }
        }

        /* Particle-wall collisions.
        * The particle might hit multiple walls at once (a corner) if it's fat,
        * so we should check all walls.*/
        double[] times = a.timeToHitWalls();
        for (int i = 0; i < DIM; i++) {
            double dt = times[i];
            if (dt < Double.POSITIVE_INFINITY) {
                pq.insert(new Event(t+dt, a, null, i));
            }
        }
    }

    /**
     * Advances the simulation of the system of particles in {@code dt} units of time.
     *
     * @param dt the amount of time to advance
     */
    public void advance (double dt) {
        double upto = t + dt;
        boolean done = false;

        while (!pq.isEmpty()) {
            /*Get impending event; discard if invalidated.*/
            Event e = pq.min();
            if (!e.isValid()) {
                pq.delMin();
                continue;
            }
            assert t <= upto : "Time has advanced too much!";

            /*Update all positions up to the time of collision.*/
            double tfinal;
            if (e.time > upto) {
                tfinal = upto;
                done = true;
            } else {
                tfinal = e.time;
            }
            for (ParticleN part : particles) {
                part.move(tfinal - t);
            }
            t = tfinal;

            /*Process the collision and update the PQ with new collisions.
            * But if we're done, then forget it, I'm leaving!*/
            if (done) {
                return;
            }
            ParticleN a = e.a;
            ParticleN b = e.b;
            assert a != null : "The particle A shouldn't be null.";
            if (b != null) {
                a.bounceOff(b); // particle-particle collision
                predict(a);
                predict(b);
            } else {
                a.bounceOffNWall(e.N); // particle-wall collision
                if (DUMPWALLS) {
                    System.out.println(t+" "+e.N+" "+this.hashCode());
                }
                predict(a);
            }

            /*If the current time becomes equal to (or slightly greater) than upto,
            * then we have done enough advancing.*/
            if (t >= upto) {
                if (t > 120) {
                    System.err.println("JA DEU");
                }
                return;
            }
        }
        if (pq.isEmpty()) System.err.println("Empty priority queue, can you believe it!?!?!?!"); //assert
    }


    /**
     *  An event during a particle collision simulation. Each event contains
     *  the time at which it will occur (assuming no supervening actions)
     *  and the particles a and b involved.
     *
     *    -  a and b both not null, N == -1:  binary collision between a and b
     *    -  a not null, b null, N != -1:     collision with wall in the Nth axis
     */
    public static class Event implements Comparable<Event> {
        private final double time; // time that event is scheduled to occur
        final ParticleN a, b; // particles involved in event, possibly null
        private final int N; // axis in which a particle-wall collision occurred
        private final int countA, countB; // collision counts at event creation


        /**
         * Create a new event to occur at time {@code t} involving {@code a} and {@code b}.
         * If {@code a} is colliding with a wall, {@code b} should be {@code null} and
         * {@code N} should be the index of the axis that corresponds to that wall.
         * Otherwise, {@code N} should be -1.
         * */
        public Event (double t, ParticleN a, ParticleN b, int N) {
            this.time = t;
            this.a = a;
            this.b = b;
            assert a != null : "The particle a shouldn't be null.";
            countA = a.count();
            if (b != null) {
                countB = b.count();
                this.N = -1;
                assert N == -1 : "N should be -1 unless particle a is colliding with a wall.";
            } else {
                countB = -1;
                this.N = N;
            }
        }

        /**Compare times when two events will occur.*/
        public int compareTo(Event that) {
            return Double.compare(this.time, that.time);
        }

        /**Has any collision occurred between when event was created and now?
         * If so, then the event has been invalidated.*/
        public boolean isValid () {
            assert a != null : "The particle a shouldn't be null.";
            boolean validA = (a.count() == countA);
            boolean validB = (b == null || (b.count() == countB));
            return validA && validB;
        }
    }
}
