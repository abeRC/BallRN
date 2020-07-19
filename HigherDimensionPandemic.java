import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import edu.princeton.cs.algs4.BST;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.LinearProbingHashST;

import java.util.*;


/**
 * Simulates a higher-dimension pandemic and displays a 3D (3 possibly random dimensions
 * out of N) view of it with jMonkeyEngine.
 * */
public class HigherDimensionPandemic extends SimpleApplication {
    // TODO shadows? (maybe create a black circle on the lower wall for each ball?)


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

    public static final double baseRecoveryTime = 42;
    private static double INITIAL_INFECTED = 0.1;
    private static double PERCENTAGE_IMMOBILIZED = 0.75;
    public static int NUM;
    public static int DIM;
    public static int[] priDim = {0, 1, 2}; // priviliged dimensions
    private static CollisionSystemRN cs;
    private int BEINGCOUNT = 0;
    private static Being[] beings;
    private static TreeSet<String> options = new TreeSet<>();
    private static int[] dimpnum = new int[2];
    private static int infectedBeings = 0;
    private static int susceptibleBeings = 0;
    private static int recoveredBeings = 0;
    private static String[] codes = {
            "chart",
            "dumpwalls",
            "dumpevents",
            "space",
            "texturedballs",
            "socialdistancing",
            "fullscreen",
    };
    private static TreeMap<String, Boolean> boolStrings = new TreeMap<>();
    private static Material spaceMat; // to avoid uninitialized variable error
    private static Texture lagoonTex;
    float time = 0;

    /**Implement extra methods to simulate a pandemic.*/
    private static class PartN extends ParticleN {
        private char status = 'S'; // S(usceptible)/I(nfected)/R(ecovered)
        private double timer = 0;

        public PartN (int N) {
            super(N);

            if (AC("socialdistancing")) {
                if (StdRandom.bernoulli(PERCENTAGE_IMMOBILIZED)) {
                    immobilize();
                }
            }

            susceptibleBeings++;
            if (StdRandom.bernoulli(INITIAL_INFECTED)) {
                infect();
            }
        }

        /**Infect this particle.*/
        private void infect () {
            if (this.status == 'R') recoveredBeings--;
            else susceptibleBeings--;
            infectedBeings++;
            status = 'U'; // updateInfection
            setColor(ParticleN.RED);
            timer = 5+Math.abs(StdRandom.gaussian(baseRecoveryTime, 4.2));
            /*In reality, this would look more like a gamma/Weibull/log-normal distribution*/
        }


        /**If one of the particles is infected, the other one becomes infected as well.*/
        @Override
        public void handleBinaryCollision (ParticleN that) {
            PartN p = (PartN) that;
            if (this.status == 'I' && p.status == 'S') {
                p.infect();
            }
            if (p.status == 'I' && this.status == 'S') {
                this.infect();
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

    /**Print program usage and all accepted flags.*/
    public static void printUsage (int exitCode) {
        String usage = "Simulate an N-dimensional pandemic with CollisionSystemRN.java,\n" +
                "and display it with jMonkeyEngine.\n" +
                "\n" +
                "   java HigherDimensionalPandemic DIM PNUM [FLAGS]\n" +
                "\n" +
                "where DIM is the number of dimensions and PNUM is the number of random\n" +
                "particles to create.\n" +
                "Extra options: \n" +
                "   --help                      Print this information.\n" +
                "   --fullscreen                Display in fullscreen.\n" +
                "   --chart                     Draw a chart using StdDraw.\n" +
                "   --space                     Use a space texture for the cube.\n" +
                "   --textured-balls            Use a lagoon texture for the balls.\n" +
                "   --social-distancing         Restrict movement by 3/4.\n" +
                "   --max-social-distancing     Restrict movement by 7/8.\n" +
                "   --dump-walls                Dump wall collision information to stdout.\n" +
                "   --dump-events                Dump PQ event information to stdout.\n";

        if (exitCode == 0) {
            System.out.println(usage);
        } else {
            System.err.println(usage);
        }
        System.exit(exitCode);
    }

    /**Dump general information about this execution.*/
    public static void dumpGeneral () {
        System.out.println("DIM: "+DIM+"\n"+
                "PNUM: "+NUM+"\n"+
                "PRIDIM: "+ Arrays.toString(priDim)+"\n"
        );
    }

    /**Initialize treemap with our mappings for strings to indices in BOOLEANS.*/
    private static void beforeFillBools () {
        for (int i = 0; i < codes.length; i++) {
            boolStrings.put(codes[i], false);
        }
    }

    /**Mark a boolean in BOOLEANS as true.*/
    private static void fillBools (String s) {
        if (options.contains(s)) {
            if (!boolStrings.containsKey(s)) {
                System.err.println("Unrecgonized option: "+s+"\n");
                printUsage(1);
            } else {
                boolStrings.put(s, true);
            }
        }
    }

    /**Parse command-line arguments.*/
    private static void parse (String[] args, boolean verboseParse) {
        int dimpnumfilled = 0;
        boolean expectingNumber = false;
        for (int i = 0; i < args.length; i++) {
            String parsed = args[i].replaceAll("[^a-z0-9.]", "").toLowerCase();

            if (expectingNumber) { /*Next token is a parameter to --social-distancing.*/
                try {
                    double d = Double.parseDouble(parsed);
                    if (d < 0 || 1 < d) {
                        throw new IllegalArgumentException("");
                    }
                    INITIAL_INFECTED = d;
                    expectingNumber = false;
                } catch(Exception e) {
                    System.err.println("Parameter to --social-distancing must be a valid double from 0.0 to 1.0\n");
                    printUsage(1);
                }
            } else {
                /*Try to parse as a main argument.*/
                try {
                    int p = Integer.parseInt(parsed);
                    dimpnum[dimpnumfilled] = p;
                    dimpnumfilled++;
                    continue;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Too many main arguments.\n");
                    printUsage(1);
                } catch (Exception ignored) {
                    ;
                }

                if (parsed.equals("socialdistancing")) {
                    options.add("socialdistancing");
                    expectingNumber = true;
                } else {
                    options.add(parsed);
                }
            }
        }
        if (options.contains("help")) { /*Print help if it seems like help is requested.*/
            printUsage(0);
        }
        if (expectingNumber) {
            System.out.println("Assuming default value of "+PERCENTAGE_IMMOBILIZED+" for --social-distancing.");
        }
        if (dimpnumfilled != 2) {
            System.err.println("Not enough main arguments provided.\n");
            printUsage(1);
        }

        /*Everything ok up to this point.*/

        if (verboseParse) {
            System.out.println("Main arguments: " + Arrays.toString(dimpnum));
            System.out.println("Options: " + options);
        }
        DIM = dimpnum[0];
        if (DIM < 3) {
            System.err.println("Invalid number of dimensions.\n");
            printUsage(1);
        }
        NUM = dimpnum[1];

        /*Set the values of the boolean variables.*/
        beforeFillBools();
        for (String opt : options) {
            fillBools(opt);
        }
    }

    /**Access the boolean variable corresponding to String s.*/
    private static boolean AC (String s) {
        return boolStrings.get(s.toLowerCase());
    }

    public static void main (String[] args) {
        if (args.length < 2) {
            System.err.println("Please provide at least the 2 main arguments.\n");
            printUsage(1);
        }

        parse(args, false);

        /*Scientifically determine the correct dimensions to analyze.*/
        pick3Dimensions();

        /*Dump general info.*/
        if (AC("dumpwalls")) {
            dumpGeneral();
        }

        /*Create higher physics.*/
        PartN[] parts = new PartN[NUM];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new PartN(DIM);
        }
        cs = new CollisionSystemRN(parts, DIM, AC("dumpwalls"), AC("dumpevents"));

        /*Initiate application.*/
        HigherDimensionPandemic app = new HigherDimensionPandemic();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Title", "HigherDimensionPandemic");
        as.put("VSync", true);
        as.put("Width", 1280);
        as.put("Height", 720);
        if (AC("fullscreen")) {
            as.put("Fullscreen",true);
            as.put("Width", 1920);
            as.put("Height", 1080);
        }
        // app.setShowSettings(false); // Uncomment to skip the initial settings popup
        app.setSettings(as);
        app.start();
    }

    /**Initialization phase.*/
    @Override
    public void simpleInitApp () {
        if (AC("chart")) {
            StdDraw.setCanvasSize(800, 100);
        }
        flyCam.setMoveSpeed(8f); // Make the camera more bearable.

        if (AC("texturedballs")) {
            lagoonTex = assetManager.loadTexture("assets/Textures/Lagoon/lagoon_west.jpg");

            /**Must add a light to make the textured objects visible! */
            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
            sun.setColor(ColorRGBA.White);
            rootNode.addLight(sun);
        }



        /*Put up walls N-dimensionally. (Actually, they're just for show)*/
        makewalls();

        /*Manifest the physical forms of our beings.*/
        beings = new Being[NUM];
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
        time += 0.0002;
        for (Being b : beings) {
            b.updatePos(); // update position
            b.updateInfection(tpf);
        }
        if (AC("chart")) {
            updateChart(time); // update the chart
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

        Box mesh = new Box(halfWallDistance, 2, halfWallDistance);
        // Starry background

        if (AC("space")) {
            spaceMat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            Texture space = assetManager.loadTexture("assets/Pictures/Cosmic Winter Wonderland.jpg");
            spaceMat.setTexture("ColorMap", space);
        }

        for (int i = 0; i < 6; i++) {
            // Randomly-colored opaque walls
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            mat.setColor("Color", ColorRGBA.randomColor());

            Geometry g = new Geometry("wall" + i, mesh);
            if (AC("space")) {
                g.setMaterial(spaceMat);
            } else {
                g.setMaterial(mat);
            }
            g.setLocalTranslation(positions[i]);

            /*0 <= i < 2: xAngle = halfPi, yAngle = 0, zAngle = 0
            * 2 <= i < 4: xAngle = 0,      yAngle = 0, zAngle = halfPi
            * 4 <= i < 6: xAngle = 0,      yAngle = 0, zAngle = 0*/
            g.rotate((1-i/2+i/4)*halfPi, 0, (i/2-i/4-i/4)*halfPi); // integer division is for compactness
            rootNode.attachChild(g); // add the wall to the scene
        }
    }

    /**Class that integrates Particles into jME's logic.*/
    private class Being {
        private final PartN p;
        private final Geometry g;

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
            if (AC("texturedballs")) {
                mat.setTexture("ColorMap", lagoonTex);
            }
            g.setMaterial(mat);
        }

        private void updateInfection (double tpf) {
            char c = p.status;
            if (c == 'I') { // infected
                p.timer -= tpf;
                if (p.timer < 0) {
                    p.status = 'R';
                    p.setColor(ParticleN.BLUE);
                    infectedBeings--;
                    recoveredBeings++;
                    updateColor();
                }
            } else if (c == 'U') { // updateColor
                updateColor();
                p.status = 'I';
                p.timer -= tpf;
            }
        }
    }

    /**Class that makes a live chart using StdDraw. The problem is it will impact the FPS
        It will update every tick and move 0.0002 in the board.
    **/
    private static void updateChart (float x) {

        double x0 = 0;
        double y0 = 0;
        double y1 = (double)infectedBeings/(double)beings.length;
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.line(x, y0, x, y1);

        y0 = y1;
        y1 = y0+(double)susceptibleBeings/(double)beings.length;
        StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.line(x, y0, x, y1);

        y0 = y1;
        y1 = y0 + (double)recoveredBeings/(double)beings.length;
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.line(x, y0, x, y1);
        /**Could probably make it a lot cleaned.*/
    }
}
