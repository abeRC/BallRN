import edu.princeton.cs.algs4.MinPQ;

/**
 *  The {@code CollisionSystem} class represents a collection of particles
 *  moving in a box, according to the laws of elastic collision.
 *  This event-based simulation relies on a priority queue.
 *  No rendering is done inside this class.
 */
public class CollisionSystem {

    /** TODO 2. gradual velocity increase up to a certain point (then it's instant)*/

    /*TODO 3. preprocessing*/

    private final int DIM;
    private MinPQ<Event> pq;          // the priority queue
    private double t = 0.0;          // simulation clock time
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
        for (Particle part : particles) { //assert
            assert part.DIM == N : "A particle has the wrong number of dimensions. All particles must be N-dimensional";
        } //assert
    }

    /**
     * Updates the priority queue with all new events for particle a, up to time {@code limit}.
     *
     * @param a the particle whose behavior we wish to predict
     * @param limit the maximum time of the simulation
     * */
    public void predict (Particle a, double limit) {
        assert a!= null : "Can't predict the behavior of a null particle, now, can we?";

        /* Particle-particle collisions.
        * Even if limit == INFINITY, never insert if dt == INFINITY.*/
        for (Particle part : particles) {
            double dt = a.timeToHit(part);
            if (t + dt < limit) {
                pq.insert(new Event(t + dt, a, part, -1));
            }
        }

        /* Particle-wall collisions.
        * The particle might hit multiple walls at once (a corner) if it's fat,
        * so we should check all walls.*/
        double[] times = a.timeToHitWalls();
        for (int i = 0; i < DIM; i++) {
            double dt = times[i];
            if (t+dt < limit) {
                pq.insert(new Event(t+dt, a, null, i));
            }
        }
    }

    /**
     * Simulates the system of particles for the specified amount of time.
     *
     * @param limit the maximum time of the simulation
     */
    public void simulate (double limit) {
        /*Initialize PQ with collision events.*/
        pq = new MinPQ<Event>();
        for (Particle part : particles) {
            predict(part, limit);
        }

        /*The main event-driven simulation loop.*/
        while (!pq.isEmpty()) {
            /*Get impending event; discard if invalidated.*/
            Event e = pq.delMin();
            if (!e.isValid()) {
                continue;
            }

            Particle a = e.a;
            Particle b = e.b;
            assert a != null : "The particle a shouldn't be null.";

            /*Update all positions up to the time of collision.*/
            for (Particle part : particles) {
                part.move(e.time - t);
            }
            t = e.time;

            /*Process the collision and update the PQ with new collisions.*/
            if (b != null) {
                a.bounceOff(b); // particle-particle collision
                predict(a, limit);
                predict(b, limit);
            } else {
                a.bounceOffNWall(e.N); // particle-wall collision
                predict(a, limit);
            }
        }
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
