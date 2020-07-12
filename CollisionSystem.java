import edu.princeton.cs.algs4.MinPQ;

/**
 *  The {@code CollisionSystem} class represents a collection of particles
 *  moving in a box, according to the laws of elastic collision.
 *  This event-based simulation relies on a priority queue.
 *  No rendering is done inside this class.
 */
public class CollisionSystem {


    /**TODO 4. advanceLazy*/
    /*TODO 3. preprocessing*/

    private final int DIM;
    private double t = 0.0;          // simulation clock time
    private MinPQ<Event> pq = new MinPQ<Event>();          // the priority queue
    private Particle[] particles;     // the array of particles

    /**
     * Initializes a system with the specified collection of particles.
     * The individual particles will be mutated during the simulation.
     *
     * @param particles the array of particles
     * @param N the number of spatial dimensions
     */
    public CollisionSystem (Particle[] particles, int N) {
        this.particles = particles.clone();   // defensive copy
        this.DIM = N;

        /*Initialize PQ with collision events.*/
        for (Particle part : particles) {
            assert part.DIM == N : "A particle has the wrong number of dimensions. All particles must be N-dimensional";
            predict(part);
        }
    }

    /** Updates the priority queue with all new events for particle a.*/
    public void predict (Particle a) {
        assert a != null : "Can't predict the behavior of a null particle, now, can we?";

        /* Particle-particle collisions.*/
        for (Particle part : particles) {
            double dt = a.timeToHit(part);
            if (dt != Double.POSITIVE_INFINITY) {
                if (dt > 1000000000) System.err.println("PART really huge time to collide"); //assert
                pq.insert(new Event(t + dt, a, part, -1));
            }
        }

        /* Particle-wall collisions.
        * The particle might hit multiple walls at once (a corner) if it's fat,
        * so we should check all walls.*/
        double[] times = a.timeToHitWalls();
        for (int i = 0; i < DIM; i++) {
            double dt = times[i];
            if (dt != Double.POSITIVE_INFINITY) {
                if (dt > 1000000000) System.err.println("WALL really great time to collide"); //assert
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
        boolean notdone = true;

        while (!pq.isEmpty() && notdone) {
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
                notdone = false;
            } else {
                tfinal = e.time;
                pq.delMin();
            }
            for (Particle part : particles) {
                part.move(tfinal - t);
            }
            t = tfinal;

            /*Process the collision and update the PQ with new collisions.
            * But if we're done, then forget it, I'm leaving!*/
            if (!notdone) {
                return;
            }
            Particle a = e.a;
            Particle b = e.b;
            assert a != null : "The particle A shouldn't be null.";
            if (b != null) {
                a.bounceOff(b); // particle-particle collision
                predict(a);
                predict(b);
            } else {
                a.bounceOffNWall(e.N); // particle-wall collision
                predict(a);
            }

            /*If the current time becomes equal to (or slightly greater) than upto,
            * then we have done enough advancing.*/
            if (t >= upto) {
                notdone = false;
                return;
            }
        }
        if (pq.isEmpty()) System.err.println("Empty priority queue, can you believe it!?!?!?!");
    }


    /***************************************************************************
     *  An event during a particle collision simulation. Each event contains
     *  the time at which it will occur (assuming no supervening actions)
     *  and the particles a and b involved.
     *
     *    -  a and b both not null, N == -1:  binary collision between a and b
     *    -  a not null, b null, N != -1:     collision with wall in the Nth axis
     ***************************************************************************/
    public static class Event implements Comparable<Event> {
        private final double time;         // time that event is scheduled to occur
        private final Particle a, b;       // particles involved in event, possibly null
        private final int N;
        private final int countA, countB;  // collision counts at event creation


        /**
         * Create a new event to occur at time {@code t} involving {@code a} and {@code b}.
         * If {@code a} is colliding with a wall, {@code b} should be {@code null} and
         * {@code N} should be the index of the axis that corresponds to that wall.
         * Otherwise, {@code N} should be -1.
         * */
        public Event (double t, Particle a, Particle b, int N) {
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
        public boolean isValid() {
            assert a != null : "The particle a shouldn't be null.";
            boolean validA = (a.count() == countA);
            boolean validB = (b == null || (b.count() == countB));
            return validA && validB;
        }
    }
}
