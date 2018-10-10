/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import javax.vecmath.Point3d;

/**
 *
 * @author Hugo
 */
public class OriHalfedge3D {
    OriFace3D face;
    OriHalfedge3D next;
    OriHalfedge3D prev;
    OriHalfedge3D pair;
    OriEdge3D edge;
    public Point3d v;

    public OriHalfedge3D(OriFace3D face, Point3d v) {
        this.face = face;
        this.v = v;
    }
    
    @Override
    public String toString (){
        String str = "he("+v.x+", "+v.y+")";
        return str;
    }
    
}
