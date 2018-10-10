/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import Simplification.Crease;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import origamid.GeomUtilD;
import oripa.geom.OriFace;
import oripa.geom.OriHalfedge;
import oripa.geom.OriVertex;

/**
 *
 * @author Hugo
 */
public class FaceRegion {

//    boolean isSorted;
    Area regionArea;
    ArrayList<DiagramSymbol> autoSymbols;
    public OriFace face;
    int faceID;
    Element domElement;
    ArrayList<FaceRegion> above;
//    ArrayList<FaceRegion> below;
    

    public FaceRegion(OriFace oriFace, int ID) {
        faceID = ID;
        face = oriFace;
        autoSymbols = new ArrayList<>();
        if (face == null) {
            return;
        }
//        regionArea = new Area(face.outline);
//        System.out.println(regionArea);
//        System.out.println(regionArea.getBounds());
//        System.out.println(regionArea.isPolygonal());
//        double[] coords = new double[2];
//        regionArea.getPathIterator(new AffineTransform()).currentSegment(coords);
//        System.out.println(coords[0]+" "+coords[1]);
//        System.out.println(face.halfedges.get(0).vertex);
//        System.exit(1);
//        isSorted = false;
    }

    Node buildElement(Document document, boolean flipped, int stackSize,
            Point2d po) {
        //Create Element group (SVG <g>)
        domElement = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "g");
        domElement.setAttributeNS(null, "id", "FaceRegion" + faceID);
        //Create an SVG <path>
        Element path = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
        domElement.appendChild(path);
        for (int i = 0; i < autoSymbols.size(); i++) {
            Element symbol = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
            domElement.appendChild(symbol);
        }
        updateElement(flipped, stackSize, po);
        return domElement;
    }

    void updateElement(boolean flipped, int stackSize,
            Point2d po) {
        //update the path element
        Element path = (Element) (domElement.getElementsByTagName("path")).item(0);
        String points = "M ";
        String style = "fill:url(#Gradient";
        if (!flipped) {
            if (face.faceFront) {
                style += "1";
            } else {
                style += "2";
            }
        } else {
            if (face.faceFront) {
                style += "2";
            } else {
                style += "1";
            }
        }
        style += ");stroke:#0000ff;stroke-width:2px;stroke-linecap:butt;stroke-linejoin:miter;";
        for (OriHalfedge he : face.halfedges) {
            Point2D.Double p = distort(po, he.vertex, flipped, stackSize);
            points += p.x + "," + p.y + " ";
        }
        points += "z";
        path.setAttribute("id", "pathFace" + faceID);
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
                if (diagramSymbol.getClass() == SymblMountain.class && !(face.faceFront ^ flipped)
                        || diagramSymbol.getClass() == SymblValley.class && (face.faceFront ^ flipped)) {
                    style += "stroke:#000000;stroke-dasharray:16,8;";
                } else {
                    style += "stroke:#640000;stroke-dasharray:16,8,4,8,4,8;";
                }
                OriVertex[] vertices = vertices = diagramSymbol.getOriVertices();
//            System.out.println("debug 2 " + diagramSymbol+" "+vertices[0].preP+ " "+vertices[0].tmpVec.x+" "+vertices[1].preP+ " "+vertices[1].tmpVec.x);
                Point2D.Double p = distort(po, vertices[0], flipped, stackSize);
                points += p.x + "," + p.y + " ";
                p = distort(po, vertices[1], flipped, stackSize);
                points += p.x + "," + p.y + " ";

            } else if (diagramSymbol.getClass() == SymblPArrow.class) {
                style = "stroke:none; stroke-width:4; marker-end:url(#PushArrow);";
                points = ((SymblPArrow) diagramSymbol).svgPoints(flipped);
            } else if (diagramSymbol.getClass() == SymblVArrow.class) {
                style = "fill:none;stroke:black;stroke-width:4;marker-end:url(#Arrow2Mend);";
                OriVertex[] vertices = diagramSymbol.getOriVertices();
//            System.out.println("debug 2 " + diagramSymbol+" "+vertices[0].preP+ " "+vertices[0].tmpVec.x+" "+vertices[1].preP+ " "+vertices[1].tmpVec.x);
                Point2D.Double p1 = distort(po, vertices[0], flipped, stackSize);
                Point2D.Double p2 = distort(po, vertices[1], flipped, stackSize);
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

    Point2D.Double distort(Point2d po, OriVertex v, boolean flipped, int stackSize) {
        Vector2d v1 = new Vector2d(po);
//        System.out.println("debug1" + v.preP+ " "+v.tmpVec.x);
        v1.scale(v.tmpVec.x / stackSize);
        v1.add(v.p);
        Point2D.Double p;
        if (flipped) {
            p = new Point2D.Double(-v1.x, v1.y);
        } else {
            p = new Point2D.Double(v1.x, v1.y);
        }
        return p;
    }

    void addCreaseLine(Vector2d p1, Vector2d p2, int type) {
        Vector2d midPoint = GeomUtilD.getMidPoint(p1, p2);
        if (GeomUtilD.isContainPointFace(face, midPoint, GeomUtilD.EPS)) {
            OriVertex v1 = GeomUtilD.getOriVertexAt(p1, face);
            OriVertex v2 = GeomUtilD.getOriVertexAt(p2, face);
            DiagramSymbol symbol;
            if (type == Crease.MOUNTAIN) {
                symbol = new SymblMountain(v1, v2);
            } else {
                symbol = new SymblValley(v1, v2);
            }
            autoSymbols.add(symbol);
        }
    }
//    void addPArrow(OriVertex v1, OriVertex v2) {
//        SymblPArrow symbol = new SymblPArrow(v1, v2, face);
//        autoSymbols.add(symbol);
//    }
}
