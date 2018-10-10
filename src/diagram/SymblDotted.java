/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.vecmath.Vector2d;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class SymblDotted implements DiagramSymbol {

    int begin[], end[];
    static Stroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL, 0, new float[]{2, 5}, 0);
    static Color color = Color.RED;
    final static String svgStart = " <line style=\"stroke:black; stroke-width:4; stroke-dasharray:5, 10;\" ";

    public SymblDotted(int beginX, int beginY, int endX, int endY) {
        begin = new int[2];
        end = new int[2];
        begin[0] = beginX;
        begin[1] = beginY;
        end[0] = endX;
        end[1] = endY;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(stroke);
        g2d.drawLine(begin[0], begin[1], end[0], end[1]);
    }

    @Override
    public void setWidth(int width) {
        stroke = new BasicStroke(width, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL, 0, new float[]{2, 5}, 0);
    }

    @Override
    public void setEndPoint(int x, int y) {
        end[0] = x;
        end[1] = y;
    }

    @Override
    public double distanceFromPoint(double[] p) {
        return oripa.geom.GeomUtil.DistancePointToSegment(new Vector2d(p),
                new Vector2d(begin[0], begin[1]), new Vector2d(end[0], end[1]));
    }

    @Override
    public int[] getCoordinates() {
        int[] points = new int[4];
        points[0] = begin[0];
        points[1] = begin[1];
        points[2] = end[0];
        points[3] = end[1];
        return points;
    }

    @Override
    public void paint(Graphics2D g2d, boolean flipped) {
        paint(g2d);
    }

    @Override
    public String toSVG(double scale, double localScale, double centerX, double centerY, boolean isFlipped) {
        String svg = svgStart + "x1=\"";
        svg += "" + (((begin[0] - 300) / localScale) * scale + centerX) + "\"";
        svg += " y1=\"";
        svg += "" + ((-((begin[1] - 300) / localScale)) * scale + centerY) + "\"";
        svg += " x2=\"";
        svg += "" + (((end[0] - 300) / localScale) * scale + centerX) + "\"";
        svg += " y2=\"";
        svg += "" + ((-((end[1] - 300) / localScale)) * scale + centerY) + "\" />\n";
        return svg;
    }

    @Override
    public OriVertex[] getOriVertices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DiagramVertex[] getDiagramVertices() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
