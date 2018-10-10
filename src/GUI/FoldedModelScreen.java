/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import diagram.SymblMountain;
import diagram.SymblDotted;
import diagram.SymblPArrow;
import diagram.SymblVArrow;
import diagram.SymblValley;
import diagram.DiagramSymbol;
import Simplification.CP;
import Simplification.Crease;
import Simplification.ExecutionNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.vecmath.Vector2d;
import origamid.GeomUtilD;
import oripa.Doc;
import oripa.ORIPA;
import oripa.RenderScreen2;
import oripa.geom.*;

/**
 *
 * @author akitaya
 */
public class FoldedModelScreen extends RenderScreen2 {

    ArrayList<DiagramSymbol> symbols;
    ArrayList<ArrayList<DiagramSymbol>> autoSymbols;
    DiagramSymbol tempSymbol;
    DiagramSymbol selectedSymbol;
    ArrayList<Vector2d> clickable;
    private Vector2d prePickV = null;
    private Vector2d preprePickV = null;
    ExecutionNode step;
    Doc doc;
    boolean isMainView;
    public boolean isFlipped;
    String description;
    static Color faceFront = new Color(160, 180, 255);
    static Color faceBack = new Color(240, 240, 240);
    static Color outlineColor = new Color(0, 0, 255);
    static Stroke outlineStroke = new BasicStroke(3);
    private AffineTransform transf;

    public FoldedModelScreen(ExecutionNode execNode, boolean isMainView, String description, boolean isFlipped) {
        this.step = execNode;
        this.isMainView = isMainView;
        this.description = description;
        symbols = new ArrayList<>();
        clickable = new ArrayList<>();
        doc = ORIPA.doc;
        autoSymbols = new ArrayList<>();
        for (int i = 0; i < doc.sortedFaces.size(); i++) {
            autoSymbols.add(new ArrayList<DiagramSymbol>());
        }
    }

    public void clearSymbols() {
        symbols.clear();
        prePickV = null;
        preprePickV = null;
        repaint();
    }

    public void deleteSelectedSymbol() {
        if (selectedSymbol != null) {
            symbols.remove(selectedSymbol);
            selectedSymbol = null;
            repaint();
        }
    }

    public void unSelected() {
        selectedSymbol = null;
        repaint();
    }

    public void setStep(ExecutionNode step) {
        this.step = step;
    }

    public void copy(FoldedModelScreen screen) {
        this.description = screen.description;
        this.step = screen.step;
        symbols = screen.symbols;
        autoSymbols = screen.autoSymbols;
        doc = screen.doc;
        isFlipped = screen.isFlipped;
        clickable = screen.clickable;
        ORIPA.doc = doc;
        this.resetViewMatrix();
        redrawOrigami();
    }

    public Doc getDoc() {
        return doc;
    }

    public ArrayList<DiagramSymbol> getSymbols() {
        return symbols;
    }

    public boolean addClickableVertex(Vector2d vec) {
        for (Vector2d clickVec : clickable) {
            if (Math.abs(clickVec.x - vec.x) < Crease.TOLERANCE
                    && Math.abs(clickVec.y - vec.y) < Crease.TOLERANCE) {
                return false;
            }
        }
        clickable.add(vec);
        return true;
    }

    public Vector2d translateCoordinates(Vector2d vec) {
        if (doc.foldedBBoxLT != null) {
            Vector2d docCenter = new Vector2d((doc.foldedBBoxLT.x + doc.foldedBBoxRB.x) / 2,
                    (doc.foldedBBoxLT.y + doc.foldedBBoxRB.y) / 2);
            double localScale = Math.min(
                    600 / (doc.foldedBBoxRB.x - doc.foldedBBoxLT.x),
                    600 / (doc.foldedBBoxRB.y - doc.foldedBBoxLT.y)) * 0.95;
            localScale *= .8;
            double x1 = (vec.x - docCenter.x) * localScale + 300;
            double y1 = -(vec.y - docCenter.y) * localScale + 300;
            return new Vector2d(x1, y1);
        }
        return null;
    }

    void addClickableOriVertex(Doc prevDoc) {
        if (transf == null) {
            setTransformation(prevDoc);
        }
        for (OriVertex oriVertex : prevDoc.vertices) {
            Vector2d pointAfter = GeomUtilD.getPositionFolded(oriVertex, doc);
            if (pointAfter != null) {
                double[] point = new double[2];
//                transf.transform(new double[]{pointAfter.x, pointAfter.y}, 0, point, 0, 1);
                Vector2d translated = translateCoordinates(pointAfter);
                if (translated != null) {
                    addClickableVertex(translated);
                    transf.transform(new double[]{oriVertex.p.x, oriVertex.p.y}, 0, point, 0, 1);
                    Vector2d translated2 = translateCoordinates(new Vector2d(point));
//                    addClickableVertex(translated2);
                    if (Math.abs(pointAfter.x - point[0]) > Crease.TOLERANCE
                            || Math.abs(pointAfter.y - point[1]) > Crease.TOLERANCE) {
//                        Vector2d translated2 = translateCoordinates(new Vector2d(point));

                        OriFace face = GeomUtilD.getFaceContainingPoint(oriVertex.preP, doc);
                        System.out.println("vertices ass" + doc.sortedFaces + " " + face + " " + oriVertex.p);
//                        autoSymbols.get(doc.sortedFaces.indexOf(face)).add(new SymblVArrow((int) translated.x,
//                                (int) translated.y, (int) translated2.x, (int) translated2.y));
                        symbols.add(new SymblVArrow((int) translated.x,
                                (int) translated.y, (int) translated2.x, (int) translated2.y));
                    }
                }
            }
        }
    }

//    Vector2d getPositionFolded(OriVertex oriVertex) {
//        for (OriFace oriFace : doc.faces) {
//            if (origamid.GeomUtilD.isOnFace(oriVertex, oriFace)) {
//                Vector2d originBefore = oriFace.halfedges.get(0).next.vertex.preP;
//                Vector2d vecBefore1 = new Vector2d();
//                vecBefore1.sub(oriFace.halfedges.get(0).vertex.preP, originBefore);
//                Vector2d vecBefore2 = new Vector2d();
//                vecBefore2.sub(oriFace.halfedges.get(0).next.next.vertex.preP, originBefore);
//                Vector2d originAfter = oriFace.halfedges.get(0).next.vertex.p;
//                Vector2d vecAfter1 = new Vector2d();
//                vecAfter1.sub(oriFace.halfedges.get(0).vertex.p, originAfter);
//                Vector2d vecAfter2 = new Vector2d();
//                vecAfter2.sub(oriFace.halfedges.get(0).next.next.vertex.p, originAfter);
//                Vector2d pointBefore = new Vector2d();
//                pointBefore.sub(oriVertex.preP, originBefore);
//                double a = 0, b = 0;
//                if ((vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y) != 0) {
//                    b = (pointBefore.y * vecBefore1.x - pointBefore.x * vecBefore1.y)
//                            / (vecBefore1.x * vecBefore2.y - vecBefore2.x * vecBefore1.y);
//                    a = (pointBefore.y * vecBefore2.x - pointBefore.x * vecBefore2.y)
//                            / (vecBefore2.x * vecBefore1.y - vecBefore1.x * vecBefore2.y);
//                }
//                Vector2d pointAfter = new Vector2d();
//                pointAfter.x = originAfter.x + a * vecAfter1.x + b * vecAfter2.x;
//                pointAfter.y = originAfter.y + a * vecAfter1.y + b * vecAfter2.y;
////                System.out.println("vecb1"+vecBefore1+" vecb2"+vecBefore2+" ob"+originBefore);
////                System.out.println("veca1"+vecAfter1+" veca2"+vecAfter2+" oa"+originAfter);
////                System.out.println("point "+pointBefore+" pointa"+pointAfter+" a "+a+" b "+b);
////                
////                AffineTransform transform = new AffineTransform();
////                transform.translate(-originBefore.x, -originBefore.y);
////                System.out.println("entrou !!!!" +vecBefore);
////                transform.rotate(vecBefore.y > 0 ? -vecBefore.angle(x) : vecBefore.angle(x));
////                transform.rotate(vecAfter.y > 0 ? vecBefore.angle(x) : -vecBefore.angle(x));
////                transform.translate(originAfter.x, originAfter.y);
////                double[] point = new double[2];
////                transform.transform(new double[]{oriVertex.preP.x, oriVertex.preP.y}, 0, point, 0, 1);
////                addClickableVertex(new Vector2d(point));
//
//                return pointAfter;
//            }
//        }
//        return null;
//    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        double scale = getScale();
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setTransform(getAffineTransform());
        for (int i = 0; i < doc.sortedFaces.size(); i++) {
            OriFace face = doc.sortedFaces.get(isFlipped ? i : doc.sortedFaces.size() - 1 - i);
            if (!isFlipped && face.faceFront || isFlipped && !face.faceFront) {
                g2d.setColor(faceFront);
            } else {
                g2d.setColor(faceBack);
            }
            int[] xPoints = new int[face.halfedges.size()];
            int[] yPoints = new int[xPoints.length];
            for (int j = 0; j < xPoints.length; j++) {
                Vector2d v = translateCoordinates(face.halfedges.get(j).vertex.p);
                xPoints[j] = (int) v.x;
                yPoints[j] = (int) v.y;
            }
            g2d.fillPolygon(xPoints, yPoints, xPoints.length);
            g2d.setColor(outlineColor);
            g2d.setStroke(outlineStroke);
            for (int j = 0; j < xPoints.length - 1; j++) {
                g2d.drawLine(xPoints[j], yPoints[j], xPoints[j + 1], yPoints[j + 1]);
            }
            g2d.drawLine(xPoints[0], yPoints[0], xPoints[xPoints.length - 1], yPoints[xPoints.length - 1]);
            ArrayList<DiagramSymbol> faceLines = autoSymbols.get(isFlipped ? i : doc.sortedFaces.size() - 1 - i);
            for (DiagramSymbol diagramSymbol : faceLines) {
                diagramSymbol.paint(g2d, isFlipped);
            }
        }
        g2d.setColor(Color.black);
//        if (doc.sortedFaces.size() > 0) {
//            Vector2d translated = translateCoordinates(doc.sortedFaces.get(0).halfedges.get(0).vertex.p);
//            Vector2d translated1 = translateCoordinates(doc.sortedFaces.get(0).halfedges.get(1).vertex.p);
//            g2d.drawLine((int) translated.x, (int) translated.y, (int) translated1.x, (int) translated1.y);
//            g2d.setColor(Color.red);
//            int[] xPoints = {(int) translated.x, (int) translated1.x, 0};
//            int[] yPoints = {(int) translated.y, (int) translated1.y, 0};
//            g2d.fillPolygon(xPoints, yPoints, 3);
//            System.out.println("asdf " + translated + " " + translated1);
//        }
        for (DiagramSymbol d : symbols) {
            d.paint(g2d);
        }
        for (Vector2d clickVec : clickable) {
            g2d.fill(new Rectangle2D.Double(clickVec.x - 2 / scale,
                    clickVec.y - 2 / scale, 4 / scale, 4 / scale));
        }
        if (tempSymbol != null) {
            g2d.setColor(Color.green);
            tempSymbol.paint(g2d);
        }
        if (selectedSymbol != null) {
            g2d.setColor(Color.green);
            selectedSymbol.paint(g2d);
        }
        if (prePickV != null) {
            g2d.setColor(Color.green);
            g2d.fill(new Rectangle2D.Double(prePickV.x - 5.0 / scale,
                    prePickV.y - 5.0 / scale, 10.0 / scale, 10.0 / scale));
        }
        if (preprePickV != null) {
            g2d.setColor(Color.green);
            g2d.fill(new Rectangle2D.Double(preprePickV.x - 5.0 / scale,
                    preprePickV.y - 5.0 / scale, 10.0 / scale, 10.0 / scale));
        }
        g.drawImage(img, 0, 0, this);
        g.drawString(description, 10, 10);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isMainView) {
            double[] point = new double[2];
            if (prePickV != null) {
                point[0] = prePickV.x;
                point[1] = prePickV.y;
                if (preprePickV == null) {
                    preprePickV = prePickV;
                    prePickV = null;
                } else {
                    preprePickV = null;
                }
            } else {
                try {
                    getAffineTransform().inverseTransform(new double[]{e.getX(), e.getY()}, 0, point, 0, 1);
                } catch (NoninvertibleTransformException ex) {
                    ex.printStackTrace();
                }
            }
            if (MainFolderFrame.jToggleButtonMountain.isSelected()) {
                if (tempSymbol == null) {
                    tempSymbol = new SymblMountain((int) point[0], (int) point[1],
                            (int) point[0], (int) point[1]);
                } else {
                    tempSymbol.setWidth(2);
                    symbols.add(tempSymbol);
                    tempSymbol = null;
                }
            } else if (MainFolderFrame.jToggleButtonValley.isSelected()) {
                if (tempSymbol == null) {
                    tempSymbol = new SymblValley((int) point[0], (int) point[1],
                            (int) point[0], (int) point[1]);
                } else {
                    tempSymbol.setWidth(2);
                    symbols.add(tempSymbol);
                    tempSymbol = null;
                }
            } else if (MainFolderFrame.jToggleButtonDotted.isSelected()) {
                if (tempSymbol == null) {
                    tempSymbol = new SymblDotted((int) point[0], (int) point[1],
                            (int) point[0], (int) point[1]);
                } else {
                    tempSymbol.setWidth(2);
                    symbols.add(tempSymbol);
                    tempSymbol = null;
                }
            } else if (MainFolderFrame.jToggleButtonSelect.isSelected()) {
                for (DiagramSymbol s : symbols) {
                    if (s.distanceFromPoint(point) < 3) {
                        selectedSymbol = s;
                        break;
                    }
                    selectedSymbol = null;
                }
            }
        } else {
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        double[] point = new double[2];
        try {
            getAffineTransform().inverseTransform(new double[]{e.getX(), e.getY()}, 0, point, 0, 1);
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
        if (tempSymbol != null) {
            tempSymbol.setEndPoint((int) point[0], (int) point[1]);
        }

        prePickV = null;
        for (Vector2d clickV : clickable) {
            if (GeomUtil.DistanceSquared(clickV.x, clickV.y, point[0], point[1])
                    < 20 / getScale()) {
                prePickV = clickV;
//                System.out.println("Clickable point "+clickV);
                break;
            }
        }
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (isMainView) {
            super.mouseWheelMoved(e);
        } else {
            getParent().getParent().dispatchEvent(e);
        }
    }

    @Override
    public void flipFaces(boolean bFlip) {
        super.flipFaces(bFlip);
        isFlipped = bFlip;
    }

    void addLineSymbol(OriEdge oriEdge) {
        if (oriEdge.type == Crease.AUX || oriEdge.type == Crease.PAPER_EDGE) {
            return;
        }
        for (OriEdge localEdge : doc.edges) {
//            if (GeomUtilD.isSameSegment(oriEdge.sv.preP, oriEdge.ev.preP,
//                    localEdge.sv.preP, localEdge.ev.preP)) {
//                return;
//            }
            if (GeomUtilD.isLineSegmentContained(oriEdge.sv.preP, oriEdge.ev.preP,
                    localEdge.sv.preP, localEdge.ev.preP)) {
//                OriEdge subtraction = GeomUtilD.subtract(localEdge, oriEdge);
//                addPushSymbol(subtraction);
                return;
            }
        }
        Vector2d point1 = translateCoordinates(GeomUtilD.getPositionFolded(oriEdge.ev, doc));
        Vector2d point2 = translateCoordinates(GeomUtilD.getPositionFolded(oriEdge.sv, doc));
        OriFace face = GeomUtilD.getFaceContainingPoint(GeomUtilD.getMidPoint(oriEdge.ev.preP, oriEdge.sv.preP), doc);
//        System.out.println(oriEdge.ev.preP.x + ", " + oriEdge.ev.preP.y + "p1 " + point1 + " p2 " + point2 + "  " + oriEdge.sv.preP.x + ", " + oriEdge.sv.preP.y);
        DiagramSymbol symbol;
        if (oriEdge.type == Crease.MOUNTAIN) {
            if (face.faceFront) {
                symbol = new SymblMountain((int) point1.x, (int) point1.y,
                        (int) point2.x, (int) point2.y);
            } else {
                symbol = new SymblValley((int) point1.x, (int) point1.y,
                        (int) point2.x, (int) point2.y);
            }
        } else {
            if (face.faceFront) {
                symbol = new SymblValley((int) point1.x, (int) point1.y,
                        (int) point2.x, (int) point2.y);
            } else {
                symbol = new SymblMountain((int) point1.x, (int) point1.y,
                        (int) point2.x, (int) point2.y);
            }
        }
        int faceIndx = doc.sortedFaces.indexOf(face);
        if(autoSymbols.size()>0)
        autoSymbols.get(faceIndx==-1?0:faceIndx).add(symbol);
    }

    public ArrayList<ArrayList<DiagramSymbol>> getAutoSymbols() {
        return autoSymbols;
    }

    private void addPushArrowSymbol(OriEdge oriEdge) {
        Vector2d sub = new Vector2d(oriEdge.ev.preP);
        sub.sub(oriEdge.sv.preP);
        sub.scale(.1);
        Vector2d mid = GeomUtilD.getMidPoint(oriEdge.sv.preP, oriEdge.ev.preP);
        mid.x -= sub.y;
        mid.y += sub.x;
        Vector2d point1 = translateCoordinates(GeomUtilD.getPositionFolded(oriEdge.ev, doc));
        Vector2d point2 = translateCoordinates(GeomUtilD.getPositionFolded(oriEdge.sv, doc));
        Vector2d point3 = translateCoordinates(GeomUtilD.getPositionFolded(new OriVertex(mid), doc));
        OriFace face = GeomUtilD.getFaceContainingPoint(mid, doc);
        mid = GeomUtilD.getMidPoint(point1, point2);
        sub = new Vector2d(point3);
        sub.sub(mid);
        sub.scale(-2);
        sub.add(mid);
        int faceIndx = doc.sortedFaces.indexOf(face);
        if(autoSymbols.size()>0)
        autoSymbols.get(faceIndx==-1?0:faceIndx).add(new SymblPArrow((int) sub.x, (int) sub.y,
                (int) mid.x, (int) mid.y));
    }

    void addPushSymbols(CP stepCp) {
        //creases of the current step (simplifyed)
        ArrayList<Crease> creases1 = step.stepCp.getCreases();
        //creases of the next step(after folding)
        ArrayList<Crease> creases2 = stepCp.getCreases();
        for (int i = 0; i < creases1.size(); i++) {
            Crease c1 = creases1.get(i);
            if (c1.getType() == Crease.AUX || c1.getType() == Crease.PAPER_EDGE) {
                continue;
            }
            boolean havePair = false;
            for (int j = 0; j < creases2.size(); j++) {
                Crease c2 = creases2.get(j);
                if (c1.equals(c2)) {
                    havePair = true;
                    break;
                }
            }
            if (havePair) {
                continue;
            }
            Vector2d s1 = new Vector2d(c1.getNodes()[0].getX(), c1.getNodes()[0].getY());
            Vector2d e1 = new Vector2d(c1.getNodes()[1].getX(), c1.getNodes()[1].getY());
            for (int j = 0; j < creases2.size(); j++) {
                Crease c2 = creases2.get(j);
                if (c2.getType() != c1.getType()) {
                    continue;
                }
                Vector2d s2 = new Vector2d(c2.getNodes()[0].getX(), c2.getNodes()[0].getY());
                Vector2d e2 = new Vector2d(c2.getNodes()[1].getX(), c2.getNodes()[1].getY());
                if (GeomUtilD.isLineSegmentContained(s2, e2, s1, e1)) {
                    if (GeomUtilD.isSamePoint(s1, s2)) {
                        s1.set(e2);
//                        break;
                    } else
                    if (GeomUtilD.isSamePoint(s1, e2)) {
                        s1.set(s2);
//                        break;
                    } else
                    if (GeomUtilD.isSamePoint(e1, s2)) {
                        e1.set(e2);
//                        break;
                    }else
                    if (GeomUtilD.isSamePoint(e1, e2)) {
                        e1.set(s2);
//                        break;
                    }
                }
            }
            System.out.println("push arrow "+s1+" "+e1);
            addPushArrowSymbol(new OriEdge(new OriVertex(s1), new OriVertex(e1), c1.getType()));
        }
    }
    
    private void setTransformation(Doc prevDoc) {
        transf = new AffineTransform();
        for (int i = 0; i < prevDoc.faces.size(); i++) {
            OriFace oriFace1 = prevDoc.faces.get(i);
            OriHalfedge he1 = oriFace1.halfedges.get(0);
            for (OriFace oriFace2 : doc.faces) {
                if (GeomUtilD.isSameFace(oriFace1, oriFace2)) {
                    OriHalfedge he2 = null;
                    for (OriHalfedge oriHalfedge : oriFace2.halfedges) {
                        if (GeomUtilD.isSamePoint(he1.vertex.preP, oriHalfedge.vertex.preP)) {
                            he2 = oriHalfedge;
                            break;
                        }
                    }
//                    if (he2 == null) {
//                        he1 = oriFace1.halfedges.get(1);
//                        System.out.println("deu nulol!!!"+step.id+" "+he1.vertex.preP);
//                        continue;
//                    }
                    boolean fliped = oriFace1.faceFront != oriFace2.faceFront;
                    Vector2d originBefore = he1.vertex.p;
                    Vector2d vecBefore = new Vector2d();
                    vecBefore.sub(he1.next.vertex.p, originBefore);
                    Vector2d originAfter = he2.vertex.p;
                    Vector2d vecAfter = new Vector2d();
                    vecAfter.sub(he2.next.vertex.p, originAfter);
                    transf.translate(originAfter.x, originAfter.y);
                    if (fliped) {
                        double angle = (vecAfter.y > 0 ? -1 : +1) * vecAfter.angle(new Vector2d(1, 0));
                        transf.rotate(-angle);
                        transf.concatenate(AffineTransform.getScaleInstance(1, -1));
                        transf.rotate(angle);
                    }
                    double angleAfter = Math.atan2(vecAfter.y, vecAfter.x);
                    double angleBefore = Math.atan2(vecBefore.y, vecBefore.x);
                    transf.rotate(angleAfter - angleBefore);
//                    System.out.println("entrou !!!!" + " " + step.id + " " + originBefore + " " + originAfter + " " + vecBefore + " " + vecAfter + " " + angleBefore * 180 / Math.PI + " " + angleAfter * 180 / Math.PI + " " + (angleAfter - angleBefore) * 180 / Math.PI + " " + (oriFace1.faceFront == oriFace2.faceFront));
                    transf.translate(-originBefore.x, -originBefore.y);
//                    Vector2d translated1 = translateCoordinates(he1.vertex.p);
//                    Vector2d translated2 = translateCoordinates(he1.next.vertex.p);
//                    Vector2d translated3 = translateCoordinates(he2.vertex.p);
//                    Vector2d translated4 = translateCoordinates(he2.next.vertex.p);
//                    symbols.add(new SymblDotted((int) translated1.x, (int) translated1.y, (int) translated2.x, (int) translated2.y));
//                    symbols.add(new SymblMountain((int) translated3.x, (int) translated3.y, (int) translated4.x, (int) translated4.y));
                    return;
                }
            }
        }
        for (OriEdge oriEdge1 : prevDoc.edges) {
            for (OriEdge oriEdge2 : doc.edges) {
                if (GeomUtilD.isSamePoint(oriEdge1.sv.preP, oriEdge2.sv.preP)) {
                    if (GeomUtilD.isSamePoint(oriEdge1.ev.preP, oriEdge2.ev.preP)) {
                        Vector2d originBefore = oriEdge1.sv.p;
                        Vector2d vecBefore = new Vector2d();
                        vecBefore.sub(oriEdge1.ev.p, originBefore);
                        Vector2d originAfter = oriEdge2.sv.p;
                        Vector2d vecAfter = new Vector2d();
                        vecAfter.sub(oriEdge2.ev.p, originAfter);
//                        boolean flipped = oriEdge1.;

                        transf.translate(originAfter.x, originAfter.y);
//                        if (flipped) {
//                            double angle = (vecAfter.y > 0 ? -1 : +1) * vecAfter.angle(new Vector2d(1, 0));
//                            transf.rotate(-angle);
//                            transf.concatenate(AffineTransform.getScaleInstance(1, -1));
//                            transf.rotate(angle);
//                        }
                        double angleAfter = Math.atan2(vecAfter.y, vecAfter.x);
                        double angleBefore = Math.atan2(vecBefore.y, vecBefore.x);
                        transf.rotate(angleAfter - angleBefore);
//                    System.out.println("entrou !!!!" + " " + step.id + " " + originBefore + " " + originAfter + " " + vecBefore + " " + vecAfter + " " + angleBefore * 180 / Math.PI + " " + angleAfter * 180 / Math.PI + " " + (angleAfter - angleBefore) * 180 / Math.PI + " " + (oriFace1.faceFront == oriFace2.faceFront));
                        transf.translate(-originBefore.x, -originBefore.y);
//                        Vector2d translated1 = translateCoordinates(he1.vertex.p);
//                        Vector2d translated2 = translateCoordinates(he1.next.vertex.p);
//                        Vector2d translated3 = translateCoordinates(he2.vertex.p);
//                        Vector2d translated4 = translateCoordinates(he2.next.vertex.p);
//                        symbols.add(new SymblDotted((int) translated1.x, (int) translated1.y, (int) translated2.x, (int) translated2.y));
//                        symbols.add(new SymblMountain((int) translated3.x, (int) translated3.y, (int) translated4.x, (int) translated4.y));
                        return;
                    }
                } 
//                else if (GeomUtilD.isSamePoint(oriEdge1.sv.preP, oriEdge2.ev.preP)) {
//                    if (GeomUtilD.isSamePoint(oriEdge1.ev.preP, oriEdge2.sv.preP)) {
//                        Vector2d originBefore = oriEdge1.sv.p;
//                        Vector2d vecBefore = new Vector2d();
//                        vecBefore.sub(oriEdge1.ev.p, originBefore);
//                        Vector2d originAfter = oriEdge2.ev.p;
//                        Vector2d vecAfter = new Vector2d();
//                        vecAfter.sub(oriEdge2.sv.p, originAfter);
//                    }
//                }
            }
        }
    }
}
