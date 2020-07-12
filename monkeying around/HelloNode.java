import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;


/** Sample 2 - How to use nodes as handles to manipulate objects in the scene.
 * You can rotate, translate, and scale objects by manipulating their parent nodes.
 * The Root Node is special: Only what is attached to the Root Node appears in the scene.
 * Do read https://wiki.jmonkeyengine.org/docs/jme3/the_scene_graph.html .*/
public class HelloNode extends SimpleApplication {

    public static void main(String[] args){
        HelloNode app = new HelloNode();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Width", 1280); as.put("Height", 720); as.put("Title", "My awesome Game"); as.put("VSync", true);
        app.setSettings(as);

        app.setShowSettings(false); //hide popup
        app.start();
    }

    @Override
    public void simpleInitApp() {

        /** create a blue box at coordinates (1,-1,1) */
        Box box1 = new Box(1,1,1);
        Geometry blue = new Geometry("BoxBlue", box1);
        blue.setLocalTranslation(new Vector3f(1,-1,1));
        Material mat1 = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        blue.setMaterial(mat1);

        /** create a red box straight above the blue one at (1,3,1) */
        Box box2 = new Box(1,1,1);
        Geometry red = new Geometry("BoxRed", box2);
        red.setLocalTranslation(new Vector3f(1,3,1));
        Material mat2 = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        red.setMaterial(mat2);

        /** Create a pivot node at (0,0,0) and attach it to the root node */
        Node pivot = new Node("pivot");
        rootNode.attachChild(pivot); // put this node in the scene

        /** Attach the two boxes to the *pivot* node. (And transitively to the root node.) */
        pivot.attachChild(blue);
        //pivot.attachChild(red);

        /** Rotate the pivot node: Note that both boxes have rotated! */
        pivot.rotate(.4f,.4f,0f);
        pivot.move(0, 0, -9);

        /** Even though we only attach red here, what we moved and rotated was the pivot!*/
        pivot.attachChild(red);
        System.out.println("local:\nred: "+rootNode.getChild("BoxRed").getLocalTranslation());
        System.out.println("blue: "+blue.getLocalTranslation());
        System.out.println("world:\nred: "+red.getWorldTranslation()); //the pivot was rotated!
        System.out.println("blue: "+blue.getWorldTranslation());


        /** Add custom data to a Spatial.
         * One should note that only custom objects that implement Savable can be passed to setUserdata()
         * Also, remember to use wrapper classes for arrays.*/

        Integer[] a = {-1, 99, 77};
        pivot.setUserData("answer to life, the universe and everything", a);
        Integer[] bla;
        bla = (Integer[])(pivot.getUserData("answer to life, the universe and everything"));
    }
}
