/******************************************************************************
 *  Compilation:  javac Particle.java
 *  Execution:    none
 *  Dependencies: StdDraw.java
 *
 *  A particle moving in the unit box with a given position, velocity,
 *  radius, and mass.
 *
 ******************************************************************************/

import java.awt.Color;

/**
 *  The {@code Particle} class represents a particle moving in the unit box,
 *  with a given position, velocity, radius, and mass. Methods are provided
 *  for moving the particle and for predicting and resolvling elastic
 *  collisions with vertical walls, horizontal walls, and other particles.
 *  This data type is mutable because the position and velocity change.
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/61event">Section 6.1</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class Particle {
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double BORDERCOORDMAX = 1.0;
    private static final double BORDERCOORDMIN = 0.0;

    private double[] r;        // position
    private double[] v;        // velocity
    private int count;            // number of collisions so far
    private final double radius;  // radius
    private final double mass;    // mass
    private final String color;    // color;
    /*use String to make it compatible both with StdDraw and jme?*/


    /**
     * Initializes a particle with the specified position, velocity, radius, mass, and color.
     *
     * @param  r <em>x</em>-coordinates of position
     * @param  v <em>x</em>-coordinates of velocity
     * @param  radius the radius
     * @param  mass the mass
     * @param  color the color
     */
    public Particle (double[] r, double[] v, double radius, double mass, String color) {
        this.r = r.clone(); //defensive copy
        this.v = v.clone(); //defensive copy
        this.radius = radius;
        this.mass   = mass;
        this.color  = color;
    }

    /**
     * Initializes a particle with a random position and velocity.
     * The position is uniform in the unit box; the velocity in
     * either direciton is chosen uniformly at random.
     */
    /*public Particle() {
    //TODO

        rx     = StdRandom.uniform(0.0, 1.0);
        ry     = StdRandom.uniform(0.0, 1.0);
        vx     = StdRandom.uniform(-0.005, 0.005);
        vy     = StdRandom.uniform(-0.005, 0.005);
        radius = 0.02;
        mass   = 0.5;
        color  = "BLACK";
    }*/

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
     * Draws this particle to standard draw.
     */
    public void draw() {
        //TODO
        //StdDraw.setPenColor(color);
       //StdDraw.filledCircle(rx, ry, radius);
    }

    /**
     * Returns the number of collisions involving this particle with
     * vertical walls, horizontal walls, or other particles.
     * This is equal to the number of calls to {@link #bounceOff},
     * {@link #bounceOffNWall}.
     *
     * @return the number of collisions involving this particle with
     *         walls, or other particles
     */
    public int count() {
        return count;
    }

    /**Returns dr = part2.r - part1.r */
    private static double[] deltaR (Particle part1, Particle part2) {
        double[] dr  = part2.r.clone();
        Couve.scaledIncrement(dr, -1, part1.r);

        return dr;
    }
    /**Returns dv = part2.v - part1.v */
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
        if (this == that) {
            return INFINITY;
        }

        /*The answer should be the same regardless of which particle is this or that.*/
        double[] dr = Particle.deltaR(this, that);
        double[] dv = Particle.deltaV(this, that);

        /*Both of these would give a messed up time.*/
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
        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        // if (drdr < sigma*sigma) StdOut.println("overlapping particles");
        /*This would also give a weird time.*/
        if (d < 0) {
            return INFINITY;
        }

        /*Solution of dr.dr = sigma^2, with dr = dr_0 + t*dv */
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    /**
     * Returns the amount of time for this particle to collide with a vertical
     * wall, assuming no interening collisions.
     *
     * @return the amount of time for this particle to collide with a vertical wall,
     *         assuming no interening collisions;
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

        // magnitude of normal force
        double magnitude = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);

        // normal force in all axes
        double[] f = Couve.scale(magnitude/dist, dr);

        // update velocities according to normal force
        Couve.scaledIncrement(this.v, 1/this.mass, f);
        Couve.scaledIncrement(that.v, -1/that.mass, f);

        // update collision counts
        this.count++;
        that.count++;
    }

    /**
     * Updates the velocity of this particle upon collision with a vertical
     * wall (by reflecting the velocity in the <em>x</em>-direction).
     * Assumes that the particle is colliding with a vertical wall at this instant.
     */
    public void bounceOffNWall (int N) {
        assert 0<=N && N<v.length: "Invalid axis for wall collision.";
        v[N] = -v[N];
        count++;
    }
}

