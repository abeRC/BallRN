import java.awt.Color;

/**
 *  A particle moving in a box,
 *  with a given position, velocity, radius, and mass. Methods are provided
 *  for moving the particle and for predicting and resolvling elastic
 *  collisions with vertical walls, horizontal walls, and other particles.
 */
public class Particle {
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double BORDERCOORDMAX = 1.0;
    private static final double BORDERCOORDMIN = 0.0;
    private static final double VELRANGE = 0.005;
    private static final double DEFAULTRADIUS = 0.02;
    private static final double DEFAULTMASS = 0.5;

    public final int DIM; //number of translational degrees fo freedom
    public final double radius;
    public final double mass;
    public final String color; /*use String to make it compatible both with StdDraw and jme?*/
    private double[] r; // position
    private double[] v; // velocity
    private int count; // number of collisions so far


    /**
     * Initializes a particle with the specified position, velocity, radius, mass, and color.
     */
    public Particle (double[] r, double[] v, double radius, double mass, String color) {
        this.r = r.clone(); //defensive copy
        this.v = v.clone(); //defensive copy
        this.DIM = this.r.length;
        assert this.DIM == this.v.length: "Particle creation failure. Position and velocity must have the same number of dimensions.";
        this.radius = radius;
        this.mass   = mass;
        this.color  = color;
    }

    /**
     * Initializes an N-dimensional particle with a random position and velocity.
     * The position is uniform in the world box; the velocity is also chosen
     * uniformly at random.
     * @param  N number of dimensions
     */
    public Particle (int N) {

        double[] r = new double[N];
        double[] v = new double[N];

        for (int i = 0; i < N; i++) {
            r[i] = StdRandom.uniform(BORDERCOORDMIN, BORDERCOORDMAX);
            v[i] = StdRandom.uniform(-VELRANGE, VELRANGE);
        }
        this.r = r;
        this.v = v;
        this.DIM = N;
        radius = DEFAULTRADIUS;
        mass   = DEFAULTMASS;
        color  = "BLACK";
    }

    /**
     * Moves this particle in a straight line (based on its velocity)
     * for the specified amount of time.
     *
     * @param  dt the amount of time
     */
    public void move (double dt) {
        Couve.scaledIncrement(r, dt, v); //r += v*dt
    }

    /**
     * Returns the number of collisions involving this particle with
     * vertical walls, horizontal walls, or other particles.
     * This is equal to the number of calls to {@link #bounceOff} and
     * {@link #bounceOffNWall}.
     *
     * @return the number of collisions involving this particle with
     *         walls or other particles
     */
    public int count () {
        return count;
    }

    /** Returns the difference between the positions of the two particles.
     * @return dr = part2.r - part1.r */
    private static double[] deltaR (Particle part1, Particle part2) {
        double[] dr  = part2.r.clone();
        Couve.scaledIncrement(dr, -1, part1.r);

        return dr;
    }

    /** Returns the difference between the velocities of the two particles.
     * @return dv = part2.v - part1.v */
    private static double[] deltaV (Particle part1, Particle part2) {
        double[] dv  = part2.v.clone();
        Couve.scaledIncrement(dv, -1, part1.v);

        return dv;
    }

    /**
     * Returns the amount of time for this particle to collide with the specified
     * particle, assuming no intervening collisions.
     *
     * @param  that the other particle
     * @return the amount of time for this particle to collide with the specified
     *         particle, assuming no intervening collisions;
     *         {@code Double.POSITIVE_INFINITY} if the particles will not collide
     */
    public double timeToHit (Particle that) {
        assert this.DIM == that.DIM: "Particles have different translational degrees of freedom.";
        if (this == that) {
            return INFINITY;
        }

        /*The answer should be the same regardless of which particle is this or that.*/
        double[] dr = Particle.deltaR(this, that); //initial position
        double[] dv = Particle.deltaV(this, that); //initial velocity

        double dvdr = Couve.dotProduct(dv, dr);
        if (dvdr > 0) {
            return INFINITY;
        }
        double dvdv = Couve.dotProduct(dv, dv);
        if (dvdv == 0) {
            return INFINITY;
        }

        double drdr = Couve.dotProduct(dr, dr);
        double sigma = this.radius + that.radius;
        // if (drdr < sigma*sigma) StdOut.println("overlapping particles");
        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        if (d < 0) {
            return INFINITY;
        }

        /*Solution of dr.dr = sigma^2, with dr = dr_0 + t*dv */
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    /**
     * Returns the amount of time for this particle to collide with a wall
     * in the Nth axis, assuming no intervening collisions.
     *
     * @param N the number which identifies an axis that connects two opposite walls
     * @return the amount of time for this particle to collide with a vertical wall,
     *         assuming no intervening collisions;
     *         {@code Double.POSITIVE_INFINITY} if the particle will not collide
     *         with a vertical wall
     */
    public double timeToHitNWall (int N) {
        //current values
        double vel = v[N];
        double pos = r[N];

        if (vel > 0) {
            return (BORDERCOORDMAX - pos - radius) / vel;
        } else if (vel < 0) {
            return (pos - BORDERCOORDMIN - radius) / -vel; //vel is negative!
        } else {
            return INFINITY;
        }
    }

    /**
     * Updates the velocities of this particle and the specified particle according
     * to the laws of elastic collision. Assumes that the particles are colliding
     * at this instant.
     *
     * @param  that the other particle
     */
    public void bounceOff (Particle that) {
        double[] dr = Particle.deltaR(this, that);
        double[] dv = Particle.deltaV(this, that);
        double dvdr = Couve.dotProduct(dv, dr);
        double dist = this.radius + that.radius;   // distance between particle centers at collision

        // impulse from normal forces
        double magnitude = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);
        double[] J = Couve.scale(magnitude/dist, dr);

        // update velocities according to the impulse
        Couve.scaledIncrement(this.v, 1/this.mass, J);
        Couve.scaledIncrement(that.v, -1/that.mass, J);

        // update collision counts
        this.count++;
        that.count++;
    }

    /**
     * Updates the velocity of this particle upon collision with a vertical
     * wall (by reflecting the velocity in the direction corresponding to the number N).
     * Assumes that the particle is colliding with a wall in the Nth axis at this instant.
     *
     * @param N the number which identifies an axis that connects two opposite walls
     */
    public void bounceOffNWall (int N) {
        assert 0<=N && N<v.length: "Invalid axis for wall collision.";
        v[N] = -v[N];
        count++;
    }
}

