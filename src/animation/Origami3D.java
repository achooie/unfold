/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animation;

import Simplification.Crease;
import java.util.ArrayList;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import origamid.GeomUtilD;
import oripa.Constants;
import oripa.DataSet;
import oripa.Doc;
import oripa.geom.OriEdge;
import oripa.geom.OriFace;
import oripa.geom.OriHalfedge;
import oripa.geom.OriLine;
import oripa.ExporterXML;

/**
 *
 * @author akitaya
 */
public class Origami3D {

    public ArrayList<OriFace3D> faces3d;
    ArrayList<OriEdge3D> edges3d;
    private ExporterXML exporter = new ExporterXML();

    public Origami3D() {
        OriFace3D.init();
    }

    public void buildFaces(Doc before, Doc after) {
        ArrayList<OriFace3D> oldFaces = null;
        Doc mix;
        if (before != null) {
            oldFaces = faces3d;
            mix = new Doc(Constants.DEFAULT_PAPER_SIZE);
            for (int i = 0; i < before.lines.size(); i++) {
                OriLine oriLine = before.lines.get(i);
                mix.addLine(oriLine);
            }
            for (int i = 0; i < after.lines.size(); i++) {
                OriLine oriLine = after.lines.get(i);
                mix.addLine(oriLine);
            }
            mix.buildOrigami3(true);
        } else {
            mix = after;
        }
        //////
        try {
            exporter.export(new DataSet(mix), "results/" + "simulation-" + ".opx");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ///////
        System.out.println("mix face n " + mix.faces.size());
        faces3d = new ArrayList<>();
        edges3d = new ArrayList<>();
        for (int i = 0; i < mix.edges.size(); i++) {
            OriEdge oriEdge = mix.edges.get(i);
            if (before == null) {
                edges3d.add(new OriEdge3D((oriEdge.type == Crease.MOUNTAIN
                        || oriEdge.type == Crease.VALLEY) ? true : false, oriEdge.type, Crease.AUX,
                        (oriEdge.type == Crease.MOUNTAIN ? Math.PI : -Math.PI)));
            } else {
                OriEdge beforeEdge = null;
                OriEdge afterEdge = null;
                System.out.println("edge " + oriEdge.ev.preP.x + " " + oriEdge.ev.preP.y + "" + oriEdge.sv.preP.x + " " + oriEdge.sv.preP.y);
                for (int j = 0; j < before.edges.size(); j++) {
                    OriEdge e = before.edges.get(j);
                    if (GeomUtilD.isLineSegmentContained(oriEdge.sv.preP,
                            oriEdge.ev.preP, e.sv.preP, e.ev.preP)) {
                        beforeEdge = e;
                        System.out.println("before edge " + e.ev.preP.x + " " + e.ev.preP.y + "" + e.sv.preP.x + " " + e.sv.preP.y);
                        break;
                    }
                }
                for (int j = 0; j < after.edges.size(); j++) {
                    OriEdge e = after.edges.get(j);
                    System.out.println("possible after edge " + e.ev.preP.x + " " + e.ev.preP.y + " " + e.sv.preP.x + " " + e.sv.preP.y);
                    if (GeomUtilD.isLineSegmentContained(oriEdge.sv.preP,
                            oriEdge.ev.preP, e.sv.preP, e.ev.preP)) {
                        afterEdge = e;
                        System.out.println("after edge " + e.ev.preP.x + " " + e.ev.preP.y + " " + e.sv.preP.x + " " + e.sv.preP.y);
                        break;
                    }
                }
                double desiredAngle = 0;
                if (beforeEdge == null) {
                    desiredAngle = (afterEdge.type == Crease.MOUNTAIN
                            ? Math.PI : -Math.PI);
                } else if (afterEdge == null) {
                    desiredAngle = (beforeEdge.type == Crease.MOUNTAIN
                            ? -Math.PI : +Math.PI);
                } else if (beforeEdge.type == Crease.MOUNTAIN && afterEdge.type == Crease.VALLEY) {
                    desiredAngle = -2 * Math.PI;
                } else if (beforeEdge.type == Crease.VALLEY && afterEdge.type == Crease.MOUNTAIN) {
                    desiredAngle = 2 * Math.PI;
                }
                edges3d.add(new OriEdge3D(desiredAngle != 0, (afterEdge == null ? Crease.AUX : afterEdge.type),
                        (beforeEdge == null ? Crease.AUX : beforeEdge.type), desiredAngle));
            }
        }
        for (int i = 0; i < mix.faces.size(); i++) {
            OriFace oriFace = mix.faces.get(i);
            OriFace3D face3d = new OriFace3D(new Transform3D(), oriFace);
            if (before == null) {
                face3d.faceAfter = oriFace;
                face3d.faceBefore = null;
            } else {
                Vector2d centerVec = new Vector2d();
                for (OriHalfedge he : oriFace.halfedges) {
                    centerVec.add(he.vertex.preP);
                }
                centerVec.scale(1.0 / oriFace.halfedges.size());
                for (int j = 0; j < after.faces.size(); j++) {
                    OriFace afterFace = after.faces.get(j);
                    if (origamid.GeomUtilD.isContainPointFace(afterFace, centerVec, 1E-10)) {
                        face3d.faceAfter = afterFace;
                        break;
                    }
                }
                for (int j = 0; j < before.faces.size(); j++) {
                    OriFace beforeFace = before.faces.get(j);
                    if (origamid.GeomUtilD.isContainPointFace(beforeFace, centerVec, 1E-10)) {
                        face3d.faceBefore = beforeFace;
                        break;
                    }
                }
                boolean foundFace = false;
                do {
                    for (int j = 0; j < oldFaces.size(); j++) {
                        OriFace simFace = oldFaces.get(j).faceSimulation;
                        if (origamid.GeomUtilD.isContainPointFace(simFace, centerVec, 1E-10)) {
                            face3d.initialTransf = new Transform3D(oldFaces.get(j).transf);
//                            oldFaces.get(j).objTrans.getTransform(face3d.initialTransf);
//                            face3d.objTrans.setTransform(face3d.initialTransf);
                            face3d.transf = new Transform3D(face3d.initialTransf);
                            face3d.zdiff = (int) -oldFaces.get(j).zPosition;
                            face3d.zPosition = (int) oldFaces.get(j).zPosition;
                            foundFace = true;
                            break;
                        }
                    }
                    centerVec.x += 2E-10 * (Math.random() - .5);
                    centerVec.y += 2E-10 * (Math.random() - .5);
                    System.out.println("center " + centerVec);
                } while (!foundFace);
            }
            faces3d.add(face3d);
            face3d.halfEdges = new OriHalfedge3D[oriFace.halfedges.size()];
            for (int j = 0; j < oriFace.halfedges.size(); j++) {
                OriHalfedge he = oriFace.halfedges.get(j);
                OriHalfedge3D he3d = new OriHalfedge3D(face3d, new Point3d(he.vertex.preP.x / 100, he.vertex.preP.y / 100, 0));
                face3d.halfEdges[j] = he3d;
            }
            for (int j = 0; j < oriFace.halfedges.size(); j++) {
                OriHalfedge he = oriFace.halfedges.get(j);
                int aux = oriFace.halfedges.indexOf(he.next);
                face3d.halfEdges[j].next = face3d.halfEdges[aux];
                aux = oriFace.halfedges.indexOf(he.prev);
                face3d.halfEdges[j].prev = face3d.halfEdges[aux];
                aux = mix.edges.indexOf(he.edge);
                edges3d.get(aux).setHalfEdge(face3d.halfEdges[j]);
            }
        }
        //\\/agaaaaaaaaaaaaaaaaaaaa
        System.out.println(mix.faces.size() + "face n " + faces3d);
        setMovableFaces();
        setFacesZindex(after);
    }

    public void addFaces2Scene(BranchGroup objRoot) {
        for (int i = 0; i < faces3d.size(); i++) {
            OriFace3D face = faces3d.get(i);
            face.build();
            objRoot.addChild(face.branchGroup);
        }
    }

    public void setMovableFaces() {
        int movability[] = new int[faces3d.size()];
        for (int i = 0; i < faces3d.size(); i++) {
            OriFace3D face = faces3d.get(i);
            for (int j = 0; j < face.halfEdges.length; j++) {
                if (face.halfEdges[j].edge.changes) {
                    movability[i]++;
                }
            }
        }
        int smallestIndex = 0;
        for (int i = 1; i < movability.length; i++) {
            if (movability[i] < movability[smallestIndex]) {
                smallestIndex = i;
            }
        }
        propagateFixed(faces3d.get(smallestIndex));
//        faces3d.get(2).isMovable = true;
    }

    private void propagateFixed(OriFace3D face) {
        System.out.println("propagate fix " + face);
        if (face.isFixed) {
            return;
        }
        face.isFixed = true;
        for (int i = 0; i < face.halfEdges.length; i++) {
            OriHalfedge3D he = face.halfEdges[i];
            System.out.println("loop " + he + face);
            if (!he.edge.changes) {
                System.out.println("loop2 " + he.pair);
                if (he.pair != null) {
                    System.out.println("loop2 " + he.pair + he.pair.face);
                    propagateFixed(he.pair.face);
                }
            }
        }
    }
//    public ArrayList<OriFace3D> buildFaces(Doc before, Doc after) {
//        ArrayList<OriFace3D> orifaces3d = new ArrayList<>();
//        Doc mix = new Doc(Constants.DEFAULT_PAPER_SIZE);
//        for (int i = 0; i < before.lines.size(); i++) {
//            OriLine oriLine = before.lines.get(i);
//            mix.addLine(oriLine);
//        }
//        for (int i = 0; i < after.lines.size(); i++) {
//            OriLine oriLine = after.lines.get(i);
//            mix.addLine(oriLine);
//        }
//        mix.buildOrigami3(true);
//        //////
//        try {
//            oripa.ExporterXML.export(new DataSet(mix), "results/mix.opx");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        ///////
//        for (int i = 0; i < mix.faces.size(); i++) {
//            OriFace oriFace = mix.faces.get(i);
//            OriVertex center = new OriVertex(oriFace.getCenter());
//            OriFace3D face3d = null;
//            for (int j = 0; j < faces3d.size(); j++) {
//                OriFace3D oriFace3D = faces3d.get(j);
//                if (GeomUtil.isOnFace(center, oriFace3D.faceSimulation)) {
//                    face3d = new OriFace3D(oriFace3D, oriFace);
//                }
//            }
//        }
//        return orifaces3d;
//    }

    public void updateTransform() {
        for (int i = 0; i < faces3d.size(); i++) {
            faces3d.get(i).updated = false;
        }
        for (int i = 0; i < edges3d.size(); i++) {
            edges3d.get(i).updated = false;
            edges3d.get(i).setSpheresUpdated(false);
        }
        for (int i = 0; i < edges3d.size(); i++) {
            edges3d.get(i).updateTransform(null);
        }
        for (int i = 0; i < faces3d.size(); i++) {
            faces3d.get(i).updateTransform();
        }
    }

    void removeFacesFromScene(BranchGroup scene) {
        for (int i = 0; i < faces3d.size(); i++) {
            OriFace3D oriFace3D = faces3d.get(i);
            scene.removeChild(oriFace3D.branchGroup);
        }
    }

    private void setFacesZindex(Doc doc) {
        ArrayList<OriFace> sortedFaces = doc.sortedFaces;
//        boolean[] isSorted = new boolean[doc.faces.size()];
//        for (int i = 0; i < doc.faces.size(); i++) {
//            for (int j = 0; j < doc.overlapRelation.length; j++) {
//                int numberOf2 = 0;
//                if (!isSorted[j]) {
//                    for (int k = 0; k < isSorted.length; k++) {
//                        if ((!isSorted[k]) && doc.overlapRelation[j][k] == 2) {
//                            numberOf2++;
//                        }
//                    }
//                    if (numberOf2 == 0) {
//                        isSorted[j] = true;
//                        sortedFaces.add(doc.faces.get(j));
//                        break;
//                    }
//                }
//            }
//        }
        boolean isFlipped = false;
        for (int i = 0; i < faces3d.size(); i++) {
            OriFace3D face = faces3d.get(i);
            if (face.isFixed) {
                Transform3D t = new Transform3D(face.transf);
//                face.objTrans.getTransform(t);
                Point3d p = new Point3d(0, 0, 1);
                t.transform(p);
                System.out.println("face front " + face.faceAfter.faceFront + " " + p.z);
                if ((!face.faceAfter.faceFront && p.z > 0)
                        || (face.faceAfter.faceFront && p.z < 0)) {
                    isFlipped = true;
                }
                break;
            }
        }
        for (int i = 0; i < faces3d.size(); i++) {
            OriFace3D face = faces3d.get(i);
            face.faceAfter.z_order = (isFlipped
                    ? sortedFaces.size() - sortedFaces.indexOf(face.faceAfter)
                    : sortedFaces.indexOf(face.faceAfter));
            face.zdiff += face.faceAfter.z_order;
            System.out.println("z diff " + face + " " + face.zdiff);
            face.updateTransform();
        }
    }
}
