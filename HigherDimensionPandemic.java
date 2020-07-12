import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

/**
 * Simulates a higher-dimension pandemic and displays a 3D (3 possibly random dimensions
 * out of N) view of it with jMonkeyEngine.
 * */
public class HigherDimensionPandemic extends SimpleApplication {
    /** TODO 2. gradual velocity increase up to a certain point (then it's instant)*/
    /**TODO 5. read from stdin*/
    /**TODO 6. attach the walls to a node and possibly translate it so it's
     *  in the middle of the simulation borders*/

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

    public static int DIM;
    public static int[] chosenDimensions = {0, 1, 2};
    public final float wallDistance = 100;
    private int BEINGCOUNT = 0;
    private Being ball;

    public static void main (String[] args){
        if (args.length == 1) {
            DIM = Integer.parseInt(args[0]);
            if (DIM < 3) {
                throw new IllegalArgumentException("Invalid number of dimensions.");
            }
        } else {
            System.err.println("Please provide exactly 1 parameter: the number of dimensions.");
            System.exit(1);
        }

        //pick3dimensions();

        HigherDimensionPandemic app = new HigherDimensionPandemic();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Title", "HigherDimensionPandemic"); as.put("VSync", true); as.put("Width", 1280); as.put("Height", 720);
        app.setShowSettings(false); // Uncomment to skip the initial settings popup
        app.setSettings(as);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //assetManager.registerLocator("Pictures", FileLocator.class);
        flyCam.setMoveSpeed(7); // Make the camera more bearable.
        Particle p = new Particle(3);
        ball = new Being(p);
        makewalls();
    }

    /**Main event loop.
     * @param tpf time per frame*/
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f curpos = ball.g.getLocalTranslation();
        ball.g.setLocalTranslation(curpos.add(0, 0, tpf));
    }

    /**Arbitrarily pick 3 dimensions to display.*/
    private static void pick3dimensions() {
        int cnt = 0;
        while (cnt < 3) {
            int r = StdRandom.uniform(DIM);
            boolean ok = true;
            for (int i = 0; i < cnt && ok; i++) {
                if (r == chosenDimensions[i]) {
                    ok = false;
                }
            }
            if (ok) {
                chosenDimensions[cnt] = r;
                cnt++;
            }
        }
    }

    /**Conjure up the (visual-only) walls.*/
    private void makewalls () {
        float halfPi = (float)Math.PI/2;
        Vector3f[] positions = {
                new Vector3f(0, 0, wallDistance),
                new Vector3f(0, 0, -wallDistance),
                new Vector3f(wallDistance, 0, 0),
                new Vector3f(-wallDistance, 0, 0),
                new Vector3f(0, wallDistance, 0),
                new Vector3f(0, -wallDistance, 0),
        };

        // Starry background
        Texture space = assetManager.loadTexture("assets/Pictures/Cosmic Winter Wonderland.jpg");
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); //default material
        mat.setTexture("ColorMap", space);
        Box mesh = new Box(200, 2, 200);

        for (int i = 0; i < 6; i++) {
            // Randomly-colored opaque walls
            /*mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            mat.setColor("Color", ColorRGBA.randomColor());*/

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

        private final Particle p;
        private final Geometry g;

        /**Create a Mesh and a Material according to the properties of p,
         * make a Geometry out of them and attach the Geometry to the rootNode.*/
        private Being (Particle p) {

            Sphere mesh = new Sphere(30, 30, (float)p.radius); // First 2 arguments control the quality of the sphere.
            String geometryName = "ball"+BEINGCOUNT;
            BEINGCOUNT++;
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md"); //default material
            float[] col = p.color();
            mat.setColor("Color", new ColorRGBA(col[0], col[1], col[2], col[3]));

            Geometry g = new Geometry(geometryName, mesh);
            g.setMaterial(mat);
            rootNode.attachChild(g); //add the Being to the scene
            this.p = p;
            this.g = g;
        }
    }
}
