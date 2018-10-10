/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simplification;

import java.util.ArrayList;
import origamid.MountainValey;
import static origamid.Unfolder.i;
import oripa.DataSet;
import translations.FromToOripa;
import oripa.ExporterXML;

/**
 *
 * @author akitaya
 */
public class CP {

    ArrayList<Crease> creases;
    ArrayList<Node> nodes;
    public ArrayList<ReflectionPath> reflectionPaths;
    public int localComplexity;
    public int generalComplexity;
    private ExporterXML exporter = new ExporterXML();

    public CP() {
        this.creases = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public void addCrease(Crease c) {
        this.creases.add(c);
    }

    public void removeCrease(Crease c) {
        int index = creases.indexOf(c);
        if (index == -1) {
            return;
        }
        Crease localC = creases.remove(index);
        Node[] localNodes = localC.getNodes();
        localNodes[0].getCreases().remove(localC);
        localNodes[1].getCreases().remove(localC);
    }

    public void addNode(Node n) {
        if (nodes.indexOf(n) == -1) {
            nodes.add(n);
        }
    }

    public Node addNode(double x, double y) {
        for (int i = 0; i < nodes.size(); i++) {
            if (Math.abs(nodes.get(i).getX() - x) < Crease.TOLERANCE
                    && Math.abs(nodes.get(i).getY() - y) < Crease.TOLERANCE) {
                return nodes.get(i);
            }
        }
        Node n = new Node(x, y);
        nodes.add(n);
        return n;
    }

    public Node getNodeAt(double x, double y) {
        for (int i = 0; i < nodes.size(); i++) {
            if (Math.abs(nodes.get(i).getX() - x) < Crease.TOLERANCE
                    && Math.abs(nodes.get(i).getY() - y) < Crease.TOLERANCE) {
                return nodes.get(i);
            }
        }
        return null;
    }

    public Node getNodeAt(Node n) {
        for (int i = 0; i < nodes.size(); i++) {
            if (Math.abs(nodes.get(i).getX() - n.x) < Crease.TOLERANCE
                    && Math.abs(nodes.get(i).getY() - n.y) < Crease.TOLERANCE) {
                return nodes.get(i);
            }
        }
        return null;
    }

    public ArrayList<Crease> getCreases() {
        return creases;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void removeRedundantNodes() {
        int i = 0;
        while (i < nodes.size()) {
            Node n = nodes.get(i);
            ArrayList<Crease> c = n.creases;
//            System.out.println(c.size()+" asdfffffffffff "+nodes.size() +" ffffffffffffffff "+c.get(1).getAngle(n)/Math.PI*180+" "+c.get(0).getAngle(n)*180/Math.PI);
            if (c.size() == 2
                    && Math.abs(c.get(1).getAngle(n) - c.get(0).getAngle(n) - Math.PI) < 1E-6) {
                mergeCrease(c.get(0), c.get(1), n);
                i--;
            }
            i++;
        }
    }

    public void mergeCrease(Crease c1, Crease c2, Node n) {
        Node n1 = c1.nodes[0].equals(n) ? c1.nodes[1] : c1.nodes[0];
        Node n2 = c2.nodes[0].equals(n) ? c2.nodes[1] : c2.nodes[0];
        c1.nodes[0] = n1;
        c1.nodes[1] = n2;
        c1.angle = Math.atan2(n2.y - n1.y, n2.x - n1.x);
        n2.deleteCrease(c2);
        n2.addCrease(c1);
        this.creases.remove(c2);
        this.nodes.remove(n);
    }

    public boolean equals(CP other) {
        ArrayList<Crease> otherCreases = new ArrayList();
        otherCreases.addAll(other.creases);
        for (Crease c1 : this.creases) {
            boolean haveEquivalent = false;
            for (Crease c2 : otherCreases) {
                if (c1.equals(c2)) {
                    haveEquivalent = true;
                    otherCreases.remove(c2);
                    break;
                }
            }
            if (!haveEquivalent) {
                return false;
            }
        }
        if (!otherCreases.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public CP clone() {
        CP clone = new CP();
        for (Crease crease : creases) {
            Node n1 = clone.addNode(crease.getNodes()[0].getX(), crease.getNodes()[0].getY());
            Node n2 = clone.addNode(crease.getNodes()[1].getX(), crease.getNodes()[1].getY());
            Crease c = new Crease(n1, n2, crease.type);
            c.reflPath = crease.reflPath;
            n1.addCrease(c);
            n2.addCrease(c);
            clone.addCrease(c);
        }
        return clone;
    }

    public boolean isUnfolded() {
        for (Crease crease : creases) {
            if (crease.getType() == Crease.MOUNTAIN || crease.getType() == Crease.VALLEY) {
                return false;
            }
        }
        return true;
    }

    public void splitCrease(Crease c, Node n) {
        if (c==null) {
            //////
            try {
                exporter.export(new DataSet(FromToOripa.getDoc(this)), "results/aaa" + i++ + ".opx");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ///////
        }
        Node n1 = c.nodes[0];
        Node n2 = c.nodes[1];
        if (n1.equals(n) || n2.equals(n)) {
            return;
        }
        int type = c.type;
        removeCrease(c);
        Crease new1 = new Crease(n1, n, type);
        Crease new2 = new Crease(n, n2, type);
        n1.addCrease(new1);
        n2.addCrease(new2);
        n.addCrease(new1);
        n.addCrease(new2);
        addCrease(new1);
        addCrease(new2);
    }

    public void exportResult(String s) {
        //////
        try {
            exporter.export(new DataSet(FromToOripa.getDoc(this)), "results/" + s + ".opx");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ///////
    }

    public void calculateComplexity() {
        if (generalComplexity == 0) {
            reflectionPaths = MountainValey.getReflectionPaths(this);
            generalComplexity = reflectionPaths.size();
            localComplexity = 0;
            for (int i = 0; i < generalComplexity; i++) {
                ReflectionPath reflPath = reflectionPaths.get(i);
                if (!reflPath.ends[0].isEdge()) {
                    reflPath.ends[0].localComplexity++;
                }
                if (!reflPath.ends[1].isEdge()) {
                    reflPath.ends[1].localComplexity++;
                }
            }
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                if (node.localComplexity > 2) {
                    localComplexity += node.localComplexity - 2;
                }
            }
        }
    }
}
