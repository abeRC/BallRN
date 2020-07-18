import edu.princeton.cs.algs4.MinPQ;

/**
 *  The {@code CollisionSystem} class represents a collection of particles
 *  moving in a box, according to the laws of elastic collision.
 *  This event-based simulation relies on a priority queue.
 *  No rendering is done inside this class.
 */
public class CollisionSystemRN {

    /*TODO B1. preprocessing*/
    // TODO: [particle limit] what if someone tries to spawn more particles than fit inside the volume? maybe set a cap?
    //      complication: Highest [sphere packing] density is known only in case of 1, 2, 3, 8 and 24 dimensions.
    //      also, the radii could be different
    //      maybe guess that getting >70% to work is likely too hard to bother with?
    // TODO: completely fix particles inside other particles

    private static final double MINF = Double.NEGATIVE_INFINITY;
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
     * @param DUMPWALLS whether to dump information about particle-wall collisions
     */
    public CollisionSystemRN (ParticleN[] particles, int N, boolean DUMPWALLS) {
        this.particles = particles.clone();   // defensive copy
        this.DIM = N;
        this.DUMPWALLS = DUMPWALLS;

        /*Initialize PQ with collision events.*/
        for (int i = 0; i < particles.length; i++) {
            ParticleN part = particles[i];

            if (part.DIM != N) {
                throw new IndexOutOfBoundsException("A particle has the wrong number of dimensions. All particles must be N-dimensional");
            }

            /*Try to get this particle out of other particles.*/
            // TODO [particle limit]
            for (int times = 0; times < 3; times++) {
                for (int j = 0; j < i; j++) {
                    System.out.println("checking whether " + part + " is inside " + particles[j]);
                    if (part.isInside(particles[j])) {
                        part.getOut(particles[j]); // If this makes it be inside particles <j, then so be it
                        System.out.println("GETTING " + part.hashCode() + " OUT OF" + particles[j].hashCode());
                    }
                }
            }
            /*Fill the PQ with this particle's predicted (possible) collisions.*/
            predict(part);
        }
    }

    /**
     * Initializes a system with the specified collection of particles.
     * The individual particles will be mutated during the simulation.
     *
     * @param particles the array of particles
     * @param N the number of spatial dimensions
     */
    public CollisionSystemRN (ParticleN[] particles, int N) {
        this(particles, N, false);
    }

    /*Marks events in the PQ containing particle a as invalid.*/
    private void clearPQof (ParticleN a) {
        for (Event e : pq) {
            if (e.a == a || e.b == a) {
                e.knownInvalid = true;
            }
        }
    }

    /** Updates the priority queue with all new events for particle a.*/
    private void predict (ParticleN a) {
        assert a != null : "Can't predict the behavior of a null particle, now, can we?";

        /*Check if the particle is inside the world bounds and, if not, teleports it to 0.*/
        boolean didSomething = a.assureInsideBounds();
        if (didSomething) {
            clearPQof(a);
            for (ParticleN other : particles) {
                if (a != other && a.isInside(other)) {
                    a.getOut(other);
                }
            }
        }

        /* Particle-particle collisions.*/
        for (ParticleN part : particles) {
            double dt = a.timeToHit(part);

            if (dt != Double.POSITIVE_INFINITY) {
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
            System.out.println(e);
            if (!e.isValid()) {
                pq.delMin();
                continue;
            }

            /*Update all positions up to the time of collision.*/
            double tfinal;
            if (e.time != MINF) { // e.time == -inf indicates a getOut event
                if (e.time > upto) {
                    tfinal = upto;
                    done = true;
                } else {
                    tfinal = e.time;
                }
                for (ParticleN part : particles) {
                    if (Double.isInfinite(tfinal-t)) {
                        System.out.println("infinite movement here"); //erase
                        System.exit(1);
                    }
                    part.move(tfinal - t);
                }
                t = tfinal;
            }

            /*Process the collision and update the PQ with new collisions.
            * But if we're done, then forget it, I'm leaving!*/
            if (done) {
                return;
            }
            ParticleN a = e.a;
            ParticleN b = e.b;
            if (b != null) {
                if (e.time == MINF) { /*One particle is inside the other*/
                    a.getOut(b);
                    //predict(a); //erase
                    //predict(b);
                } else {
                    if (e.time == MINF) {
                        System.out.println("infinite movement inside the else"); //erase
                        System.exit(1);
                    }

                    a.bounceOff(b); /*Particle-particle collision.*/
                    predict(a);
                    predict(b);
                }
            } else {
                /*Particle-wall collision.*/
                a.bounceOffNWall(e.N);
                if (DUMPWALLS) {
                    System.out.println(t+" "+e.N+" "+a.hashCode());
                }
                predict(a);
            }

            /*Remember to exclude the event if we've processed it.*/
            if (pq.isEmpty()) {
                System.out.println("it shouldn't be empty"); //erase
                throw new ArrayIndexOutOfBoundsException("it shouldn't be empty");
            } else {
                pq.delMin();
            }

            /*If the current time becomes equal to upto,
            * then we have done enough advancing.*/
            assert t <= upto : "t is greater than upto at a point of the loop where it shouldn't be.";
            if (t == upto) {
                return;
            }
        }
        System.out.println("it shouldn't be empty"); //erase
        throw new ArrayIndexOutOfBoundsException("it shouldn't be empty"); //erase
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
        private boolean knownInvalid = false;


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

            return !knownInvalid && validA && validB;
        }
        /**String representation.*/
        @Override
        public String toString() {
            if (b == null) { // wall collision
                return "Event{" +
                        "time=" + time +
                        ", a=" + a.hashCode() +
                        ", b= null" +
                        ", N=" + N +
                        ", countA=" + countA +
                        ", countB=" + countB +
                        '}';
            } else { // binary collision
                return "Event{" +
                        "time=" + time +
                        ", a=" + a.hashCode() +
                        ", b=" + b.hashCode() +
                        ", N=" + N +
                        ", countA=" + countA +
                        ", countB=" + countB +
                        '}';
            }
        }
    }
}
