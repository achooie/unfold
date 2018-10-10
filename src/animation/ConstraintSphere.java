/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import Simplification.Crease;
import java.util.ArrayList;
import javax.vecmath.Vector2d;

/**
 *
 * @author akitaya
 */
public class ConstraintSphere {

    public ArrayList<OriEdge3D> edges;
    public ArrayList<ConstraintGeodesic> geodesics;
    public ArrayList<ConstraintTriangle> triangles;
    public ArrayList<ConstraintSphere> neighbors;
    boolean isEdge = false;
    boolean updated = false;
    int compAngleIndex;

    public ConstraintSphere(OriHalfedge3D he0) {
        edges = new ArrayList<>();
        geodesics = new ArrayList<>();
        neighbors = new ArrayList<>();
        OriHalfedge3D he = he0;
        double angle = 2 * Math.PI;
        OriEdge3D edge = he.edge;
        int signal = 1;
        edges.add(edge);
        Vector2d prevVec = new Vector2d(he.next.v.x, he.next.v.y);
        prevVec.sub(new Vector2d(he.v.x, he.v.y));
        do {
            he = he.prev.pair;
            if (he == null) {
                isEdge = true;
                return;
            }
            edge = he.edge;
            Vector2d vec1 = new Vector2d(he.next.v.x, he.next.v.y);
            vec1.sub(new Vector2d(he.v.x, he.v.y));
            if (edge.changes && angle == 2 * Math.PI) {
                edges.add(edge);
                geodesics.add(new ConstraintGeodesic(prevVec.angle(vec1)));
            } else {
                angle += signal * prevVec.angle(vec1);
                angle %= 2 * Math.PI;
                signal *= -1;
                if (edge.changes) {
                    edges.add(edge);
                    //last one
                    if (angle < 0) {
                        angle *= -1;
                        compAngleIndex = edges.size() - 2;
                    } else {
                        compAngleIndex = edges.size() - 1;
                    }
                    geodesics.add(new ConstraintGeodesic(angle));
                    angle = 2 * Math.PI;
                }
            }
            prevVec = vec1;
            //todo

//            if (!edge.changes) {
//                int signal = -1;
//                angle = prevVec.angle(vec1);
//                while (!edge.changes) {
//                    he = he.prev.pair;
//                    edge = he.edge;
//                    if (he == null) {
//                        isEdge = true;
//                        return;
//                    }
//                    prevVec = vec1;
//                    vec1 = new Vector2d(he.next.v.x, he.next.v.y);
//                    vec1.sub(new Vector2d(he.v.x, he.v.y));
//                    angle += prevVec.angle(vec1) * signal;
//                    signal *= -1;
//                }
//                if (angle < 0) {
//                    compAngleIndex = edges.size() - 1;
//                } else {
//                    compAngleIndex = edges.size();
//                }
//                angles.add(angle);
//                edges.add(edge);
//                he = he.prev.pair;
//                edge = he.edge;
//                if (he == null) {
//                    isEdge = true;
//                    return;
//                }
//            }
//            
        } while (!he.equals(he0));
        edges.remove(edges.size() - 1);
        compAngleIndex %= edges.size();
        System.out.println(this);
        buildTriangles();
        buildNeighborSpheres(he0);
    }

    private void buildNeighborSpheres(OriHalfedge3D he0) {
        for (int i = 1; i < edges.size(); i++) {
            OriEdge3D edge = edges.get(i);
            if (edge.spheres == null) {
                edge.spheres = new ConstraintSphere[2];
            }
            if (edge.he3d[0].v.epsilonEquals(he0.v, 1E-12)) {
                if (edge.spheres[0] == null) {
                    edge.spheres[0] = this;
                }
                if (edge.spheres[1] == null) {
                    edge.spheres[1] = new ConstraintSphere(edge.he3d[1]);
                    neighbors.add(edge.spheres[1]);
                }
            } else {
                if (edge.spheres[0] == null) {
                    edge.spheres[0] = new ConstraintSphere(edge.he3d[0]);
                    neighbors.add(edge.spheres[0]);
                }
                if (edge.spheres[1] == null) {
                    edge.spheres[1] = this;
                }
            }
        }
    }

    @Override
    public String toString() {
        ArrayList<Double> aux = new ArrayList<>();
        for (int i = 0; i < geodesics.size(); i++) {
            aux.add(geodesics.get(i).length * 180 / Math.PI);
        }
        return super.toString() + " " + edges + " " + aux + " " + isEdge + " " + compAngleIndex;
    }

    void solveDihedralAngles() {
        if (updated) {
            return;
        }
        System.out.println(edges.get(0).updated+"soulve dihedral " + this);
        if (Math.abs(edges.get(0).foldingAngle - edges.get(0).desiredAngle) > Crease.TOLERANCE) {
            if(!edges.get(0).updated){
                edges.get(0).foldingAngle += edges.get(0).desiredAngle / 64;
                edges.get(0).updated = true;
            }

            if (!isEdge) {
                if (edges.size() == 4) {
                    if (compAngleIndex == 0) {
                        triangles.get(0).dihedral[0] = edges.get(0).foldingAngle;
                    } else {
                        triangles.get(0).dihedral[0] = Math.PI - edges.get(0).foldingAngle;
                    }
                    triangles.get(0).cosineRule();
                    triangles.get(0).solveRemainingDihedral();
                    triangles.get(1).solveDihedral();
                    System.out.println("triangles " + triangles.get(0) + "\n" + triangles.get(1));
                    edges.get(1).foldingAngle = triangles.get(0).dihedral[1] + triangles.get(1).dihedral[0];
                    edges.get(2).foldingAngle = triangles.get(1).dihedral[1];
                    edges.get(3).foldingAngle = triangles.get(0).dihedral[2] + triangles.get(1).dihedral[2];                    
                    edges.get(1).updated = true;
                    edges.get(2).updated = true;
                    edges.get(3).updated = true;
                    for (int i = 1; i < edges.size(); i++) {
                        if (edges.get(i).beforeType == Crease.AUX && i != compAngleIndex) {
                            edges.get(i).foldingAngle = Math.PI - edges.get(i).foldingAngle;
                        }
                        edges.get(i).foldingAngle *= Math.signum(edges.get(i).desiredAngle);
                    }
//                    System.out.println("angle " + triangles.get(0) + "\n" + triangles.get(1));
//                    double geodesicD = Math.acos(Math.cos(geodesics.get(0)) * Math.cos(geodesics.get(3))
//                            + Math.sin(geodesics.get(0)) * Math.sin(geodesics.get(3)) * Math.cos((Math.PI - edges.get(0).foldingAngle)));
//                    edges.get(3).foldingAngle = 2 * Math.atan(2 * Math.sin(geodesics.get(0)) / (Math.tan((Math.PI - edges.get(0).foldingAngle) / 2) * Math.sin(geodesics.get(0) + geodesics.get(2))
//                            + Math.sin(geodesics.get(2) - geodesics.get(0)) / Math.tan((Math.PI - edges.get(0).foldingAngle) / 2)));
//                    double aux = (Math.cos(geodesicD) - Math.cos(geodesics.get(1)) * Math.cos(geodesics.get(2)))
//                            / (Math.sin(geodesics.get(1)) * Math.sin(geodesics.get(2)));
//                    edges.get(2).foldingAngle = Math.PI - Math.acos(aux > 1 ? 2 - aux : aux);
//                    edges.get(3).foldingAngle *= Math.signum(edges.get(3).desiredAngle);
//                    edges.get(2).foldingAngle *= Math.signum(edges.get(2).desiredAngle);
//                    System.out.println("angles " + geodesicD * 180 / Math.PI + " " + edges.get(0).foldingAngle * 180 / Math.PI + " " + edges.get(3).foldingAngle * 180 / Math.PI
//                            + " " + edges.get(2).foldingAngle * 180 / Math.PI + " " + Math.tan((Math.PI - edges.get(0).foldingAngle) / 2));
////                System.exit(compAngleIndex);
                } else {
                    for (int i = 1; i < edges.size(); i++) {
                        OriEdge3D edge = edges.get(i);
                        if (Math.abs(edge.foldingAngle - edge.desiredAngle) > Crease.TOLERANCE) {
                            edge.foldingAngle += edge.desiredAngle / 64;
                            edge.updated = true;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < edges.size(); i++) {
                OriEdge3D edge = edges.get(i);
                edge.foldingAngle = edge.desiredAngle;
            }
        }
        updated = true;
        if (neighbors != null) {
            for (int i = 0; i < neighbors.size(); i++) {
                neighbors.get(i).solveDihedralAngles();
            }
        }

    }

    private void buildTriangles() {
        if(edges.size()<4){
            return;
        }
        triangles = new ArrayList<>();
        ConstraintGeodesic aux = new ConstraintGeodesic(0);
        triangles.add(new ConstraintTriangle(geodesics.get(geodesics.size() - 1),
                geodesics.get(0), aux));
        for (int i = 2; i < edges.size() - 2; i++) {
            OriEdge3D oriEdge3D = edges.get(i);
            //to do 
            //mor than one degrees of freedom
        }
        triangles.add(new ConstraintTriangle(aux, geodesics.get(geodesics.size() - 3),
                geodesics.get(geodesics.size() - 2)));
        triangles.get(triangles.size() - 2).setNext(triangles.get(triangles.size() - 1));
    }
}
