package translations;

import diagram.DiagramSymbol;
import GUI.FoldedModelScreen;
import diagram.CanvasSVG;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.vecmath.Vector2d;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import oripa.Doc;
import oripa.geom.OriFace;
import oripa.geom.OriHalfedge;

public class ExporterSVG {

    static final int size = 1000;
    final static String head =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
            + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\"\n"
            + "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\"\n"
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"\n"
            + " width=\"" + size + "px\" height=\"";
    final static String end = "</svg>";
    final static String defs = "<defs id=\"defs466\">\n"
            + "<marker inkscape:stockid=\"Arrow2Mend\" orient=\"auto\" refY=\"0.0\" \n"
            + "refX=\"0.0\" id=\"Arrow2Mend\" style=\"overflow:visible;\">\n"
            + "<path id=\"path4246\" style=\"fill-rule:evenodd;stroke-width:0.62500000;stroke-linejoin:round;\" \n"
            + "d=\"M 8.7185878,4.0337352 L -2.2072895,0.016013256 L 8.7185884,-4.0017078 C 6.9730900,-1.6296469 6.9831476,1.6157441 8.7185878,4.0337352 z \" \n"
            + "transform=\"scale(0.6) rotate(180) translate(0,0)\" /></marker>\n"
            + "<marker \nstockid  = \"PushArrow\" \norient  = \"auto\" \nrefY  = \"0.0\" \nrefX  = \"0.0\" \nid  = \"PushArrow\" \nstyle  = \"overflow:visible;\"> \n"
            + "<path \nstyle  = \"fill:#FFFFFF;stroke:#000000;stroke-width:1;stroke-linecap:butt;\n"
            + "stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" \n"
            + "d  = \"M -0.67959949,0 -7.617562,-7.6952 -7.661756,-2.7836 "
            + "c -4.098649,0.067 -7.910611,-0.8767 -11.23229,-2.6497 1.008667,2.8513 2.137562,4.1384 3.12203,5.1274 "
            + "-1.023414,1.095 -2.12133,2.4352 -3.169334,5.1129 4.077885,-2.0537 7.229117,-2.6113 11.1876805,-2.6433 l 0.047303,4.7679 z\" \n"
            + "id  = \"path3084\" inkscape:connector-curvature  = \"0\" sodipodi:nodetypes  = \"ccccccccc\"/></marker>\n"
            + "</defs>\n";
    final static String gradient =
            " <linearGradient id=\"Gradient1\" x1=\"20%\" y1=\"0%\" x2=\"80%\" y2=\"100%\">\n"
            + " <stop offset=\"5%\" stop-color=\"#DDEEFF\" />\n"
            + " <stop offset=\"95%\" stop-color=\"#7788FF\" />\n"
            + " </linearGradient>\n"
            + " <linearGradient id=\"Gradient2\" x1=\"20%\" y1=\"0%\" x2=\"80%\" y2=\"100%\">\n"
            + " <stop offset=\"5%\" stop-color=\"#FFFFEE\" />\n"
            + " <stop offset=\"95%\" stop-color=\"#DDDDDD\" />\n"
            + " </linearGradient>\n";
//    final static String valeyLineStart = " <line style=\"stroke:black; stroke-width:4; stroke-dasharray:20, 10;\" ";
//    final static String mountainLineStart = " <line style=\"stroke:black; stroke-width:4; stroke-dasharray:20, 5, 2, 5, 2, 5;\" ";
//    final static String dottedLineStart = " <line style=\"stroke:black; stroke-width:4; stroke-dasharray:5, 10;\" ";
//    final static String vArrowStart = "<line style=\"fill:none;stroke:black;stroke-width:4;"
//            + "marker-end:url(#Arrow2Mend);\"  ";
    final static String polygonStart = "<path style=\"fill:url(#Gradient1);"
            + "stroke:#0000ff;stroke-width:2px;stroke-linecap:butt;stroke-linejoin:miter;"
            + "stroke-opacity:1;fill-opacity:1.0\" d=\"M ";
    final static String polygonStart2 = "<path style=\"fill:url(#Gradient2);"
            + "stroke:#0000ff;stroke-width:2px;stroke-linecap:butt;stroke-linejoin:miter;"
            + "stroke-opacity:1;fill-opacity:1.0\" d=\"M ";

    public static void export(Component[] sequence, String filepath) throws Exception {

        double scale = (size - 5) / ((FoldedModelScreen) sequence[0]).getDoc().size;
        double center = size / 2;
        FileWriter fw = new FileWriter(filepath);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(head);
            bw.write(size * sequence.length + "px\"\n"
                    + " viewBox=\"0 0 " + size + " " + size + "\" >\n");
            bw.write(defs);
            bw.write(gradient);

            for (int l = 0; l < sequence.length; l++) {
                Doc doc = ((FoldedModelScreen) sequence[l]).getDoc();
                ArrayList<DiagramSymbol> symbols = ((FoldedModelScreen) sequence[l]).getSymbols();
                ArrayList<ArrayList<DiagramSymbol>> autoSymbols = ((FoldedModelScreen) sequence[l]).getAutoSymbols();
                double localScale = Math.min(
                        600 / (doc.foldedBBoxRB.x - doc.foldedBBoxLT.x),
                        600 / (doc.foldedBBoxRB.y - doc.foldedBBoxLT.y)) * 0.95;
                localScale *= .8;

                Vector2d maxV = new Vector2d(-Double.MAX_VALUE, -Double.MAX_VALUE);
                Vector2d minV = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
                Vector2d modelCenter = new Vector2d();
                for (OriFace face : doc.faces) {
                    for (OriHalfedge he : face.halfedges) {
                        maxV.x = Math.max(maxV.x, he.vertex.p.x);
                        maxV.y = Math.max(maxV.y, he.vertex.p.y);
                        minV.x = Math.min(minV.x, he.vertex.p.x);
                        minV.y = Math.min(minV.y, he.vertex.p.y);
                    }
                }
                modelCenter.x = (maxV.x + minV.x) / 2;
                modelCenter.y = (maxV.y + minV.y) / 2;

                ArrayList<OriFace> sortedFaces = doc.sortedFaces;

                boolean isFlipped = ((FoldedModelScreen) sequence[l]).isFlipped;
                for (int i = 0; i < sortedFaces.size(); i++) {
                    OriFace face = isFlipped ? sortedFaces.get(i)
                            : sortedFaces.get(sortedFaces.size() - i - 1);
                    java.util.ArrayList<Vector2d> points = new java.util.ArrayList<>();
                    for (OriHalfedge he : face.halfedges) {

                        if (he.vertex.p.x > maxV.x) {
                            throw new Exception("Size of vertices exceeds maximum");
                        }

                        Vector2d v1 = new Vector2d(he.vertex.tmpVec);
                        v1.scale(4.0 / he.vertex.tmpInt / sortedFaces.size());
                        System.out.println(" acresc1 " + v1 + " " + he.vertex.tmpInt);
                        v1.add(he.vertex.p);
                        Vector2d v2 = new Vector2d(he.next.vertex.tmpVec);
                        v2.scale(4.0 / he.next.vertex.tmpInt / sortedFaces.size());
                        System.out.println(" acresc1 " + v2 + " " + he.next.vertex.tmpInt);
                        v2.add(he.next.vertex.p);
                        double x1 = (v1.x - modelCenter.x) * scale + center;
                        double y1 = (v1.y - modelCenter.y) * scale + (l - sequence.length / 2.0 + 1) * size;
                        double x2 = (v2.x - modelCenter.x) * scale + center;
                        double y2 = (v2.y - modelCenter.y) * scale + (l - sequence.length / 2.0 + 1) * size;
                        if (!points.contains(new Vector2d(x1, y1))) {
                            points.add(new Vector2d(x1, y1));
                        }
                        if (!points.contains(new Vector2d(x2, y2))) {
                            points.add(new Vector2d(x2, y2));
                        }
                    }
                    if ((!face.faceFront && isFlipped)
                            || (face.faceFront && !isFlipped)) {
                        bw.write(polygonStart);
                    } else {
                        bw.write(polygonStart2);
                    }
                    for (Vector2d p : points) {
                        bw.write(p.x + "," + p.y + " ");
                    }
                    bw.write(" z\" />\n");
                    for (DiagramSymbol diagramSymbol : autoSymbols.get(isFlipped ? i
                            : sortedFaces.size() - i - 1)) {
                        bw.write(diagramSymbol.toSVG(scale, localScale, center, (l - sequence.length / 2.0 + 1) * size, isFlipped));
                    }
                }
                for (DiagramSymbol diagramSymbol : symbols) {
                    bw.write(diagramSymbol.toSVG(scale, localScale, center, (l - sequence.length / 2.0 + 1) * size, false));
                }
            }
            bw.write(end);
        }

    }

    public static void exportSVG(Component[] sequence, String filePath) throws UnsupportedEncodingException, TransformerConfigurationException, TransformerException, IOException {

        Document document = CanvasSVG.createNewDocument(size, size * sequence.length);

        FileWriter fw = new FileWriter(filePath);
//        ((CanvasSVG) sequence[0]).getSVGDocument().;
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        for (int i = 0; i < sequence.length; i++) {
            CanvasSVG canvasi = ((CanvasSVG) sequence[i]);
            String transfScaleGroup = "translate(" + size / 2 + "," + (i + 0.5) * size + ") scale(" + size / 600 + ") ";
            Vector2d modelCenter = canvasi.modelCenter;
            if (!canvasi.isFlipped) {
                transfScaleGroup += "translate(" + -modelCenter.x + "," + -modelCenter.y + ")";
            } else {
                transfScaleGroup += "translate(" + modelCenter.x + "," + -modelCenter.y + ")";
            }

            Element scaleGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
            Node g = document.importNode(canvasi.getGroup(), true);
            scaleGroup.appendChild(g);
            scaleGroup.setAttributeNS(null, "transform", transfScaleGroup);
            document.getDocumentElement().appendChild(scaleGroup);
        }

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(fw);
        transformer.transform(source, result);
        fw.close();
    }
}
