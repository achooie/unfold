/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translations;

import Simplification.CP;
import Simplification.Crease;
import Simplification.Node;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.ArrayList;
import javax.vecmath.Vector2d;
import oripa.Constants;
import oripa.DataSet;
import oripa.Doc;
import oripa.OriLineProxy;
import oripa.geom.OriLine;

/**
 *
 * @author akitaya
 */
public class FromToOripa {

    static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<java version=\"1.5.0_02\" class=\"java.beans.XMLDecoder\">\n "
            + "<object class=\"oripa.DataSet\">\n  <void property=\"lines\">\n   "
            + "<array class=\"oripa.OriLineProxy\" length=\"";
    static String footer = "</array>\n</void>\n<void property=\"mainVersion\">\n"
            + "<int>1</int>\n</void>\n<void property=\"paperSize\">\n"
            + "<double>400.0</double>\n</void>\n</object>\n</java>";

    public static CP loadCP(String path) {

        DataSet dataset;
        CP cp = new CP();
        try {
            XMLDecoder dec = new XMLDecoder(
                    new BufferedInputStream(
                    new FileInputStream(path)));
            dataset = (DataSet) dec.readObject();
            dec.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        OriLineProxy lines[] = dataset.getLines();
        for (int i = 0; i < lines.length; i++) {
            Node n1 = cp.addNode(lines[i].getX0(), lines[i].getY0());
            Node n2 = cp.addNode(lines[i].getX1(), lines[i].getY1());
            Crease c = new Crease(n1, n2, lines[i].getType());
            n1.addCrease(c);
            n2.addCrease(c);
            cp.addCrease(c);
        }
        return cp;
    }

    public static void writeCP(CP cp, String path) {

        BufferedWriter write;
        try {
            System.out.println("*** got to write function");
            write = new BufferedWriter(new FileWriter(new File(path)));
            write.write(header);
            ArrayList<Crease> creases = cp.getCreases();
            write.write(creases.size() + "\">\n");

            int i = 0;
            for (Crease c : creases) {
                write.write("<void index=\"" + (i++) + "\">\n");
                write.write("<object class=\"oripa.OriLineProxy\">\n");
                write.write("<void property=\"type\">\n"
                        + "<int>" + c.getType() + "</int>\n</void>\n");
                Node[] n = c.getNodes();
                write.write("<void property=\"x0\">+\n<double>" + n[0].getX() + "</double></void>");
                write.write("<void property=\"x1\">+\n<double>" + n[1].getX() + "</double></void>");
                write.write("<void property=\"y0\">+\n<double>" + n[0].getY() + "</double></void>");
                write.write("<void property=\"y1\">+\n<double>" + n[1].getY() + "</double></void>");
                write.write("</object></void> ");

            }

            write.write(footer);

            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static Doc getDoc(CP cp) {
        Doc doc = new Doc(Constants.DEFAULT_PAPER_SIZE);
        doc.lines.clear();
        ArrayList<Crease> creases = cp.getCreases();
        for (int i = 0; i < creases.size(); i++) {
            Crease c = creases.get(i);
            OriLine line = new OriLine(
                    new Vector2d(c.getNodes()[0].getX(), c.getNodes()[0].getY()),
                    new Vector2d(c.getNodes()[1].getX(), c.getNodes()[1].getY()),
                    c.getType());
            doc.lines.add(line);
        }
        return doc;
    }

    public static CP getCP(Doc doc) {
        CP cp = new CP();
        for (OriLine oriLine : doc.lines) {
            Node n1 = cp.addNode(oriLine.p0.x, oriLine.p0.y);
            Node n2 = cp.addNode(oriLine.p1.x, oriLine.p1.y);
            Crease c = new Crease(n1, n2, oriLine.type);
            n1.addCrease(c);
            n2.addCrease(c);
            cp.addCrease(c);
        }
        return cp;
    }
}
