import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.math.ColorRGBA;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class HelloJME3 extends SimpleApplication {

    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1); // create cube shape
        Sphere s = new Sphere(50,50,2);
        Geometry geomB = new Geometry("Boxxxa", b);  // create cube geometry from the shape
        Geometry geomS = new Geometry("Spherical", s);

        Material matb = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        matb.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        Material matc = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        matc.setColor("Color", ColorRGBA.randomColor());

        geomB.setMaterial(matb);                   // set the cube's material
        geomS.setMaterial(matc);
        rootNode.attachChild(geomB);              // make the cube appear in the scene
        rootNode.attachChild(geomS);
    }
}
