import com.jme3.math.FastMath;
import edu.princeton.cs.algs4.StdRandom;

import java.util.Arrays;

/**
 *  A particle moving in a box,
 *  with a given position, velocity, radius, and mass. Methods are provided
 *  for moving the particle and for predicting and resolvling elastic
 *  collisions with vertical walls, horizontal walls, and other particles.
 */
public class ParticleN {
    public static final double BORDERCOORDMAX = 20.0; //1 for stddraw
    public static final double BORDERCOORDMIN = -20.0; //0 for stddraw
    public static final float[] BLUE = new float[]{0, 0, 1, 1};
    public static final float[] RED = new float[]{1, 0, 0, 1};
    public static final float[] GREEN = new float[]{0, 1, 0, 1};
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double MINF = Double.NEGATIVE_INFINITY;
    private static final double VELRANGE = 10; //0.005 for stddraw
    private static final double DEFAULTRADIUS = 1; //0.02 for stddraw
    private static final double DEFAULTMASS = 0.5;
    private static final float[] DEFAULTCOLOR = new float[]{0, 1, 0, 1}; //green



    private static int GETOUTCNT = 0;


    public final int DIM; //number of translational degrees of freedom
    public final double radius;
    private double mass;
    private final double[] r; // position
    private final double[] v; // velocity
    private float[] color; // array of red, green, blue, alpha values
    private int count; // number of collisions so far
    private double savedMass = Double.NaN;

    // TODO: are overlapping particles a problem?
    // TODO: what if someone tries to spawn more particles than fit inside the volume? maybe set a cap?
    // TODO: should we always update both collision counts? does it matter?

    /**
     * Initializes a particle with the specified position, velocity, radius, mass, and color.
     */
    public ParticleN (double[] r, double[] v, double radius, double mass, float[] color) {
        this.r = r.clone(); //defensive copy
        this.v = v.clone(); //defensive copy
        this.DIM = this.r.length;
        this.radius = radius;
        this.mass = mass;
        this.color = color.clone(); //defensive copy
    }

    /**
     * Initializes an N-dimensional particle with a random position and velocity.
     * The position is uniform in the world box; the velocity is also chosen
     * uniformly at random.
     * @param  N number of dimensions
     */
    public ParticleN (int N) {
        double[] r = new double[N];
        double[] v = new double[N];

        for (int i = 0; i < N; i++) {
            /*Careful not to place anything out of the canvas!*/
            r[i] = StdRandom.uniform(BORDERCOORDMIN+DEFAULTRADIUS, BORDERCOORDMAX-DEFAULTRADIUS);
            v[i] = StdRandom.uniform(-VELRANGE, VELRANGE);
        }
        this.r = r;
        this.v = v;
        this.DIM = N;
        radius = DEFAULTRADIUS;
        mass   = DEFAULTMASS;
        color  = DEFAULTCOLOR;
    }

    /**Returns this particle's mass.*/
    public double mass () {
        return mass;
    }

    /**Returns (a copy of) this particle's color.*/
    public float[] color () {
        return this.color.clone();
    }

    /**Changes this particle's color.*/
    public void setColor (float[] color) {
        this.color = color.clone();
    }

    /**Returns the position in the Nth dimension.*/
    public double position (int N) {
        return r[N];
    }

    /**Returns the velocity in the Nth dimension.*/
    public double velocity (int N) {
        return v[N];
    }

    /**Prevent this particle from moving.*/
    public void immobilize () {
        savedMass = mass;
        mass = INFINITY;
        for (int i = 0; i < DIM; i++) {
            v[i] = 0;
        }
    }

    /**Frees this particle from imprisonment.*/
    public void free () {
        if (!Double.isNaN(savedMass)) {
            mass = savedMass;
        }
    }

    /**
     * Moves this particle in a straight line (based on its velocity)
     * for the specified amount of time.
     *
     * @param  dt the amount of time
     */
    public void move (double dt) {
        if (dt == MINF) {
            System.out.println("infinite movement1");
            System.exit(1);
        }
        if (Double.isInfinite(dt)) {
            System.out.println("inf mov 2");
            System.exit(1);
        }
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
    private static double[] deltaR (ParticleN part1, ParticleN part2) {
        double[] dr  = part2.r.clone();
        Couve.scaledIncrement(dr, -1, part1.r);

        return dr;
    }

    /** Returns the difference between the velocities of the two particles.
     * @return dv = part2.v - part1.v */
    private static double[] deltaV (ParticleN part1, ParticleN part2) {
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
    public double timeToHit (ParticleN that) {
        if (this == that) {
            return INFINITY;
        }

        /*The answer should be the same regardless of which particle is this or that.*/
        double[] dr = ParticleN.deltaR(this, that); //initial position
        double[] dv = ParticleN.deltaV(this, that); //initial velocity

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
        // TODO: are overlapping particles a problem?
        if (drdr < sigma*sigma) {
            System.out.println("overlapping particles!");
            return MINF;
        }
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

    /**Calculates timeToHitNWall for all walls.*/
    public double[] timeToHitWalls () {
        double[] times = new double[DIM];

        for (int i = 0; i < DIM; i++) {
            times[i] = timeToHitNWall(i);
        }
        return times;
    }

    /**
     * Updates the velocities of this particle and the specified particle according
     * to the laws of elastic collision. Assumes that the particles are colliding
     * at this instant.
     * This code also deals with collisions involving immobilized particles.
     *
     * @param  that the other particle
     */
    public void bounceOff (ParticleN that) {
        if (this.mass == INFINITY && that.mass == INFINITY) {
            assert false : "collision between 2 fixed particles";
            // might happen if they spawn inside one another
            // what if someone tries to spawn more particles than fit inside the volume?
            // we should also deal with overlapping particles
            return;
        }

        /*Calculate some important quantities.*/
        double[] dr = ParticleN.deltaR(this, that);
        double[] dv = ParticleN.deltaV(this, that);
        double dvdr = Couve.dotProduct(dv, dr);
        double dist = this.radius + that.radius;   // distance between particle centers at collision
        /*TODO This is only correct for frontal collisions...?*/

        /*Deal with infinities and update the velocities.*/
        boolean thisIsImmobilized = Double.isInfinite(this.mass);
        boolean thatIsImmobilized = Double.isInfinite(that.mass);
        if (thatIsImmobilized) {
            double velMagnitude = Math.sqrt(Couve.dotProduct(this.v, this.v));
            System.out.println("before:" + Arrays.toString(this.v)+" | "+Math.sqrt(Couve.dotProduct(this.v, this.v)));
            Couve.scale(0, this.v); //zero out
            Couve.scaledIncrement(this.v, -1 * velMagnitude / dist, dr);
            System.out.println("after:" + Arrays.toString(this.v)+" | " +Math.sqrt(Couve.dotProduct(this.v, this.v)) +"\n");
            this.count++;
            //that.count++;

        } else if (thisIsImmobilized) {
            double velMagnitude = Math.sqrt(Couve.dotProduct(that.v, that.v));
            System.out.println("before:"+ Arrays.toString(that.v)+" | "+Math.sqrt(Couve.dotProduct(that.v, that.v)));
            Couve.scale(0, that.v); //zero out
            Couve.scaledIncrement(that.v, 1*velMagnitude / dist, dr);
            System.out.println("after:"+ Arrays.toString(that.v)+" | "+Math.sqrt(Couve.dotProduct(that.v, that.v))+"\n");
            that.count++;
            //this.count++;

        } else { // regular collision

            /*Impulse from normal forces.*/
            double impulseMagnitude = -2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);
            //double[] impulse = Couve.scale(impulseMagnitude/dist, dr);
            System.out.println("  impulse: "+impulseMagnitude);

            Couve.scaledIncrement(this.v, -1 / this.mass * impulseMagnitude / dist, dr);
            Couve.scaledIncrement(that.v, 1 / that.mass * impulseMagnitude / dist, dr);

            /*Update collision counts.*/
            // TODO: should we always update both collision counts? does it matter?
            this.count++;
            that.count++;
        }

        /*Overriding subclasses have the chance to do something here.*/
        this.handleBinaryCollision(that);
    }

    /**Intentionally-not-implemented method that is called at the end of a binary collision.*/
    public void handleBinaryCollision (ParticleN that) {
        ;
    }

    /**Separates the two particles.*/
    public void getOut (ParticleN that) {
        // TODO: make getOut smooth and make it work if r1 == r2
        GETOUTCNT++;
        if (GETOUTCNT > 50) System.exit(1);
        double dist = this.radius + that.radius;   // distance between particle centers at collision
        double[] dr = ParticleN.deltaR(this, that);
        for (int i = 0; i < dr.length; i++) {
            if (dr[i] == 0) {
                System.out.println("exactly equal!!");
                System.out.println(Arrays.toString(dr));
                System.exit(1);
            }
        }

        // Might look better if switched around?
        System.out.println("SEPARATING PARTICLES!  radius1: "+this.radius+" radius2:"+that.radius);
        System.out.println("before (r1): "+Arrays.toString(this.r));
        System.out.println("before (r2): "+Arrays.toString(that.r));
        Couve.scaledIncrement(this.r, -that.radius / dist, dr);
        Couve.scaledIncrement(that.r, this.radius / dist, dr);
        System.out.println("after (r1): "+Arrays.toString(this.r));
        System.out.println("after (r2): "+Arrays.toString(that.r));
    }

    /**
     * Updates the velocity of this particle upon collision with a vertical
     * wall (by reflecting the velocity in the direction corresponding to the number N).
     * Assumes that the particle is colliding with a wall in the Nth axis at this instant.
     *
     * @param N the number which identifies an axis that connects two opposite walls
     */
    public void bounceOffNWall (int N) {
        v[N] = -v[N];
        count++;
    }
}

