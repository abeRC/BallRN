import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/** Sample 4 - how to trigger repeating actions from the main event loop.
 * In this example, you use the loop to make the player character
 * rotate continuously. */
public class HelloLoop extends SimpleApplication {

    public static void main(String[] args){
        HelloLoop app = new HelloLoop();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Width", 1280); as.put("Height", 720); as.put("Title", "My awesome Game"); as.put("VSync", true);
        app.setSettings(as);

        app.setShowSettings(false); //hide popup
        app.start();
    }

    protected Geometry player;
    protected Geometry doppel;
    protected Geometry rick;
    protected float factor = 1;
    protected double timesinceepilepsy = 0;

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(7);

        /** this blue box is our player character */
        Box b = new Box(1, 1, 1);
        player = new Geometry("blue cube", b);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        player.setMaterial(mat);
        rootNode.attachChild(player);

        /** rotating, pulsating, color-changing doppelganger*/
        doppel = new Geometry("doppelganger", b);
        doppel.setLocalTranslation(3,0,0);
        Material matd = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        matd.setColor("Color", ColorRGBA.Red);
        doppel.setMaterial(matd);
        rootNode.attachChild(doppel);

        /** rick*/
        rick = new Geometry("rick", new Box(0.28f, 0.28f, 0.28f));
        rick.setLocalTranslation(-4, 0.5f,-7);
        Material matr = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        matr.setColor("Color", ColorRGBA.Green);
        rick.setMaterial(matr);
        rootNode.attachChild(rick);
    }

    /** Use the main event loop to trigger repeating actions.
    * @param tpf time per frame*/
    @Override
    public void simpleUpdate(float tpf) {
        timesinceepilepsy += tpf;

        // make the player rotate:
        player.rotate(0, 2*tpf, 0);
        doppel.rotate(0, 4*tpf, 0);

        //pulsate
        float curscale = doppel.getLocalScale().x;
        if (curscale > 1.5) {
            factor = -1;
        } else if (curscale < 0.5) {
            factor = 1;
        }
        doppel.scale(1+ factor*1.7f*tpf);

        //change color every 0.17s
        if (timesinceepilepsy > 0.12) {
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.randomColor());
            doppel.setMaterial(mat);
            timesinceepilepsy = 0;
        }

        //roll
        rick.rotate(2*tpf, 0, 0);
        Vector3f curpos = rick.getLocalTranslation();
        rick.setLocalTranslation(curpos.add(0, 0, 2*tpf)); //careful not to overwrite anything!
    }


    /**
     * Everything in a game happens either during initialization, or during the update loop.
     * This means that these two methods grow very long over time.
     * Follow these two strategies to spread out init and update code over several modular Java classes:
         * Move code blocks from the simpleInitApp() method to AppStates.
         * Move code blocks from the simpleUpdate() method to Custom Controls.
     * Keep this in mind for later when your application grows.*/
}
