/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import Simplification.CP;
import Simplification.Crease;
import Simplification.ExecutionNode;
import Simplification.Node;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import origamid.GeomUtilD;
import oripa.Doc;
import oripa.geom.*;

/**
 *
 * @author akitaya
 */
public class CanvasSVG extends JSVGCanvas
        implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    public oripa.Doc oriDoc;
    ArrayList<ArrayList<FaceRegion>> faceLayers;
    Diagram diagram;
    Document document;
    Element scaleGroup;
    Element rotationGroup;
    public ExecutionNode step;
    boolean isMainView;
    public boolean isFlipped;
    public String description;
    Point2d po;
    int width, height;
    double rotateAngle;
    private Point2D preMousePoint;
    double scale;
    public Vector2d modelCenter;
    private AffineTransform transfFromNext;
    final static String header1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
            + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\"\n"
            + "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\"\n"
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\""
            + " width=\"";
    final static String header2 = "px\" height=\"";
    final static String header3 = "px\" viewBox=\"0 0 ";
    final static String header4 = "\">\n<defs id=\"defs1\">\n"
            + "<marker orient=\"auto\" refY=\"0\" \n"
            + "refX=\"0\" id=\"Arrow2Mend\" style=\"overflow:visible;\">\n"
            + "<path id=\"path1\" style=\"fill-rule:evenodd;stroke-width:0.62500000;stroke-linejoin:round;\" \n"
            + "d=\"M 8.7185878,4.0337352 L -2.2072895,0.016013256 L 8.7185884,-4.0017078 C 6.9730900,-1.6296469 6.9831476,1.6157441 8.7185878,4.0337352 z \" \n"
            + "transform=\"scale(0.6) rotate(180) translate(0,0)\" /></marker>\n"
            + "<marker \norient  = \"auto\" \nrefY  = \"0.0\" \nrefX  = \"0.0\" \nid  = \"PushArrow\" \nstyle  = \"overflow:visible;\"> \n"
            + "<path \nstyle  = \"fill:#FFFFFF;stroke:#000000;stroke-width:1;stroke-linecap:butt;\n"
            + "stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" \n"
            + "d  = \"M -0.67959949,0 -7.617562,-7.6952 -7.661756,-2.7836 "
            + "c -4.098649,0.067 -7.910611,-0.8767 -11.23229,-2.6497 1.008667,2.8513 2.137562,4.1384 3.12203,5.1274 "
            + "-1.023414,1.095 -2.12133,2.4352 -3.169334,5.1129 4.077885,-2.0537 7.229117,-2.6113 11.1876805,-2.6433 l 0.047303,4.7679 z\" \n"
            + "id  = \"path2\" /></marker>\n"
            + "</defs>\n"
            + " <linearGradient id=\"Gradient1\" x1=\"20%\" y1=\"0%\" x2=\"80%\" y2=\"100%\">\n"
            + " <stop offset=\"5%\" stop-color=\"#DDEEFF\" />\n"
            + " <stop offset=\"95%\" stop-color=\"#7788FF\" />\n"
            + " </linearGradient>\n"
            + " <linearGradient id=\"Gradient2\" x1=\"20%\" y1=\"0%\" x2=\"80%\" y2=\"100%\">\n"
            + " <stop offset=\"5%\" stop-color=\"#FFFFEE\" />\n"
            + " <stop offset=\"95%\" stop-color=\"#DDDDDD\" />\n"
            + " </linearGradient>\n"
            + "</svg>";

    public CanvasSVG(int width, int height) {
        super();
        setDisableInteractions(true);
        this.width = width;
        this.height = height;
        scale = width / 600.0;
    }

    public CanvasSVG(ExecutionNode execNode, boolean isMainView, String description,
            boolean isFlipped, int width, int height) throws Exception {
        super();
        this.width = width;
        this.height = height;
        this.step = execNode;
        this.isMainView = isMainView;
        this.description = description;
        this.isFlipped = isFlipped;
        setDisableInteractions(true);
        po = new Point2d(15, 15);
        oriDoc = GeomUtilD.foldCP(step.stepCp);

        buildFaces();
        
        scale = width / 600.0;
//
//        setUpDocument();
    }

    private void setUpDocument() {
        document = createNewDocument(width, height);

        setDocumentState(ALWAYS_DYNAMIC);
        buildDocument();
        setSVGDocument((SVGDocument) document);
//        System.out.println("setup " + this.hashCode());
    }

    public void copy(CanvasSVG screen) {
        this.oriDoc = screen.oriDoc;
        this.step = screen.step;
        this.isFlipped = screen.isFlipped;
        this.description = screen.description;
        this.faceLayers = screen.faceLayers;
        this.po = screen.po;
        this.rotateAngle = screen.rotateAngle;
        setUpDocument();
    }

    public CanvasSVG copy(int width, int height) {
        CanvasSVG c = new CanvasSVG(width, height);
        c.oriDoc = oriDoc;
        c.step = step;
        c.isFlipped = isFlipped;
        c.description = description;
        c.faceLayers = faceLayers;
        c.po = po;
        c.rotateAngle = rotateAngle;
//        c.setUpDocument();
        c.importGroup(rotationGroup);
        return c;
    }

    public void flipFaces(boolean selected) {
        isFlipped = selected;
        setUpDocument();
    }

    public void clearSymbols() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void deleteSelectedSymbol() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void unSelected() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void redrawOrigami() {
        setUpDocument();
    }

    public void setDiagramNotation(CanvasSVG mainView) {
        CP nextStep = mainView.step.stepCp;
        //crease lines 

        for (ArrayList<FaceRegion> layers : faceLayers) {
            for (FaceRegion faceRegion : layers) {
                for (Crease crease : nextStep.getCreases()) {
                    Node[] n = crease.getNodes();
                    faceRegion.addCreaseLine(new Vector2d(n[0].getX(), n[0].getY()),
                            new Vector2d(n[1].getX(), n[1].getY()), crease.getType());
                }
            }
        }

        //push arrows

        if (step.maneuvers.get(0) != null) {
            addPushSymbols(nextStep);
        }

        //Fold arrows

        addFoldArrow(mainView.oriDoc);

    }

    private void buildFaces() {
        faceLayers = new ArrayList<>();
        //contruct faceRegion objects
        ArrayList<FaceRegion> faces = new ArrayList<>();
        for (int i = 0; i < oriDoc.faces.size(); i++) {
            faces.add(new FaceRegion(oriDoc.faces.get(i), i));
        }

        //insert faces in the layers    
        int layerNumber = 0;
        while (!faces.isEmpty()) {
            faceLayers.add(getLayer(faces, layerNumber));
            layerNumber++;
        }

        //finish calculating the vertex z-index
        for (OriVertex oriVertex : oriDoc.vertices) {
            oriVertex.tmpVec.scale(1.0 / oriVertex.tmpInt);
        }

        //solve Colisions with 
        for (OriEdge oriEdge : oriDoc.edges) {
            if (oriEdge.type == Crease.PAPER_EDGE) {
                continue;
            }
            int face1 = oriEdge.left.face.z_order;
            int face2 = oriEdge.right.face.z_order;
            for (OriFace f : oriDoc.faces) {
                if (GeomUtilD.isBetween(f.z_order, face1, face2)
                        && GeomUtilD.isFaceOverlap(f, oriEdge.left.face, GeomUtilD.EPS)) {
                    for (OriHalfedge he : f.halfedges) {
                        if (GeomUtilD.DistancePointToLine(he.vertex.p,
                                new Line(oriEdge.ev.p,
                                new Vector2d(oriEdge.sv.p.x - oriEdge.ev.p.x, oriEdge.sv.p.y - oriEdge.ev.p.y)))
                                < GeomUtilD.EPS) {
                            double dx = oriEdge.sv.p.x - oriEdge.ev.p.x;
                            if (dx != 0) {
                                he.vertex.tmpVec.x = oriEdge.ev.tmpVec.x + (he.vertex.p.x - oriEdge.ev.p.x)
                                        * (oriEdge.sv.tmpVec.x - oriEdge.ev.tmpVec.x) / dx;
                            } else {
                                he.vertex.tmpVec.x = oriEdge.ev.tmpVec.x + (he.vertex.p.y - oriEdge.ev.p.y)
                                        * (oriEdge.sv.tmpVec.x - oriEdge.ev.tmpVec.x) / (oriEdge.sv.p.y - oriEdge.ev.p.y);
                            }
                        }
                    }
                }
            }
        }

    }

    private ArrayList<FaceRegion> getLayer(ArrayList<FaceRegion> faces, int layerNumber) {
        ArrayList<FaceRegion> layer = new ArrayList<>();
        for (FaceRegion f : faces) {
            if (isTopFace(f, faces)) {
                layer.add(f);
                f.face.z_order = layerNumber;
                for (OriHalfedge he : f.face.halfedges) {
                    he.vertex.tmpInt++;
                    he.vertex.tmpVec.add(new Point2d(layerNumber, 0));
//                    System.out.println("     tempint.." + he.vertex.tmpInt + " " + he.vertex.tmpVec);
                }
            }
        }
        if (layer.isEmpty()) {
//            ArrayList<FaceRegion> occlusionCycle = getOcclusionCycle(faces);
//            splitFace(occlusionCycle);
//            return getLayer(faces, layerNumber);
        }
        for (FaceRegion f : layer) {
            faces.remove(f);
        }
        return layer;
    }

    boolean isTopFace(FaceRegion faceRegionCandidate, ArrayList<FaceRegion> faces) {
        boolean isTopFace = true;
        for (FaceRegion faceRegionComparator : faces) {
            if (faceRegionCandidate.faceID == faceRegionComparator.faceID) {
                continue;
            }
            if (oriDoc.overlapRelation[faceRegionCandidate.faceID][faceRegionComparator.faceID] == 2) {
//                Area intersection = (Area) faceRegionCandidate.regionArea.clone();
//                intersection.intersect(faceRegionComparator.regionArea);
//                if (!intersection.isEmpty()) {
                if (GeomUtilD.isFaceOverlap(faceRegionCandidate.face,
                        faceRegionComparator.face, GeomUtilD.EPS)) {
                    isTopFace = false;
                    break;
                }
            }
        }
        return isTopFace;
    }

    ArrayList<FaceRegion> above(FaceRegion faceRegionCandidate, ArrayList<FaceRegion> faces) {
        ArrayList<FaceRegion> above = new ArrayList<>();
        for (FaceRegion faceRegionComparator : faces) {
            if (faceRegionCandidate.faceID == faceRegionComparator.faceID) {
                continue;
            }
            if (oriDoc.overlapRelation[faceRegionCandidate.faceID][faceRegionComparator.faceID] == 2) {
//                Area intersection = (Area) faceRegionCandidate.regionArea.clone();
//                intersection.intersect(faceRegionComparator.regionArea);
//                if (!intersection.isEmpty()) {
                if (GeomUtilD.isFaceOverlap(faceRegionCandidate.face,
                        faceRegionComparator.face, GeomUtilD.EPS)) {
                    above.add(faceRegionComparator);
                }
            }
        }
        return above;
    }

    //REDO!!!!!!!!!!!!!!!!!!!!!!!
    private void splitFace(ArrayList<FaceRegion> faces) {
//        JOptionPane.showMessageDialog(
//                    null, "Split Face", "Not correctly implemented",
//                    JOptionPane.ERROR_MESSAGE);
        System.out.println("face split Not correctly implemented");

//        ArrayList<FaceRegion> occlusionCycle = getOcclusionCycle(faces);
        
        
        for (FaceRegion faceRegion : faces) {
            ArrayList<FaceRegion> above = above(faceRegion, faces);
            if (above.size() == 1) {
                faces.remove(faceRegion);
                return;
            }
        }
    }

//    private ArrayList<FaceRegion> getOcclusionCycle(ArrayList<FaceRegion> faces) {
//        ArrayList<FaceRegion> cycle = new ArrayList<>();
//        for (FaceRegion face : faces) {
//            if (oriDoc.overlapRelation[faceRegionCandidate.faceID][faceRegionComparator.faceID] == 2) {
////                Area intersection = (Area) faceRegionCandidate.regionArea.clone();
////                intersection.intersect(faceRegionComparator.regionArea);
////                if (!intersection.isEmpty()) {
//                if (GeomUtilD.isFaceOverlap(faceRegionCandidate.face,
//                        faceRegionComparator.face, GeomUtilD.EPS)) {
//                    above.add(faceRegionComparator);
//                }
//            }
//        }
//        return above;
//    }

    private void calculateModelCenter() {
        //calculate the center of the origami
        Vector2d maxV = new Vector2d(-Double.MAX_VALUE, -Double.MAX_VALUE);
        Vector2d minV = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
        modelCenter = new Vector2d();
        for (OriFace face : oriDoc.faces) {
            for (OriHalfedge he : face.halfedges) {
                maxV.x = Math.max(maxV.x, he.vertex.p.x);
                maxV.y = Math.max(maxV.y, he.vertex.p.y);
                minV.x = Math.min(minV.x, he.vertex.p.x);
                minV.y = Math.min(minV.y, he.vertex.p.y);
            }
        }
        modelCenter.x = (maxV.x + minV.x) / 2;
        modelCenter.y = (maxV.y + minV.y) / 2;
    }

    private void buildDocument() {

        calculateModelCenter();
        // Get the root element (the 'svg' element).
//        Element svgRoot = document.getDocumentElement();

        rotationGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
        scaleGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");

        if (!isFlipped) {
            for (int i = faceLayers.size() - 1; i >= 0; i--) {
                ArrayList<FaceRegion> layer = faceLayers.get(i);
                for (FaceRegion faceRegion : layer) {
                    updateTransform();
                    rotationGroup.appendChild(faceRegion.buildElement(document,
                            isFlipped, faceLayers.size(), po));
                }
            }
        } else {
            for (ArrayList<FaceRegion> layer : faceLayers) {
                for (FaceRegion faceRegion : layer) {
                    updateTransform();
                    rotationGroup.appendChild(faceRegion.buildElement(document,
                            isFlipped, faceLayers.size(), po));
//                    System.out.println("transform " + transform);
                }
            }
        }

        scaleGroup.appendChild(rotationGroup);
        document.getDocumentElement().appendChild(scaleGroup);

    }

    private void updateDocument() {
        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

            @Override
            public void run() {
                for (ArrayList<FaceRegion> layer : faceLayers) {
                    for (FaceRegion faceRegion : layer) {
                        updateTransform();
                        faceRegion.updateElement(isFlipped, faceLayers.size(), po);
                    }
                }
            }
        });
//        System.out.println(updateManager + " " + updateManager.getUpdateTracker().hasChanged() + " " + document);
    }

    private void updateTransform() {

        String transfScaleGroup = "translate(" + width / 2 + "," + height / 2 + ") scale(" + scale + ") ";
        String transfRotationGroup = "rotate(" + rotateAngle + ",";
        if (!isFlipped) {
            transfScaleGroup += "translate(" + -modelCenter.x + "," + -modelCenter.y + ")";
            transfRotationGroup += modelCenter.x + "," + modelCenter.y + ")";
        } else {
            transfScaleGroup += "translate(" + modelCenter.x + "," + -modelCenter.y + ")";
            transfRotationGroup += -modelCenter.x + "," + modelCenter.y + ")";
        }
        scaleGroup.setAttributeNS(null, "transform", transfScaleGroup);
        rotationGroup.setAttributeNS(null, "transform", transfRotationGroup);
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                po.add(new Vector2d(0, -1));
                setUpDocument();
                break;
            case KeyEvent.VK_DOWN:
                po.add(new Vector2d(0, 1));
                setUpDocument();
                break;
            case KeyEvent.VK_LEFT:
                po.add(new Vector2d(-1, 0));
                setUpDocument();
                break;
            case KeyEvent.VK_RIGHT:
                po.add(new Vector2d(1, 0));
                setUpDocument();
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            scale *= 1.2;
            setUpDocument();
        } else {
            scale *= 0.8;
            setUpDocument();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        preMousePoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
            rotateAngle += ((double) e.getX() - preMousePoint.getX());
            preMousePoint = e.getPoint();
            updateDocument();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public static Document createNewDocument(int width, int height) {
// Create a new document
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory fa = new SAXSVGDocumentFactory(parser);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        String header = header1 + width + header2 + height
                + header3 + width + " " + height + header4;
        StringReader sr = new StringReader(header);
        try {
            return fa.createSVGDocument(svgNS, sr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Element getGroup() {
        return rotationGroup;
    }

    private void importGroup(Element rotationGroup) {
        document = createNewDocument(width, height);
        setDocumentState(ALWAYS_DYNAMIC);

        scaleGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
        this.rotationGroup = (Element) document.importNode(rotationGroup, true);
        scaleGroup.appendChild(this.rotationGroup);
        document.getDocumentElement().appendChild(scaleGroup);
        calculateModelCenter();
        updateTransform();

        setSVGDocument((SVGDocument) document);
    }

    void addPushSymbols(CP stepCp) {
        //creases of the current step (simplifyed)
        ArrayList<Crease> creases1 = step.stepCp.getCreases();
        //creases of the next step(after folding)
        ArrayList<Crease> creases2 = stepCp.getCreases();
        for (int i = 0; i < creases1.size(); i++) {
            Crease c1 = creases1.get(i);
            if (c1.getType() == Crease.AUX || c1.getType() == Crease.PAPER_EDGE) {
                continue;
            }
            boolean havePair = false;
            for (int j = 0; j < creases2.size(); j++) {
                Crease c2 = creases2.get(j);
                if (c1.equals(c2)) {
                    havePair = true;
                    break;
                }
            }
            if (havePair) {
                continue;
            }
            Vector2d s1 = new Vector2d(c1.getNodes()[0].getX(), c1.getNodes()[0].getY());
            Vector2d e1 = new Vector2d(c1.getNodes()[1].getX(), c1.getNodes()[1].getY());
            for (int j = 0; j < creases2.size(); j++) {
                Crease c2 = creases2.get(j);
                if (c2.getType() != c1.getType()) {
                    continue;
                }
                Vector2d s2 = new Vector2d(c2.getNodes()[0].getX(), c2.getNodes()[0].getY());
                Vector2d e2 = new Vector2d(c2.getNodes()[1].getX(), c2.getNodes()[1].getY());
                if (GeomUtilD.isLineSegmentContained(s2, e2, s1, e1)) {
                    if (GeomUtilD.isSamePoint(s1, s2)) {
                        s1.set(e2);
//                        break;
                    } else if (GeomUtilD.isSamePoint(s1, e2)) {
                        s1.set(s2);
//                        break;
                    } else if (GeomUtilD.isSamePoint(e1, s2)) {
                        e1.set(e2);
//                        break;
                    } else if (GeomUtilD.isSamePoint(e1, e2)) {
                        e1.set(s2);
//                        break;
                    }
                }
            }
            if (!GeomUtilD.isSamePoint(s1, e1)) {
                addPushArrowSymbol(new OriEdge(new OriVertex(s1), new OriVertex(e1), c1.getType()));
            }
        }
    }

    private void addPushArrowSymbol(OriEdge oriEdge) {

        Vector2d sub = new Vector2d(oriEdge.ev.preP);
        sub.sub(oriEdge.sv.preP);
        sub.scale(.1);
        Vector2d mid = GeomUtilD.getMidPoint(oriEdge.sv.preP, oriEdge.ev.preP);
        mid.x -= sub.y;
        mid.y += sub.x;
        FaceRegion faceReg = GeomUtilD.getFaceRegionContainingPoint(mid, faceLayers);
        System.out.println("positions debug ");
        Vector2d point1 = GeomUtilD.getPositionFolded(oriEdge.ev, oriDoc);
        Vector2d point2 = GeomUtilD.getPositionFolded(oriEdge.sv, oriDoc);
        Vector2d point3 = GeomUtilD.getPositionFolded(new OriVertex(mid), oriDoc);
        mid = GeomUtilD.getMidPoint(point1, point2);
        sub = new Vector2d(point3);
        sub.sub(mid);
        sub.scale(-2);
        sub.add(mid);
        faceReg.autoSymbols.add(new SymblPArrow((int) sub.x, (int) sub.y,
                (int) mid.x, (int) mid.y));
    }

    public Vector2d translateCoordinates(Vector2d vec) {
        if (oriDoc.foldedBBoxLT != null) {
            Vector2d docCenter = new Vector2d((oriDoc.foldedBBoxLT.x + oriDoc.foldedBBoxRB.x) / 2,
                    (oriDoc.foldedBBoxLT.y + oriDoc.foldedBBoxRB.y) / 2);
            double localScale = Math.min(
                    600 / (oriDoc.foldedBBoxRB.x - oriDoc.foldedBBoxLT.x),
                    600 / (oriDoc.foldedBBoxRB.y - oriDoc.foldedBBoxLT.y)) * 0.95;
            localScale *= .8;
            double x1 = (vec.x - docCenter.x) * localScale + 300;
            double y1 = -(vec.y - docCenter.y) * localScale + 300;
            return new Vector2d(x1, y1);
        }
        return null;
    }

    private void addFoldArrow(Doc nextDoc) {
        if (transfFromNext == null) {
            setTransformation(nextDoc);
        }
        for (OriVertex oriVertex : nextDoc.vertices) {
            Vector2d pointBefore = GeomUtilD.getPositionFolded(oriVertex, oriDoc);
            if (pointBefore != null) {
                Point2D.Double pointAfter = new Point2D.Double(oriVertex.p.x, oriVertex.p.y);
                transfFromNext.transform(pointAfter, pointAfter);
                if (Math.abs(pointBefore.x - pointAfter.x) > Crease.TOLERANCE
                        || Math.abs(pointBefore.y - pointAfter.y) > Crease.TOLERANCE) {
                    Vector2d positionAfter = new Vector2d(pointAfter.x, pointAfter.y);

                    FaceRegion faceReg = GeomUtilD.getFaceRegionContainingPoint(oriVertex.preP, faceLayers);
                    OriVertex from = GeomUtilD.getOriVertexAt(oriVertex.preP, faceReg.face);
                    OriVertex to = new OriVertex(positionAfter);
                    to.tmpVec.x = from.tmpVec.x;
                    faceReg.autoSymbols.add(new SymblVArrow(from, to));
                }

            }
        }
    }

    private void setTransformation(Doc prevDoc) {
        transfFromNext = new AffineTransform();
        for (int i = 0; i < prevDoc.faces.size(); i++) {
            OriFace oriFace1 = prevDoc.faces.get(i);
            OriHalfedge he1 = oriFace1.halfedges.get(0);
            for (OriFace oriFace2 : oriDoc.faces) {
                if (GeomUtilD.isSameFace(oriFace1, oriFace2)) {
                    OriHalfedge he2 = null;
                    for (OriHalfedge oriHalfedge : oriFace2.halfedges) {
                        if (GeomUtilD.isSamePoint(he1.vertex.preP, oriHalfedge.vertex.preP)) {
                            he2 = oriHalfedge;
                            break;
                        }
                    }
                    boolean fliped = oriFace1.faceFront != oriFace2.faceFront;
                    Vector2d originBefore = he1.vertex.p;
                    Vector2d vecBefore = new Vector2d();
                    vecBefore.sub(he1.next.vertex.p, originBefore);
                    Vector2d originAfter = he2.vertex.p;
                    Vector2d vecAfter = new Vector2d();
                    vecAfter.sub(he2.next.vertex.p, originAfter);
                    transfFromNext.translate(originAfter.x, originAfter.y);
                    if (fliped) {
                        double angle = (vecAfter.y > 0 ? -1 : +1) * vecAfter.angle(new Vector2d(1, 0));
                        transfFromNext.rotate(-angle);
                        transfFromNext.concatenate(AffineTransform.getScaleInstance(1, -1));
                        transfFromNext.rotate(angle);
                    }
                    double angleAfter = Math.atan2(vecAfter.y, vecAfter.x);
                    double angleBefore = Math.atan2(vecBefore.y, vecBefore.x);
                    transfFromNext.rotate(angleAfter - angleBefore);
                    transfFromNext.translate(-originBefore.x, -originBefore.y);
                    return;
                }
            }
        }
        for (OriEdge oriEdge1 : prevDoc.edges) {
            for (OriEdge oriEdge2 : oriDoc.edges) {
                if (GeomUtilD.isSamePoint(oriEdge1.sv.preP, oriEdge2.sv.preP)) {
                    if (GeomUtilD.isSamePoint(oriEdge1.ev.preP, oriEdge2.ev.preP)) {
                        Vector2d originBefore = oriEdge1.sv.p;
                        Vector2d vecBefore = new Vector2d();
                        vecBefore.sub(oriEdge1.ev.p, originBefore);
                        Vector2d originAfter = oriEdge2.sv.p;
                        Vector2d vecAfter = new Vector2d();
                        vecAfter.sub(oriEdge2.ev.p, originAfter);

                        transfFromNext.translate(originAfter.x, originAfter.y);

                        double angleAfter = Math.atan2(vecAfter.y, vecAfter.x);
                        double angleBefore = Math.atan2(vecBefore.y, vecBefore.x);
                        transfFromNext.rotate(angleAfter - angleBefore);
                        transfFromNext.translate(-originBefore.x, -originBefore.y);
                        return;
                    }
                }
            }
        }
    }
}
