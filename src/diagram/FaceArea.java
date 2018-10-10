/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import Simplification.Crease;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.vecmath.Vector2d;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import origamid.AffineTransformBuilder;
import origamid.GeomUtilD;
import oripa.Doc;
import oripa.geom.OriFace;
import oripa.geom.OriHalfedge;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class FaceArea {

    Area area;
    ArrayList<DiagramVertex> outline;
    ArrayList<FaceArea> above;
    ArrayList<FaceArea> below;
    ArrayList<FaceArea> undefined;
    ArrayList<DiagramSymbol> autoSymbols;
    AffineTransform transform;
    Element domElement;
    int oriFaceID;
    public OriFace oriFace;
    boolean faceFront;
    boolean sorted;

    FaceArea(OriFace oriFace, ArrayList<OriVertex> oriVertices, ArrayList<DiagramVertex> vertices, int iD) {
        area = new Area(oriFace.outline);
        oriFaceID = iD;
        this.oriFace = oriFace;
        outline = new ArrayList<>();
        sorted = false;
        faceFront = oriFace.faceFront;
        above = new ArrayList<>();
        below = new ArrayList<>();
        undefined = new ArrayList<>();
        autoSymbols = new ArrayList<>();
        for (OriHalfedge he : oriFace.halfedges) {
            outline.add(vertices.get(oriVertices.indexOf(he.vertex)));
        }
        transform = (new AffineTransformBuilder(outline.get(0).CPposition, outline.get(1).CPposition, outline.get(2).CPposition,
                outline.get(0), outline.get(1), outline.get(2))).getTransformation();
//        System.out.println("outline "+outline.get(0)+ " "+ outline.get(0).CPposition+" "+transform.transform(outline.get(0).CPposition, null));
//        System.out.println("outline "+outline.get(1)+ " "+ outline.get(1).CPposition+" "+transform.transform(outline.get(1).CPposition, null));
//        System.out.println("outline "+outline.get(2)+ " "+ outline.get(2).CPposition+" "+transform.transform(outline.get(2).CPposition, null));
    }

    FaceArea(FaceArea face, ArrayList<DiagramVertex> subOutline) {
        oriFaceID = face.oriFaceID;
        oriFace = face.oriFace;
        outline = subOutline;
        sorted = false;
        faceFront = face.faceFront;
        transform = face.transform;
        above = new ArrayList<>();
        below = new ArrayList<>();
        undefined = new ArrayList<>();
        autoSymbols = new ArrayList<>();
//        above.addAll(face.above);
        Polygon2D outlinePolygon2D = new Polygon2D();
//        System.out.println("new face "+outline.size());
        for (DiagramVertex diagramVertex : subOutline) {
//            System.out.println("new face "+diagramVertex);
            outlinePolygon2D.addPoint(diagramVertex);
        }
        area = new Area(outlinePolygon2D);
    }

    // Splits the face in two. 
    //This object contains the intersection with the immediately above face
    //the returned object contains the subtraction.
    FaceArea split(ArrayList<DiagramVertex> splitVertices) {
        FaceArea immediateAbove = above.get(0);
        Area newArea = (Area) area.clone();
        newArea.subtract(immediateAbove.area);
        System.out.println("splitting " + oriFaceID + " " + immediateAbove.oriFaceID);
        if (!newArea.isSingular()) {
            return null;
        }
        PathIterator oldPath = area.getPathIterator(null);
        while (!oldPath.isDone()) {
            double[] coordinates = new double[2];
            oldPath.currentSegment(coordinates);
            System.out.println("old outline " + coordinates[0] + ", " + coordinates[1]);
//            for (DiagramVertex prevV : outline) {
//                
//            }
            oldPath.next();
        }
        oldPath = immediateAbove.area.getPathIterator(null);
        while (!oldPath.isDone()) {
            double[] coordinates = new double[2];
            oldPath.currentSegment(coordinates);
            System.out.println("sub outline " + coordinates[0] + ", " + coordinates[1]);
//            for (DiagramVertex prevV : outline) {
//                
//            }
            oldPath.next();
        }
        PathIterator newPath = newArea.getPathIterator(null);
        System.out.println("new area " + newArea.isEmpty());
        for (int i = 0; !newPath.isDone(); i++) {
            double[] coordinates = new double[2];
            newPath.currentSegment(coordinates);
            System.out.println("new outline " + coordinates[0] + ", " + coordinates[1]);
//            for (DiagramVertex prevV : outline) {
//                
//            }
            newPath.next();
        }
        return null;
    }

    public FaceArea copy() {
        FaceArea copy = new FaceArea(null, null, outline, oriFaceID);
        copy.above = new ArrayList<>();
        copy.below = new ArrayList<>();
        copy.undefined = new ArrayList<>();
//        ...
        return copy;
    }

    Node buildElement(Document document) {
        //Create Element group (SVG <g>)
        domElement = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "g");
        domElement.setAttributeNS(null, "id", "FaceArea" + oriFaceID + this);
        //Create an SVG <path>
        Element path = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
        domElement.appendChild(path);
        for (int i = 0; i < autoSymbols.size(); i++) {
            Element symbol = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
            domElement.appendChild(symbol);
        }
//        updateElement(flipped);
        return domElement;
    }

    void updateElement(boolean flipped, int numberOfLayers) {
        //update the path element
        Element path = (Element) (domElement.getElementsByTagName("path")).item(0);
        String points = "M ";
        String style = "fill:url(#Gradient";
        if (flipped) {
            if (faceFront) {
                style += "1";
            } else {
                style += "2";
            }
        } else {
            if (faceFront) {
                style += "2";
            } else {
                style += "1";
            }
        }
        style += ");stroke:#0000ff;stroke-width:2px;stroke-linecap:butt;stroke-linejoin:miter;";
        for (DiagramVertex v : outline) {
            points += v.getPositionString(numberOfLayers, flipped) + " ";
        }
//        for (OriHalfedge he : face.halfedges) {
//            Point2D.Double p = distort(po, he.vertex, flipped, stackSize);
//            points += p.x + "," + p.y + " ";
//        }
        points += "z";
        path.setAttribute("id", "pathFace" + oriFaceID + this);
        path.setAttribute("d", points);
        path.setAttribute("style", style);
//        if (faceID == 0) {
//            System.out.println("update " + path + " " + path.getAttribute("d"));
//        }

        //update the diagram symbols
        for (int i = 0; i < autoSymbols.size(); i++) {
            DiagramSymbol diagramSymbol = autoSymbols.get(i);
            Element pathSymbol = (Element) (domElement.getElementsByTagName("path")).item(i + 1);
            if (diagramSymbol.getClass() == SymblMountain.class || diagramSymbol.getClass() == SymblValley.class) {
                points = "M ";
                style = "fill:none;stroke-width:4;";
                if (diagramSymbol.getClass() == SymblMountain.class && (faceFront ^ flipped)
                        || diagramSymbol.getClass() == SymblValley.class && !(faceFront ^ flipped)) {
                    style += "stroke:#000000;stroke-dasharray:16,8;";
                } else {
                    style += "stroke:#640000;stroke-dasharray:16,8,4,8,4,8;";
                }
                DiagramVertex[] vertices = diagramSymbol.getDiagramVertices();

                points += vertices[0].getPositionString(numberOfLayers, flipped) + " ";
                points += vertices[1].getPositionString(numberOfLayers, flipped) + " ";

            } //            else if (diagramSymbol.getClass() == SymblPArrow.class) {
            //                style = "stroke:none; stroke-width:4; marker-end:url(#PushArrow);";
            //                points = ((SymblPArrow) diagramSymbol).svgPoints(flipped);
            //            } 
            else if (diagramSymbol.getClass() == SymblVArrow.class) {
                style = "fill:none;stroke:black;stroke-width:4;marker-end:url(#Arrow2Mend);";
                DiagramVertex[] vertices = diagramSymbol.getDiagramVertices();
//            System.out.println("debug 2 " + diagramSymbol+" "+vertices[0].preP+ " "+vertices[0].tmpVec.x+" "+vertices[1].preP+ " "+vertices[1].tmpVec.x);
                Point2D.Double p1 = vertices[0].getPosition(numberOfLayers, flipped);
                Point2D.Double p2 = vertices[1].getPosition(numberOfLayers, flipped);
                int dx = (int) (p2.x - p1.x);
                int dy = (int) (p2.y - p1.y);
                int ctrlx = dx / 2 - dy / 8;
                int ctrly = dy / 2 + dx / 8;
                points = "M" + p1.x + ",";
                points += p1.y + " ";
                points += "q" + (ctrlx) + ",";
                points += ctrly + " ";
                points += (.9 * dx) + ",";
                points += (.9 * dy);
            }

            pathSymbol.setAttribute("id", "pathSymbol" + (i + 1));
            pathSymbol.setAttribute("d", points);
            pathSymbol.setAttribute("style", style);
        }
    }

    void addCreaseLine(Vector2d p1, Vector2d p2, int type, Doc oriDoc) {
        DiagramVertex v1 = getVertexAt(p1, oriDoc);
        DiagramVertex v2 = getVertexAt(p2, oriDoc);
        DiagramSymbol symbol;
        if (type == Crease.MOUNTAIN) {
            symbol = new SymblMountain(v1, v2);
        } else {
            symbol = new SymblValley(v1, v2);
        }
        autoSymbols.add(symbol);
    }

    private DiagramVertex getVertexAt(Vector2d p, Doc oriDoc) {
        for (int i = 0; i < outline.size(); i++) {
            DiagramVertex vertex = outline.get(i);
            DiagramVertex nextVertex = outline.get((i + 1) % outline.size());
            Vector2d cpPosition = new Vector2d(vertex.CPposition.x, vertex.CPposition.y);
            Vector2d nextVcpPosition = new Vector2d(nextVertex.CPposition.x, nextVertex.CPposition.y);
            if (GeomUtilD.DistanceSquared(p, cpPosition) < GeomUtilD.EPS) {
                return vertex;
            }
            if (GeomUtilD.isOnSegment(p, cpPosition, nextVcpPosition)) {
                double d1 = GeomUtilD.Distance(cpPosition, nextVcpPosition);
                double d2 = GeomUtilD.Distance(cpPosition, p);
                //new heigth should be a poundering mean of heights
                double newHeight = (d2 * nextVertex.height + (d1 - d2) * vertex.height) / d1;
                //so should be the folded position 
                Vector2d newPosition = new Vector2d(
                        (d2 * nextVertex.x + (d1 - d2) * vertex.x) / d1,
                        (d2 * nextVertex.y + (d1 - d2) * vertex.y) / d1);
                DiagramVertex boundary[] = {vertex, nextVertex};
                DiagramVertex newV = new DiagramVertex(newPosition, p, boundary);
                newV.p0 = vertex.p0;
                newV.height = newHeight;
                return newV;
            }
        }
        DiagramVertex newV = new DiagramVertex(GeomUtilD.getPositionFolded(p, oriDoc.faces.get(oriFaceID)), p, null);
        newV.height = 0;
        return newV;
    }

    void addFoldArrow(Vector2d preP, Vector2d positionAfter, Doc oriDoc) {
        DiagramVertex v1 = getVertexAt(preP, oriDoc);
        DiagramVertex v2 = new DiagramVertex(positionAfter, null, null);
        DiagramSymbol symbol = new SymblVArrow(v1, v2);
        autoSymbols.add(symbol);
    }
}
