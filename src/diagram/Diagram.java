/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import Simplification.CP;
import Simplification.Crease;
import Simplification.Node;
import java.awt.geom.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Vector2d;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import origamid.GeomUtilD;
import oripa.Doc;
import oripa.geom.*;

/**
 *
 * @author akitaya
 */
public class Diagram {

    Doc oriDoc;
    ArrayList<ArrayList<FaceArea>> faceLayers;
    public ArrayList<DiagramVertex> vertices;
    private AffineTransform transfFromNext;
    ArrayList<FaceArea> originalFaces;
    boolean hasOcclusionCycle;

//    Diagram(Doc oriDoc) {
//        System.out.println("entrei!");
//        this.oriDoc = oriDoc;
//        initVertices();
//        buildFaces();
//        System.out.println("sai!");
//    }
    Diagram(Doc oriDoc, Point2D.Double p0) {
        System.out.println("entrei!");
        this.oriDoc = oriDoc;
        initVertices(p0);
        buildFaces();
        System.out.println("sai!");
    }

    private void initVertices(Point2D.Double p0) {
        vertices = new ArrayList<>();
//        splitVertices = new ArrayList<>();
        for (OriVertex oriVertex : oriDoc.vertices) {
            DiagramVertex v = new DiagramVertex(oriVertex, p0);
            vertices.add(v);
        }
    }

    private void buildFaces() {
        faceLayers = new ArrayList<>();

        //contruct faceRegion objects
        originalFaces = new ArrayList<>();
        for (int i = 0; i < oriDoc.faces.size(); i++) {
            originalFaces.add(new FaceArea(oriDoc.faces.get(i), oriDoc.vertices, vertices, i));
        }

        //organize above-below relation
        for (FaceArea face : originalFaces) {
            setAboveBelow(face, originalFaces);
        }

//        //split faces
//        ArrayList<FaceArea> splittedFaces = new ArrayList<>();
////        System.out.println("number of faces "+faces.size());
//        for (FaceArea face : faces) {
//            System.out.println("face above "+face.oriFaceID + " "+face.above.size());
//            ArrayList<FaceArea> s = split(face);
//            splittedFaces.addAll(s);
//            System.out.println("splitting face "+s.size());
//        }
//        
//        System.out.println("comparison " +faces.size()+" "+splittedFaces.size());
//        for (FaceArea faceArea : splittedFaces) {
//            System.out.println("splitted faces "+faceArea.outline.size());
//        }
        //insert faces in the layers    
        ArrayList<FaceArea> faces = (ArrayList<FaceArea>) originalFaces.clone();
        int layerNumber = 0;
        while (!faces.isEmpty() && layerNumber < 50) {
            //mudar!!!!!!!!!!!!!!!!!!!!!!
            faceLayers.add(getLayer(faces, layerNumber));
            //commented begin
//            faceLayers.add(splitFace1(faces));
//            System.out.println("visible layer");
//            for (FaceArea faceArea : faceLayers.get(layerNumber)) {
//                faceArea.buildElement(CanvasSVG1.createNewDocument(0, 0));
//                faceArea.updateElement(false, 1);
//                Element path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//                //printElement(path);
//            }
//            System.out.println("hidden faces");
//            for (FaceArea faceArea : faces) {
//                faceArea.buildElement(CanvasSVG1.createNewDocument(0, 0));
//                faceArea.updateElement(false, 1);
//                Element path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//                //printElement(path);
//            }
//            System.exit(-1);
            // commented end
            layerNumber++;
        }
//        System.exit(42);

        //finish calculating the vertex z-index
        for (int i = 0; i < faceLayers.size(); i++) {
            ArrayList<FaceArea> layer = faceLayers.get(i);
            for (FaceArea face : layer) {
                for (DiagramVertex v : face.outline) {
                    v.height += i;
                    v.numberOfFaces++;
                }
            }
        }
        for (DiagramVertex v : vertices) {
            v.height /= v.numberOfFaces;
        }
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

    private void setAboveBelow(FaceArea face, ArrayList<FaceArea> faces) {
        for (FaceArea faceRegionComparator : faces) {
            if (oriDoc.overlapRelation[face.oriFaceID][faceRegionComparator.oriFaceID] == 2) {
                face.above.add(faceRegionComparator);
            } else if (oriDoc.overlapRelation[face.oriFaceID][faceRegionComparator.oriFaceID] == 1) {
                face.below.add(faceRegionComparator);
            } else if (oriDoc.overlapRelation[face.oriFaceID][faceRegionComparator.oriFaceID] == 9) {
                face.undefined.add(faceRegionComparator);
//                if (face.oriFaceID < faceRegionComparator.oriFaceID) {
//                    face.above.add(faceRegionComparator);
//                    faceRegionComparator.below.add(face);
//                    System.out.println(face.oriFaceID + "  " + faceRegionComparator.oriFaceID
//                            + " or " + oriDoc.overlapRelation[faceRegionComparator.oriFaceID][face.oriFaceID]);
//                }
            }
        }
    }
//

    private ArrayList<FaceArea> getLayer(ArrayList<FaceArea> faces, int layerNumber) {
//        System.out.println("getLayer " + layerNumber + " " + faces.size());
        ArrayList<FaceArea> layer = new ArrayList<>();
//        layer = splitFace1(faces);
        for (FaceArea f : faces) {
            boolean isTop = true;
            for (FaceArea faceAbove : f.above) {
                if (!faceAbove.sorted) {
                    isTop = false;
                    break;
                }
            }
            if (isTop) {
                layer.add(f);
//                f.face.z_order = layerNumber;
//                for (DiagramVertex v : f.outline) {
//                    v.height += layerNumber;
//                    v.numberOfFaces++;
//                }
            }
        }

        if (layer.isEmpty()) {
//            ArrayList<FaceRegion> occlusionCycle = getOcclusionCycle(faces);
            layer = splitFace1(faces);
            for (FaceArea f : layer) {
                f.sorted = true;
            }
            return layer;
//            return getLayer(faces, layerNumber);
        }
        for (FaceArea f : layer) {
            faces.remove(f);
            f.sorted = true;
//            System.out.println("removing " + f.oriFaceID);
        }
        return layer;
    }

//    
//    private ArrayList<FaceRegion> getOcclusionCycle(ArrayList<FaceRegion> faces) {
//        for (FaceRegion face : faces) {
//            if (face.above.size()==1) {
//                
//            }
//        }
//        return null;
//    }
//    private void splitFace(ArrayList<FaceArea> faces) {
//        System.out.println("entering split");
//        for (FaceArea face : faces) {
//            if (face.above.size() == 1) {
//                FaceArea newFaceArea = face.split(splitVertices);
//                if (newFaceArea != null) {
//                    faces.add(newFaceArea);
//                    return;
//                }
//            }
//        }
//        System.out.println("entering split2");
//        for (FaceArea face : faces) {
//            if (face.above.size() == 2) {
//                FaceArea newFaceArea = face.split(splitVertices);
//                if (newFaceArea != null) {
//                    faces.add(newFaceArea);
//                    return;
//                }
//            }
//        }
//        System.out.println("Error! - splitFace");
//        System.exit(13);
//    }
    private ArrayList<FaceArea> splitFace1(ArrayList<FaceArea> faces) {
        //System.out.println("split1 " + faces.size());
        ArrayList<FaceArea> visibleLayer = new ArrayList<>();
        ArrayList<FaceArea> hiddenFaces = new ArrayList<>();
        for (FaceArea face : faces) {
            Area visibleArea = (Area) face.area.clone();
            //calculate the visible area
            for (FaceArea faceAbove : face.above) {
                if (faceAbove.sorted) {
                    continue;
                }
                visibleArea.subtract(faceAbove.area);
//                System.out.println("subtraction " + face.oriFaceID + " " + faceAbove.oriFaceID);
            }
            ArrayList<FaceArea> subtractionFaces = buildFacesFromArea(visibleArea, face);
            Area invisibleArea = (Area) face.area.clone();
            invisibleArea.subtract(visibleArea);
            ArrayList<FaceArea> intersectionFaces = buildFacesFromArea(invisibleArea, face);
            for (FaceArea faceAbove : face.above) {
                faceAbove.below.remove(face);
                for (FaceArea intersectFace : intersectionFaces) {
                    Area intersectArea = (Area) intersectFace.area.clone();
                    intersectArea.intersect(faceAbove.area);
                    if (!intersectArea.isEmpty()) {
                        faceAbove.below.add(intersectFace);
                        intersectFace.above.add(faceAbove);
                    }
                }
            }
            for (FaceArea faceBelow : face.below) {
                faceBelow.above.remove(face);
                for (FaceArea intersectFace : intersectionFaces) {
                    Area intersectArea = (Area) intersectFace.area.clone();
                    intersectArea.intersect(faceBelow.area);
                    if (!intersectArea.isEmpty()) {
                        faceBelow.above.add(intersectFace);
                        intersectFace.below.add(faceBelow);
                    }
                }
                for (FaceArea intersectFace : subtractionFaces) {
                    Area intersectArea = (Area) intersectFace.area.clone();
                    intersectArea.intersect(faceBelow.area);
                    if (!intersectArea.isEmpty()) {
                        faceBelow.above.add(intersectFace);
                        intersectFace.below.add(faceBelow);
                    }
                }
            }

            visibleLayer.addAll(0, subtractionFaces);
            hiddenFaces.addAll(intersectionFaces);
        }
        faces.clear();
        faces.addAll(hiddenFaces);

        //System.out.println("visible layer " + visibleLayer.size());
        //System.out.println("hidden faces " + hiddenFaces.size());
//        for (FaceArea faceArea : visibleLayer) {
//            faceArea.buildElement(provisorio, false);
//            Element path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//            printElement(path);
////            System.out.println("above "+faceArea.above);
////            System.out.println("bel "+faceArea.below);
//        }


//        for (FaceArea faceArea : hiddenFaces) {
//            faceArea.buildElement(provisorio, false);
//            Element path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//            printElement(path);
//            System.out.println("above "+faceArea.above);
//            System.out.println("bel "+faceArea.below);
//        }
//        FaceArea faceArea = originalFaces.get(43);
//        faceArea.buildElement(provisorio, false);
//        Element path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//        printElement(path);
//        faceArea = originalFaces.get(45);
//        faceArea.buildElement(provisorio, false);
//        path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//        printElement(path);
//        faceArea = originalFaces.get(35);
//        faceArea.buildElement(provisorio, false);
//        path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//        printElement(path);
//        faceArea = originalFaces.get(36);
//        faceArea.buildElement(provisorio, false);
//        path = (Element) (faceArea.domElement.getElementsByTagName("path")).item(0);
//        printElement(path);

//        System.out.println(originalFaces.get(35).above.size());
//        System.out.println(originalFaces.get(35).below.size());
//        System.out.println(oriDoc.overlapRelation[48][45]);
//        System.exit(2);
        return visibleLayer;
    }

//    private ArrayList<FaceArea> split(FaceArea face) {
////        System.out.println("split");
////        for (DiagramVertex diagramVertex : face.outline) {
////            System.out.println("original " + diagramVertex);
////        }
//        if (face.above.isEmpty()) {
//            ArrayList<FaceArea> splitted = new ArrayList<>();
//            splitted.add(face);
//            return splitted;
//        }
//        FaceArea above = face.above.remove(0);
//
//        //intersection
//        Area intersection = (Area) face.area.clone();
//        intersection.intersect(above.area);
//        if (intersection.isEmpty()) {
//            //no intersection
//            return split(face);
//        }
//
//        Area subtraction = (Area) face.area.clone();
//        subtraction.subtract(above.area);
//        if (subtraction.isEmpty()) {
//            //all area of the face is located inside the above area
//            ArrayList<FaceArea> splitted = split(face);
//            for (FaceArea faceArea : splitted) {
//                faceArea.above.add(above);
//            }
//            return splitted;
//        }
//
//        PathIterator it = subtraction.getPathIterator(null);
//        double[] coords = new double[3];
//        ArrayList<DiagramVertex> subOutline = null;
//        ArrayList<FaceArea> subFaces = new ArrayList<>();
//
//        while (!it.isDone()) {
//            int segType = it.currentSegment(coords);
//            switch (segType) {
//                case PathIterator.SEG_MOVETO:
//                    subOutline = new ArrayList<>();
//                case PathIterator.SEG_LINETO:
//                    Point2D cpCoord = null;
//                    try {
//                        cpCoord = face.transform.inverseTransform(new Point2D.Double(coords[0], coords[1]), null);
//                    } catch (NoninvertibleTransformException ex) {
//                        ex.printStackTrace();
//                    }
//                    int size = subOutline.size();
//                    if (size != 0) {
//                        if (subOutline.get(size - 1).isSameCPCoord(cpCoord)) {
//                            break;
//                        }
//                    }
//                    subOutline.add(getVertexAt(cpCoord, face));
//                    break;
//                case PathIterator.SEG_CLOSE:
////                    System.out.println("close "+ subOutline.get(0)+" "+subOutline.get(subOutline.size() - 1));
//                    if (subOutline.get(0).isSameCPCoord(subOutline.get(subOutline.size() - 1).CPposition)) {
//                        subOutline.remove(subOutline.size() - 1);
////                        System.out.println("removeu");
//                    }
//                    if (subOutline.size() < 3) {
//                        subOutline = new ArrayList<>();
//                        //not a face
//                        break;
//                    }
//                    subFaces.add(new FaceArea(face, subOutline));
//                    break;
//                default:
//                    throw new UnsupportedOperationException("Not a polygon");
//            }
////            System.out.println("subtraction " + segType + " " + coords[0] + ", " + coords[1] + ", " + coords[2]);
//            it.next();
//        }
//
//        //above das subfaces eh o mesmo da face mae ate aqui
//        ArrayList<FaceArea> splitted = new ArrayList<>();
//        for (FaceArea subF : subFaces) {
////            ArrayList<FaceArea> subSplit = split(subF);    
//            splitted.addAll(split(subF));
////            for (FaceArea faceArea : subSplit) {
////                faceArea.above.add(above);
////                splitted.add(faceArea);
////            }
//        }
//
//        //colcular a intersecao
//        it = intersection.getPathIterator(null);
//        ArrayList<DiagramVertex> intersOutline = null;
//        FaceArea intersArea = null;
//        while (!it.isDone()) {
//            int segType = it.currentSegment(coords);
//            switch (segType) {
//                case PathIterator.SEG_MOVETO:
//                    intersOutline = new ArrayList<>();
//                case PathIterator.SEG_LINETO:
//                    Point2D cpCoord = null;
//                    try {
//                        cpCoord = face.transform.inverseTransform(new Point2D.Double(coords[0], coords[1]), null);
//                    } catch (NoninvertibleTransformException ex) {
//                        ex.printStackTrace();
//                    }
//                    int size = intersOutline.size();
//                    if (size != 0) {
//                        if (intersOutline.get(size - 1).isSameCPCoord(cpCoord)) {
//                            break;
//                        }
//                    }
//                    intersOutline.add(getVertexAt(cpCoord, face));
//                    break;
//                case PathIterator.SEG_CLOSE:
//                    if (intersOutline.get(0).isSameCPCoord(intersOutline.get(intersOutline.size() - 1).CPposition)) {
//                        intersOutline.remove(intersOutline.size() - 1);
//                    }
//                    if (intersOutline.size() < 3) {
//                        //no intersection
//                        return split(face);
////                        System.exit(2);
//                    }
//                    intersArea = new FaceArea(face, intersOutline);
//                    break;
//                default:
//                    throw new UnsupportedOperationException("Not a polygon");
//            }
////            System.out.println("intersection " + segType + " " + coords[0] + ", " + coords[1] + ", " + coords[2] + " " + PathIterator.SEG_CLOSE);
//            it.next();
//        }
//
//        ArrayList<FaceArea> intersSplit = split(intersArea);
//        for (FaceArea faceArea : intersSplit) {
//            faceArea.above.add(above);
//            splitted.add(faceArea);
//        }
//
//        return splitted;
////        throw new UnsupportedOperationException("Not yet implemented");
//    }
    private DiagramVertex getVertexAt(Point2D cpCoord, FaceArea face) {
        for (int i = 0; i < vertices.size(); i++) {
            DiagramVertex v = vertices.get(i);
            if (v.isSameCPCoord(cpCoord)) {
                return v;
            }
        }
        DiagramVertex boundaryV[] = new DiagramVertex[2];
        FaceArea originalFace = originalFaces.get(face.oriFaceID);
        for (int i = 0; i < originalFace.outline.size(); i++) {
            DiagramVertex oldOutlineV1 = originalFace.outline.get(i);
            DiagramVertex oldOutlineV2 = originalFace.outline.get((i + 1) % originalFace.outline.size());
            if (GeomUtilD.isOnSegment(cpCoord, oldOutlineV1.CPposition, oldOutlineV2.CPposition)) {
                boundaryV[0] = oldOutlineV1;
                boundaryV[1] = oldOutlineV2;
            }
        }
        DiagramVertex newV = new DiagramVertex(
                face.transform.transform(cpCoord, null), cpCoord, boundaryV);
        newV.p0 = vertices.get(0).p0;
        vertices.add(newV);
        return newV;
    }

    private ArrayList<FaceArea> buildFacesFromArea(Area visibleArea, FaceArea face) {
        PathIterator it = visibleArea.getPathIterator(null);
        double[] coords = new double[3];
        ArrayList<DiagramVertex> subOutline = null;
        ArrayList<FaceArea> subFaces = new ArrayList<>();

        while (!it.isDone()) {
            int segType = it.currentSegment(coords);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    subOutline = new ArrayList<>();
                case PathIterator.SEG_LINETO:
                    Point2D cpCoord = null;
                    try {
                        cpCoord = face.transform.inverseTransform(new Point2D.Double(coords[0], coords[1]), null);
                    } catch (NoninvertibleTransformException ex) {
                        ex.printStackTrace();
                    }
                    int size = subOutline.size();
                    if (size != 0) {
                        if (subOutline.get(size - 1).isSameCPCoord(cpCoord)) {
                            break;
                        }
                    }
                    subOutline.add(getVertexAt(cpCoord, face));
                    break;
                case PathIterator.SEG_CLOSE:
//                    System.out.println("close "+ subOutline.get(0)+" "+subOutline.get(subOutline.size() - 1));
                    if (subOutline.get(0).isSameCPCoord(subOutline.get(subOutline.size() - 1).CPposition)) {
                        subOutline.remove(subOutline.size() - 1);
//                        System.out.println("removeu");
                    }
                    if (subOutline.size() < 3) {
                        subOutline = new ArrayList<>();
                        //not a face
                        break;
                    }
                    subFaces.add(new FaceArea(face, subOutline));
                    break;
                default:
                    throw new UnsupportedOperationException("Not a polygon");
            }
//            System.out.println("subtraction " + segType + " " + coords[0] + ", " + coords[1] + ", " + coords[2]);
            it.next();
        }
        return subFaces;
    }

    private void printElement(Element path) {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        try {
            transformer.transform(new DOMSource(path),
                    new StreamResult(buffer));
        } catch (TransformerException ex) {
            Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(buffer.toString());
    }

    void buildSVGDoc(Document document) {
        for (ArrayList<FaceArea> layer : faceLayers) {
            for (FaceArea face : layer) {
                face.buildElement(document);
            }
        }
    }

    void addFaces(Element rotationGroup, boolean flipped) {
        if (!flipped) {
            for (ArrayList<FaceArea> layer : faceLayers) {
                for (FaceArea face : layer) {
                    face.updateElement(flipped, faceLayers.size());
                    rotationGroup.appendChild(face.domElement);
                }
            }
        } else {
            for (int i = faceLayers.size() - 1; i >= 0; i--) {
                ArrayList<FaceArea> layer = faceLayers.get(i);
                for (FaceArea face : layer) {
                    face.updateElement(flipped, faceLayers.size());
                    rotationGroup.appendChild(face.domElement);
                }
            }
        }
    }

    void addFoldLines(CP nextStep) {
        for (ArrayList<FaceArea> layers : faceLayers) {
            for (FaceArea faceArea : layers) {
                for (Crease crease : nextStep.getCreases()) {
                    Node[] n = crease.getNodes();
                    Vector2d p1 = new Vector2d(n[0].getX(), n[0].getY());
                    Vector2d p2 = new Vector2d(n[1].getX(), n[1].getY());
                    Vector2d midPoint = GeomUtilD.getMidPoint(p1, p2);
                    if (GeomUtilD.isContainPointFace(
                            oriDoc.faces.get(faceArea.oriFaceID), midPoint, GeomUtilD.EPS)) {
                        faceArea.addCreaseLine(p1, p2, crease.getType(), oriDoc);
                    }
                }
            }
        }
    }

    void addFoldArrow(Doc nextDoc) {
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
                    FaceArea faceArea = GeomUtilD.getFaceAreaContainingPoint(oriVertex.preP, faceLayers);
                    faceArea.addFoldArrow(oriVertex.preP, positionAfter, oriDoc);
//                    OriVertex from = GeomUtilD.getOriVertexAt(oriVertex.preP, faceArea.oriFace);
//                    OriVertex to = new OriVertex(positionAfter);
//                    to.tmpVec.x = from.tmpVec.x;
//                    faceArea.autoSymbols.add(new SymblVArrow(from, to));
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
