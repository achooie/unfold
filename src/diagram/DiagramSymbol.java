/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import java.awt.Graphics2D;
import oripa.geom.OriVertex;

/**
 *
 * @author akitaya
 */
public interface DiagramSymbol {
    
    public void paint(Graphics2D g);
    
    public void setEndPoint(int x, int y);
    
    public double distanceFromPoint(double [] p);
    
    public void setWidth(int w);
    
    public int[] getCoordinates();
    
    public OriVertex[] getOriVertices();
    
    public DiagramVertex[] getDiagramVertices();

    public void paint(Graphics2D g2d, boolean flipped);
    
    public String toSVG(double scale, double localScale, double centerX, double centerY, boolean isFlipped);
}
