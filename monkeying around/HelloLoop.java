import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);

        /** this blue box is our player character */
        Box b = new Box(1, 1, 1);
        player = new Geometry("blue cube", b);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        player.setMaterial(mat);
        rootNode.attachChild(player);
    }

    /** Use the main event loop to trigger repeating actions.
    * @param tpf time per frame*/
    @Override
    public void simpleUpdate(float tpf) {
        // make the player rotate:
        player.rotate(0, 2*tpf, 0);
    }


    /**
     * Everything in a game happens either during initialization, or during the update loop.
     * This means that these two methods grow very long over time.
     * Follow these two strategies to spread out init and update code over several modular Java classes:
         * Move code blocks from the simpleInitApp() method to AppStates.
         * Move code blocks from the simpleUpdate() method to Custom Controls.
     * Keep this in mind for later when your application grows.*/
}
