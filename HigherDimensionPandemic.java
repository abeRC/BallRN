import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

/**
 * Simulates a higher-dimension pandemic and displays a 3D (3 possibly random dimensions
 * out of N) view of it with jMonkeyEngine.
 * */
public class HigherDimensionPandemic extends SimpleApplication {
    /**TODO A1. Set up program arguments (dimension, number of particles, radius, etc?)
     *  Take optional parameters starting from no. of particles.*/
    /**TODO A2. attach the walls to a node and possibly translate it so it's
     *  in the middle of the simulation borders*/
    /** TODO A3. gradual velocity increase up to a certain point (then it's instant)*/
    /**TODO A4. read from stdin*/


    /*
     * The jME game loop has 3 phases:
     *      Init: The simpleInitApp() method is executed only once, right at the beginning;
     *      Update: The simpleUpdate() method runs repeatedly, during the game.
     *      Render: After every update, the jMonkeyEngine automatically redraws (renders)
     *              the screen for you.
     * To make a jME game, we extend SimpleApplication and override the methods that handle
     * the init and update phases.
     *
     * The game scene is made out of Spatial objects, which can be divided into 2 subclasses:
     *      Geometry, which is a mesh (shape) with a material;
     *      Node, which is an invisible handler for grouping Geometry and Node objects together.
     * Only Geometry objects attached to the rootNode are displayed.
     * */

    public static final double baseRecoveryTime = 20;
    public static int DIM;
    public static int[] priDim = {0, 1, 2}; // priviliged dimensions
    private static CollisionSystemRN cs;
    private int BEINGCOUNT = 0;
    private static Being[] beings;

    /**Implement extra methods to simulate the pandemic.*/
    private static class PartN extends ParticleN {
        private char status = 'S'; // S(usceptible)/I(nfected)/R(ecovered)
        private double timer = 0;

        public PartN (double[] r, double[] v, double radius, double mass, float[] color) {
            super(r, v, radius, mass, color);
        }
        public PartN (int N) {
            super(N);
        }

        /**If one of the particles is infected, the other one becomes infected as well.*/
        @Override
        public void handleBinaryCollision (ParticleN that) {
            PartN p = (PartN) that;
            if (this.status == 'I') {
                p.status = 'I';
                p.setColor(ParticleN.RED);
                p.timer = StdRandom.uniform(baseRecoveryTime*0.5, baseRecoveryTime*1.5);
            }
            if (p.status == 'I') {
                this.status = 'I';
                this.setColor(ParticleN.RED);
                this.timer = StdRandom.uniform(baseRecoveryTime*0.5, baseRecoveryTime*1.5);
            }
        }

        /**Return a character representing this particle's status:
         * 'S': susceptible;
         * 'I': infected;
         * 'R': recovered.
         * */
        public char getStatus () {
            return status;
        }
    }

    public static void main (String[] args){
        // 3 -> no de parts; parts.length -> no de parts; cs.particles.length -> no de parts
        if (args.length == 1) {
            DIM = Integer.parseInt(args[0]);
            if (DIM < 3) {
                throw new IllegalArgumentException("Invalid number of dimensions.");
            }
        } else {
            System.err.println("Please provide exactly 1 parameter: the number of dimensions.");
            System.exit(1);
        }

        /*Scientifically determine the correct dimensions to analyze.*/
        //pick3dimensions();

        /*Create higher physics.*/
        PartN[] parts = new PartN[20];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new PartN(DIM);
        }
        cs = new CollisionSystemRN(parts, DIM);

        /*Initiate application.*/
        HigherDimensionPandemic app = new HigherDimensionPandemic();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Title", "HigherDimensionPandemic"); as.put("VSync", true); as.put("Width", 1280); as.put("Height", 720);
        app.setShowSettings(false); // Uncomment to skip the initial settings popup
        app.setSettings(as);
        app.start();
    }

    /**Initialization phase.*/
    @Override
    public void simpleInitApp () {
        flyCam.setMoveSpeed(7); // Make the camera more bearable.

        /*Put up walls N-dimensionally. (Actually, they're just for show)*/
        makewalls();

        /*Manifest the physical forms of our beings.*/
        beings = new Being[cs.particles.length];
        for (int i = 0; i < beings.length; i++) {
            beings[i] = new Being((PartN)cs.particles[i]);
        }
    }

    /**Main event loop.
     * @param tpf time per frame*/
    @Override
    public void simpleUpdate (float tpf) {
        /*Advance the simulation and update the positions of all beings.*/
        cs.advance(tpf);
        for (Being b : beings) {
            b.updatePos();
        }
    }

    /**Arbitrarily pick 3 dimensions to display.*/
    private static void pick3Dimensions () {
        int cnt = 0;
        while (cnt < 3) {
            int r = StdRandom.uniform(DIM);
            boolean ok = true;
            for (int i = 0; i < cnt && ok; i++) {
                if (r == priDim[i]) {
                    ok = false;
                }
            }
            if (ok) {
                priDim[cnt] = r;
                cnt++;
            }
        }
    }

    /**Conjure up the (just for show) walls.*/
    private void makewalls () {
        float halfPi = (float) Math.PI/2;
        float halfWallDistance = 0.5f*(float) (ParticleN.BORDERCOORDMAX - ParticleN.BORDERCOORDMIN);
        float wallThickness = 2;
        float p = halfWallDistance + wallThickness;

        Vector3f[] positions = {
                new Vector3f(0, 0, p),
                new Vector3f(0, 0, -p),
                new Vector3f(p, 0, 0),
                new Vector3f(-p, 0, 0),
                new Vector3f(0, p, 0),
                new Vector3f(0, -p, 0),
        };

        // Starry background
        //Texture space = assetManager.loadTexture("assets/Pictures/Cosmic Winter Wonderland.jpg");
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); //default material
        //mat.setTexture("ColorMap", space);
        Box mesh = new Box(halfWallDistance, 2, halfWallDistance);
        for (int i = 0; i < 6; i++) {
            // Randomly-colored opaque walls
            mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            mat.setColor("Color", ColorRGBA.randomColor());

            Geometry g = new Geometry("wall" + i, mesh);
            g.setMaterial(mat);
            g.setLocalTranslation(positions[i]);

            /*0 <= i < 2: xAngle = halfPi, yAngle = 0, zAngle = 0
            * 2 <= i < 4: xAngle = 0,      yAngle = 0, zAngle = halfPi
            * 4 <= i < 6: xAngle = 0,      yAngle = 0, zAngle = 0*/
            g.rotate((1-i/2+i/4)*halfPi, 0, (i/2-i/4-i/4)*halfPi); // integer division is for comapctness
            rootNode.attachChild(g); //add the wall to the scene
        }
    }

    /**Class that integrates Particles into jME's logic.*/
    private class Being {
        private final PartN p;
        private final Geometry g;

        /**Update the Geometry's position based on the particle's position in the priviliged dimensions.*/
        private void updatePos () {
            g.setLocalTranslation(
                    (float) p.position(priDim[0]),
                    (float) p.position(priDim[1]),
                    (float) p.position(priDim[2]));
        }

        /**Update the Geometry's material's color based on the color data in the particle.*/
        private void updateColor () {
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            float[] col = p.color();
            mat.setColor("Color", new ColorRGBA(col[0], col[1], col[2], col[3]));
            g.setMaterial(mat);
        }

        /**Create a Mesh and a Material according to the properties of p,
         * make a Geometry out of them and attach the Geometry to the rootNode.*/
        private Being (PartN p) {
            Sphere mesh = new Sphere(30, 30, (float) p.radius); // First 2 arguments control the quality of the sphere.
            String geometryName = "ball"+BEINGCOUNT;
            BEINGCOUNT++;

            Geometry g = new Geometry(geometryName, mesh);

            this.p = p;
            this.g = g;
            updatePos(); // set initial coordinates
            updateColor(); // create material with p.color()
            rootNode.attachChild(g); //add the Being to the scene
        }
    }
}
