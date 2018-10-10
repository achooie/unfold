/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.0
 */
package diagram;

import java.awt.geom.Point2D;
import javax.vecmath.Vector2d;
import origamid.GeomUtilD;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class DiagramVertex extends Point2D.Double {

    double height;
    int numberOfFaces;
    Point2D.Double p0;
    Point2D.Double CPposition;
    boolean isAuxiliar;
    DiagramVertex[] boundaryVertices;

    DiagramVertex(OriVertex oriVertex, Point2D.Double po) {
        height = 0;
        this.x = oriVertex.p.x;
        this.y = oriVertex.p.y;
        p0 = po;
        CPposition = new Point2D.Double(oriVertex.preP.x, oriVertex.preP.y);
    }

    DiagramVertex(Point2D foldedCoord, Point2D cpCoord, DiagramVertex[] boudaryV) {
        height = 0;
        this.x = foldedCoord.getX();
        this.y = foldedCoord.getY();
        p0 = new Point2D.Double(15, 15);
        CPposition = (Double) cpCoord;
        isAuxiliar = true;
        boundaryVertices = boudaryV;
    }

    DiagramVertex(Vector2d foldedCoord, Vector2d cpCoord, DiagramVertex[] boudaryV) {
        height = 0;
        this.x = foldedCoord.getX();
        this.y = foldedCoord.getY();
        p0 = new Point2D.Double(15, 15);
        if (cpCoord != null) {
            CPposition = new Point2D.Double(cpCoord.x, cpCoord.y);
        }
        isAuxiliar = true;
        boundaryVertices = boudaryV;
    }

    boolean isSameCPCoord(Point2D cpCoord) {
//        System.out.println(CPposition.x - cpCoord.getX());
//        System.out.println(CPposition.y - cpCoord.getY());
        return Math.abs(CPposition.x - cpCoord.getX()) < 0.1
                && Math.abs(CPposition.y - cpCoord.getY()) < 0.1;
    }

    String getPositionString(int numberOfLayers, boolean isFlipped) {
        if (isFlipped) {
            return (-x - p0.x * height / numberOfLayers) + "," + (y + p0.y * height / numberOfLayers);
        }
        return (x + p0.x * height / numberOfLayers) + "," + (y + p0.y * height / numberOfLayers);
    }

    Point2D.Double getPosition(int numberOfLayers, boolean isFlipped) {
        if (isFlipped) {
            return new Point2D.Double((-x - p0.x * height / numberOfLayers), (y + p0.y * height / numberOfLayers));
        }
        return new Point2D.Double((x + p0.x * height / numberOfLayers), (y + p0.y * height / numberOfLayers));
    }
}
