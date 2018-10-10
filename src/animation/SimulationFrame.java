/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

/**
 *
 * @author Hugo
 */
import GUI.FoldedModelScreen;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.media.j3d.*;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import translations.OBJExporter;

public class SimulationFrame extends JFrame implements ActionListener, KeyListener {

    private Button go = new Button("Go");
    private Timer timer;
    public Origami3D origami3d;
    Component[] choosedSequence;
//    Doc doc1 = new Doc(Constants.DEFAULT_PAPER_SIZE);
//    Doc doc2 = new Doc(Constants.DEFAULT_PAPER_SIZE);
//    Doc doc3 = new Doc(Constants.DEFAULT_PAPER_SIZE);
    BranchGroup scene;
    int counter;

    public SimulationFrame(Component[] choosedSequence) {
        super();
        this.choosedSequence = choosedSequence;
        setTitle("Animation Frame");
        setBounds(0, 0, 1000, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GraphicsConfiguration config =
                SimpleUniverse.getPreferredConfiguration();

        Canvas3D c = new Canvas3D(config);

        getContentPane().add(c, BorderLayout.CENTER);

        c.addKeyListener(this);

        timer = new Timer(100, this);

        //timer.start();

        Panel p = new Panel();

        p.add(go);

        getContentPane().add(p, BorderLayout.NORTH);

        go.addActionListener(this);

        go.addKeyListener(this);

        // Create a simple scene and attach it to the virtual universe

        scene = createSceneGraph();


        SimpleUniverse u = new SimpleUniverse(c);

        ViewingPlatform viewingPlatform = u.getViewingPlatform();        
        TransformGroup steerTG = viewingPlatform.getViewPlatformTransform();
        Transform3D t3d = new Transform3D();
        steerTG.getTransform(t3d);
// args are: viewer posn, where looking, up direction
        t3d.lookAt(new Point3d(0, 0, 10), new Point3d(0, 0, 0), new Vector3d(0, 1, 0));
        t3d.invert();
        steerTG.setTransform(t3d);
        
        OrbitBehavior orbit = new OrbitBehavior(c,
                OrbitBehavior.REVERSE_ALL);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        orbit.setSchedulingBounds(bounds);
        viewingPlatform.setViewPlatformBehavior(orbit);
        u.addBranchGraph(scene);
    }

    private BranchGroup createSceneGraph() {

        // Create the root of the branch graph

        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        
        Background background = new Background(new Color3f(1f, 1f, 1f));
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
        background.setApplicationBounds(sphere);
        objRoot.addChild(background);

//        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//
//        objRoot.addChild(objTrans);

        // Create a simple shape leaf node, add it to the scene graph.




        origami3d = new Origami3D();

//        doc1.addLine(new OriLine(200, 200, -200, -200, Crease.VALLEY));
////        doc1.addLine(new OriLine(0, 200, 0, -200, Crease.MOUNTAIN));
////        doc1.addLine(new OriLine(0, 200, 200, -200, Crease.MOUNTAIN));
//        doc1.buildOrigami3(true);
        origami3d.buildFaces(null, ((FoldedModelScreen)choosedSequence[0]).getDoc());
        origami3d.addFaces2Scene(objRoot);

        BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        Color3f light1Color = new Color3f(1.0f, 1.0f, 1.0f);

        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);

        Vector3f light1Direction2 = new Vector3f(-4.0f, 7.0f, 12.0f);

        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        DirectionalLight light2 = new DirectionalLight(light1Color, light1Direction2);

        light1.setInfluencingBounds(bounds);
        light2.setInfluencingBounds(bounds);

        objRoot.addChild(light1);
        objRoot.addChild(light2);

        // Set up the ambient light

        Color3f ambientColor = new Color3f(0.9f, 0.9f, 0.9f);

        AmbientLight ambientLightNode = new AmbientLight(ambientColor);

        ambientLightNode.setInfluencingBounds(bounds);

        objRoot.addChild(ambientLightNode);

        return objRoot;

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Invoked when a key has been released.
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //Invoked when a key has been typed.
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // start timer when button is pressed

        if (e.getSource() == go) {

            if (!timer.isRunning()) {

                timer.start();

            } else {
                timer.stop();
            }

        } else {

            origami3d.updateTransform();
            System.out.println("counter " + counter++);
            if (counter >= 65 * choosedSequence.length) {
                timer.stop();
                return;
            }
            if (counter % 65==0) {
                int step = counter/65;
                origami3d.removeFacesFromScene(scene);
//                doc2.addLine(new OriLine(0, 200, 0, -200, Crease.VALLEY));
//                doc2.addLine(new OriLine(-200, 0, 0, 0, Crease.VALLEY));
//                doc2.addLine(new OriLine(0, 0, 200, 0, Crease.MOUNTAIN));
////                doc2.addLine(new OriLine(0, 200, 200, -200, Crease.VALLEY));
//                doc2.buildOrigami3(true);
                origami3d.buildFaces(((FoldedModelScreen)choosedSequence[step-1]).getDoc(), 
                        ((FoldedModelScreen)choosedSequence[step]).getDoc());
                origami3d.addFaces2Scene(scene);
                timer.stop();
            }
            try {
                OBJExporter.export(origami3d, "animation/"+counter+".obj");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

//    public static void main(String[] args) {
//
//        System.out.println("Program Started");
//
//        SimulationFrame bb = new SimulationFrame();
//
//        bb.addKeyListener(bb);
//        bb.setVisible(true);
//
//    }
}
