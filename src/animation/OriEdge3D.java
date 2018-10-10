/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import Simplification.Crease;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author akitaya
 */
public class OriEdge3D {

    public OriHalfedge3D[] he3d = null;
    public boolean changes;
    public boolean updated;
    public int type = 0;
    public int beforeType = 0;
    double foldingAngle;
    double desiredAngle;
    public ConstraintSphere[] spheres = null;

    public OriEdge3D(boolean changes, int type, int beforeType, double desiredAngle) {
        this.changes = changes;
        this.type = type;
        this.beforeType = beforeType;
        he3d = new OriHalfedge3D[2];
        this.desiredAngle = desiredAngle;
    }

    public void setHalfEdge(OriHalfedge3D he) {
        if (he3d[0] == null) {
            he3d[0] = he;
            he.edge = this;
        } else {
            he3d[1] = he;
            he.edge = this;
            he.pair = he3d[0];
            he3d[0].pair = he;
//            System.out.println("sethalfedge "+he3d[0].pair+" "+he3d[1].pair);
        }
    }

    public void updateTransform(Transform3D initial) {
        if (initial == null) {
            if (changes) {
                if ((he3d[0].face.isFixed && !he3d[1].face.isFixed)
                        || (he3d[1].face.isFixed && !he3d[0].face.isFixed)) {
                    OriFace3D movable = he3d[0].face.isFixed ? he3d[1].face : he3d[0].face;
                    if (movable.updated) {
                        int movIndex = movable.equals(he3d[0].face) ? 0 : 1;
                        Point3d movedPoint1 = new Point3d(he3d[movIndex].v);
                        Point3d fixedPoint1 = new Point3d(movedPoint1);
                        Point3d movedPoint2 = new Point3d(he3d[(movIndex + 1) % 2].v);
                        Point3d fixedPoint2 = new Point3d(movedPoint2);
                        Transform3D movTransf = new Transform3D(movable.transf);
                        Transform3D fixTransf = new Transform3D(he3d[(movIndex + 1) % 2].face.transf);
//                        he3d[(movIndex + 1) % 2].face.objTrans.getTransform(fixTransf);
                        movTransf.transform(movedPoint1);
                        movTransf.transform(movedPoint2);
                        fixTransf.transform(fixedPoint1);
                        fixTransf.transform(fixedPoint2);
                        if (movedPoint1.distanceSquared(fixedPoint1) > 1E-5) {
                            Point3d middlePoint1 = new Point3d((movedPoint1.x + fixedPoint1.x) / 2,
                                    (movedPoint1.y + fixedPoint1.y) / 2, (movedPoint1.z + fixedPoint1.z) / 2);
                            
//                            updateOffsetPosition(he3d[movIndex], middlePoint1);
                            updateOffsetPosition(he3d[movIndex], movedPoint1);
                            
//                            System.out.println("move point 1 " + movedPoint1 + " " + fixedPoint1);
                        } else {
                            restoreOffsetPosition(he3d[movIndex]);
//                            System.out.println("restore point 1 " + movedPoint1 + " " + fixedPoint1);
                        }
                        if (movedPoint2.distanceSquared(fixedPoint2) > 1E-5) {
                            Point3d middlePoint2 = new Point3d((movedPoint2.x + fixedPoint2.x) / 2,
                                    (movedPoint2.y + fixedPoint2.y) / 2, (movedPoint2.z + fixedPoint2.z) / 2);
                            
//                            updateOffsetPosition(he3d[(movIndex + 1) % 2], middlePoint2);
                            updateOffsetPosition(he3d[(movIndex + 1) % 2], movedPoint2);
//                            System.out.println("move point 2 " + movedPoint2 + " " + fixedPoint2);
                        } else {
                            restoreOffsetPosition(he3d[(movIndex + 1) % 2]);
//                            System.out.println("restore point 2 " + movedPoint2 + " " + fixedPoint2);
                        }
                        return;
                    }
                    movable.updated = true;
//                    updated = true;
//                    Transform3D transform = new Transform3D();
//                    movable.objTrans.getTransform(transform);

                    if (spheres == null) {
                        spheres = new ConstraintSphere[2];
                        spheres[0] = new ConstraintSphere(he3d[0]);
                        spheres[1] = new ConstraintSphere(he3d[1]);
                        spheres[0].neighbors.add(spheres[1]);
                        spheres[1].neighbors.add(spheres[0]);
                    }
                    if (spheres[0].isEdge) {
                        spheres[1].solveDihedralAngles();
                    } else {
                        spheres[0].solveDihedralAngles();
                    }
                    Transform3D transf = new Transform3D();
                    transf.mul(movable.initialTransf);
                    Transform3D rot = rotate(movable, foldingAngle);
//                    Transform3D zshift = new Transform3D();
//                    zshift.setTranslation(new Vector3d(0, 0, (desiredAngle > 0 ? .01 : -.01)));
//                    transf.mul(zshift);
                    transf.mul(rot);
//                    movable.objTrans.setTransform(transf);
                    movable.transf = transf;
                    for (int i = 0; i < movable.halfEdges.length; i++) {
                        OriHalfedge3D he = movable.halfEdges[i];
                        Transform3D result = new Transform3D();
                        result.mulInverse(transf, movable.initialTransf);
                        he.edge.updateTransform(result);
                    }


                    System.out.println("1 " + this + " " + foldingAngle * 180 / Math.PI + " " + desiredAngle * 180 / Math.PI + " " + movable);
                }
            }//else{

            //}
        } else {
//        System.out.println("entrou 2"+he3d[0]+he3d[1]);
            OriFace3D movable = null;
            if (he3d[0] == null || he3d[1] == null) {
                return;
            }
            if ((!he3d[0].face.isFixed) && (!he3d[0].face.updated)) {
                movable = he3d[0].face;
            } else if ((!he3d[1].face.isFixed) && (!he3d[1].face.updated)) {
                movable = he3d[1].face;
            }
            if (movable == null) {
                return;
            }
//            Transform3D transform = new Transform3D();
//            movable.objTrans.getTransform(transform);
//            initial.mul(transform);
            Transform3D transf = new Transform3D();
            transf.mul(initial);
            transf.mul(movable.initialTransf);
            System.out.println("2 " + this + " " + foldingAngle * 180 / Math.PI + " " + desiredAngle * 180 / Math.PI + " " + movable);
            if (changes) {
//                if (Math.abs(foldingAngle - desiredAngle) > Crease.TOLERANCE) {
//                    foldingAngle += desiredAngle / 64;
//                }
                Transform3D rot = rotate(movable, foldingAngle);
//                Transform3D zshift = new Transform3D();
//                zshift.setTranslation(new Vector3d(0, 0, (desiredAngle > 0 ? .01 : -.01)));
//                transf.mul(zshift);
                transf.mul(rot);

            }
            movable.updated = true;
//            movable.objTrans.setTransform(transf);
            movable.transf = transf;
            for (int i = 0; i < movable.halfEdges.length; i++) {
                OriHalfedge3D he = movable.halfEdges[i];
                Transform3D result = new Transform3D();
                result.mulInverse(transf, movable.initialTransf);
                he.edge.updateTransform(result);
            }
        }
    }

    public Transform3D rotate(OriFace3D face, double angle) {
        Transform3D transf = new Transform3D();
        Transform3D transl1 = new Transform3D();
        Vector3d translVec1 = new Vector3d(-he3d[0].v.x, -he3d[0].v.y, 0);
        transl1.setTranslation(translVec1);
        double alfa = Math.atan2(he3d[1].v.y - he3d[0].v.y, he3d[1].v.x - he3d[0].v.x);

        Transform3D rot1 = new Transform3D();
        rot1.rotZ(-alfa);
        Transform3D rot2 = new Transform3D();
        rot2.rotX(he3d[0].face.equals(face) ? angle : -angle);
        transf.mul(rot1, transl1);
        transf.mul(rot2, transf);
        rot1.invert();
        transf.mul(rot1, transf);
        transl1.invert();
        transf.mul(transl1, transf);
        return transf;
    }

    @Override
    public String toString() {
        String str = "edge3d t:" + type + " " + he3d[0] + " " + he3d[1];
        return str;
    }

    private void updateOffsetPosition(OriHalfedge3D he0, Point3d middlePoint) {
        OriHalfedge3D he = he0;
        boolean reachedEdge = false;
//        System.out.println("entrou 1");
        do {
//            System.out.println("entrou " + he + reachedEdge);
            OriFace3D face = he.face;
            Transform3D transf = new Transform3D(face.transf);
//            face.objTrans.getTransform(transf);
            Point3d transfMiddlePoint = new Point3d(middlePoint);
            transf.invert();
            transf.transform(transfMiddlePoint);
            face.updatePoint(transfMiddlePoint, he0.v);
            if (!reachedEdge) {
                if (he.pair != null) {
                    he = he.pair.next;
                } else {
                    reachedEdge = true;
                    he = he0.prev.pair;
                }
            } else {
                he = he.prev.pair;
            }
            if (he == null) {
                return;
            }
        } while (!he.equals(he0));
    }

    private void restoreOffsetPosition(OriHalfedge3D he0) {
        OriHalfedge3D he = he0;
        boolean reachedEdge = false;
//        System.out.println("entrou 1");
        do {
//            System.out.println("entrou " + he + " " + reachedEdge);
            OriFace3D face = he.face;
            face.updatePoint(he.v, he.v);
            if (!reachedEdge) {
                if (he.pair != null) {
                    he = he.pair.next;
                } else {
                    reachedEdge = true;
                    he = he0.prev.pair;
                }
            } else {
                he = he.prev.pair;
            }
            if (he == null) {
                return;
            }
        } while (!he.equals(he0));
    }

    void setSpheresUpdated(boolean b) {
        if (spheres!=null) {
            spheres[0].updated = b;
            spheres[1].updated = b;
        }
    }
}
