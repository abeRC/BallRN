import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/** Sample 3 - how to load an OBJ model, and OgreXML model,
 * a material/texture, or text.
 * You need the assets from jme3-testdata.jar.*/
public class HelloAssets extends SimpleApplication {

    public static void main(String[] args) {
        HelloAssets app = new HelloAssets();
        AppSettings as = new AppSettings(true); //default settings
        as.put("Width", 1280); as.put("Height", 720); as.put("Title", "My awesome Game"); as.put("VSync", false);
        app.setSettings(as);

        app.setShowSettings(false); //hide popup
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(20);
        /**You can use the inherited assetManager object directly,
         * or use the accessor app.getAssetManager().
         * If you have a model without materials,
         * you have to add a default material to make it visible.*/
        Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material mat_default = new Material(
        /**Common indicates that the asset is "somewhere" in the jME3 JAR*/
                assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teapot.setMaterial(mat_default);
        rootNode.attachChild(teapot);

        // Create a wall with a simple texture from test_data
        Box box = new Box(2.5f,2.5f,1.0f);
        Spatial wall = new Geometry("Box", box );
        Material mat_brick = new Material(
                assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap",
                assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        wall.setMaterial(mat_brick);
        wall.setLocalTranslation(2.0f,-2.5f,0.0f);
        rootNode.attachChild(wall);

        // Display a line of text with a default font
        //guiNode.detachAllChildren(); //Clear existing text in the guiNode by detaching all its children.
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Hello World");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);

        // Load a model from test_data (OgreXML + material + texture)
        Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninja.scale(0.05f, 0.05f, 0.05f);
        ninja.rotate(0.0f, -3.0f, 0.0f);
        ninja.setLocalTranslation(0.0f, -5.0f, -2.0f);
        rootNode.attachChild(ninja);

        Spatial elephant = assetManager.loadModel("Models/Elephant/Elephant.mesh.xml");
        elephant.scale(0.2f, 0.2f, 0.2f);
        elephant.setLocalTranslation(0, -5, -20);
        elephant.rotate(0, (float)Math.PI, 0);
        rootNode.attachChild(elephant);

        // You must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight(); //Directional lights have no specific position in the scene.
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

        /**Load assets from a custom path.*/
        /*assetManager.registerLocator("https://storage.googleapis.com/"
                + "google-code-archive-downloads/v2/code.google.com/"
                + "jmonkeyengine/wildhouse.zip", HttpZipLocator.class);
        Spatial scene = assetManager.loadModel("main.scene");
        scene.setLocalTranslation(50,3,0);
        rootNode.attachChild(scene);*/


        /**Note:

         JME3 can convert and load: Ogre XML models + materials; Ogre DotScenes;
         Wavefront OBJ + MTL models; .gltf files.
         The loadModel() method loads these original file formats
         when you run your code directly from the SDK.
         If you however build the executables using the default build script,
         then the original model files (XML, OBJ, etc) are not included.
         For QA test builds and for the final release build, you use .j3o files exclusively.
         You should use the SDK to convert the files to the .j30 format.*/
    }
}
