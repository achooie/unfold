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
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public class SymblVArrow implements DiagramSymbol {

    int begin[], end[];
    OriVertex sv, ev;
    DiagramVertex sv1, ev1;
    static Stroke stroke = new BasicStroke(2);
    static Color color = Color.black;
    final static String svgStart = "<path style=\"fill:none;stroke:black;stroke-width:4;"
            + "marker-end:url(#Arrow2Mend);\" ";

    public SymblVArrow(int beginX, int beginY, int endX, int endY) {
        begin = new int[2];
        end = new int[2];
        begin[0] = beginX;
        begin[1] = beginY;
        end[0] = endX;
        end[1] = endY;
    }
    
    SymblVArrow(OriVertex sv, OriVertex ev) {
        this.sv = sv;
        this.ev = ev;
    }
    SymblVArrow(DiagramVertex sv, DiagramVertex ev) {
        this.sv1 = sv;
        this.ev1 = ev;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(stroke);
        QuadCurve2D q = new QuadCurve2D.Float();
        int dx = end[0] - begin[0];
        int dy = end[1] - begin[1];
        int ctrlx = begin[0] + dx / 2 - dy / 8;
        int ctrly = begin[1] + dy / 2 + dx / 8;
// draw QuadCurve2D.Float with set coordinates
        q.setCurve(begin[0] + dx / 20, begin[1] + dy / 20, ctrlx, ctrly,
                end[0] - dx / 20, end[1] - dy / 20);
        g2d.draw(q);
        g2d.drawLine(end[0] - dx / 20, end[1] - dy / 20, end[0] - dx / 9 + dy / 35, end[1] - dy / 9 - dx / 35);
        g2d.drawLine(end[0] - dx / 20, end[1] - dy / 20, (int) (end[0] - dx / 10.5 - dy / 20), (int) (end[1] - dy / 10.5 + dx / 20));
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
        int ctrlx = dx / 2 - dy / 8;
        int ctrly = dy / 2 + dx / 8;
        svg += "M" + (((begin[0] + dx / 20 - 300) / localScale) * scale + centerX) + ",";
        svg += ((-(begin[1] + dy / 20 - 300) / localScale) * scale + centerY) + " ";
        svg += "q" + (ctrlx / localScale * scale) + ",";
        svg += (-ctrly / localScale * scale) + " ";
        svg += (((.9 * dx) / localScale) * scale) + ",";
        svg += ((-(.9 * dy) / localScale) * scale) + "\" />\n";
        return svg;
    }

    @Override
    public OriVertex[] getOriVertices() {
        return new OriVertex[]{sv, ev};
    }

    @Override
    public DiagramVertex[] getDiagramVertices() {
        return new DiagramVertex[] {sv1, ev1};
    }
}
