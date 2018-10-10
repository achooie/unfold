/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.QuadCurve2D;
import javax.vecmath.Vector2d;
import origamid.GeomUtilD;
import oripa.geom.OriFace;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class SymblPArrow implements DiagramSymbol {

    int begin[], end[];
    OriVertex sv, mv, ev;
    static Stroke stroke = new BasicStroke(2);
    static Color color = Color.BLACK;
    final static String svgStart = "<path style=\"stroke:none; stroke-width:4; marker-end:url(#PushArrow);\" ";

    public SymblPArrow(int beginX, int beginY, int endX, int endY) {
        begin = new int[2];
        end = new int[2];
        begin[0] = beginX;
        begin[1] = beginY;
        end[0] = endX;
        end[1] = endY;
    }

    SymblPArrow(OriVertex sv, OriVertex ev, OriFace face) {
        this.sv = sv;
        this.ev = ev;
        Vector2d preP = new Vector2d(ev.preP);
        preP.add(sv.preP);
        preP.scale(0.5);
        mv = GeomUtilD.getOriVertexAt(preP, face);
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(stroke);
        QuadCurve2D q = new QuadCurve2D.Float();
        int dx = end[0] - begin[0];
        int dy = end[1] - begin[1];
        int[] outlineX = {end[0] - dx / 20, end[0] - dx / 2 + dy / 2,
            end[0] - dx / 2 + dy / 6, end[0] - 3 * dx / 2 + dy / 2,
            end[0] - dx, end[0] - 3 * dx / 2 - dy / 2,
            end[0] - dx / 2 - dy / 6, end[0] - dx / 2 - dy / 2};
        int[] outlineY = {end[1] - dy / 20, end[1] - dy / 2 - dx / 2,
            end[1] - dy / 2 - dx / 6, end[1] - 3 * dy / 2 - dx / 2,
            end[1] - dy, end[1] - 3 * dy / 2 + dx / 2,
            end[1] - dy / 2 + dx / 6, end[1] - dy / 2 + dx / 2};
        g2d.fillPolygon(outlineX, outlineY, 8);

        g2d.setColor(color);

        g2d.drawLine(end[0] - dx / 20, end[1] - dy / 20, end[0] - dx / 2 + dy / 2, end[1] - dy / 2 - dx / 2);
        g2d.drawLine(end[0] - dx / 2 + dy / 2, end[1] - dy / 2 - dx / 2, end[0] - dx / 2 + dy / 6, end[1] - dy / 2 - dx / 6);
        g2d.drawLine(end[0] - dx / 2 + dy / 6, end[1] - dy / 2 - dx / 6, end[0] - 3 * dx / 2 + dy / 2, end[1] - 3 * dy / 2 - dx / 2);
        g2d.drawLine(end[0] - 3 * dx / 2 + dy / 2, end[1] - 3 * dy / 2 - dx / 2, end[0] - dx, end[1] - dy);

        g2d.drawLine(end[0] - dx / 20, end[1] - dy / 20, end[0] - dx / 2 - dy / 2, end[1] - dy / 2 + dx / 2);
        g2d.drawLine(end[0] - dx / 2 - dy / 2, end[1] - dy / 2 + dx / 2, end[0] - dx / 2 - dy / 6, end[1] - dy / 2 + dx / 6);
        g2d.drawLine(end[0] - dx / 2 - dy / 6, end[1] - dy / 2 + dx / 6, end[0] - 3 * dx / 2 - dy / 2, end[1] - 3 * dy / 2 + dx / 2);
        g2d.drawLine(end[0] - 3 * dx / 2 - dy / 2, end[1] - 3 * dy / 2 + dx / 2, end[0] - dx, end[1] - dy);

    }

    @Override
    public void setWidth(int width) {
        stroke = new BasicStroke(width);
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
        String svg = svgStart + "d=\"";
        int dx = end[0] - begin[0];
        int dy = end[1] - begin[1];
        svg += "m " + (((begin[0] - 300) / localScale) * scale + centerX) + ",";
        svg += ((-(begin[1] - 300) / localScale) * scale + centerY) + " ";
        svg += ((dx / localScale) * scale) + ",";
        svg += ((-dy / localScale) * scale) + "\" />\n";
        return svg;
    }

    public String svgPoints(boolean isFlipped) {
        int dx = end[0] - begin[0];
        int dy = end[1] - begin[1];
        String svg = "m " + (isFlipped ? -begin[0] : begin[0]) + ",";
        svg += begin[1] + " ";
        svg += (isFlipped ? -dx : dx) + "," + dy;
        return svg;
    }

    @Override
    public OriVertex[] getOriVertices() {
        return new OriVertex[]{sv, mv, ev};
    }

    @Override
    public DiagramVertex[] getDiagramVertices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
