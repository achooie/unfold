/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Simplification;

import java.util.ArrayList;

/**
 *
 * @author akitaya
 */
public class Maneuver {

    int nodesNumber;
    boolean[] checkedNodes;
    ArrayList<int[]> beforeGraph;
    ArrayList<int[]> afterGraph;
    ArrayList<Matching> matchings;
    Matching test;
    String description;

    public Maneuver(int nodesNumber, ArrayList<int[]> beforeGraph, ArrayList<int[]> afterGraph, String description) {
        this.nodesNumber = nodesNumber;
        this.beforeGraph = beforeGraph;
        this.afterGraph = afterGraph;
        this.description = description;
    }

    public ArrayList<Matching> findMatch(CP cp, ArrayList<ReflectionPath> reflectionPaths) {
//        System.out.println("entrou "+description);
        matchings = new ArrayList<>();
        for (int i = 0; i < cp.nodes.size(); i++) {
            Node node = cp.nodes.get(i);
            if (node.isEdge()) {
                continue;
            }

            checkedNodes = new boolean[nodesNumber];
            test = new Matching(nodesNumber, node);
            checkNode(node);
//            if (checkNode(node)) {
//                
//                System.out.println("node   " + node.x + " , " + node.y);
//                if(validateMatch(reflectionPaths)){
//                    return true;
//                }
//            }
        }
        
        // remove invalid maneuvers
        for (int i = 0; i < matchings.size(); i++) {
            Matching matching = matchings.get(i);
            if (matching.validateMatch1(reflectionPaths, matchings)) {
                if (matching.isReflection) {
                    matchings.remove(i);
                    i--;
                }
//                System.out.println("valid maneuver" + matching+" "+matching.isReflection+" "+matching.reflectionMatching);
            } else {
                if (matching.isReflection) {
                    if (matchings.remove(matching.reflectionMatching)) {
                        i--;
                    }
                }
//                System.out.println("invalid maneuver" + matching + " " + matching.isReflection + i + " " + matching.reflectionMatching + " " + matchings.indexOf(matching.reflectionMatching));
                matchings.remove(i);
                i--;
            }
        }

        return matchings;
    }

    private boolean checkNode(Node n) {
        checkedNodes[0] = true;
        for (int i = 0; i < n.creases.size(); i++) {
            //check for clockwise and counterclockwise in case of assimetrical maneuvers
            for (int rotation = 0; rotation < 2; rotation++) {
                boolean matchesConections = true;
                if ((n.creases.get(i).type == Crease.MOUNTAIN && beforeGraph.get(0)[0] < 0)
                        || (n.creases.get(i).type == Crease.VALLEY && beforeGraph.get(0)[0] > 0)) {
                    // Mountain = minus signal
                    test.mountainIsMinus = true;
                } else {
                    // Mountain = plus signal
                    test.mountainIsMinus = false;
                }

                if (rotation == 0) {
                    for (int j = 0; j < beforeGraph.get(0).length; j++) {
                        if ((test.mountainIsMinus ? beforeGraph.get(0)[j] < 0 : beforeGraph.get(0)[j] > 0)
                                ? n.creases.get((i + j) % n.creases.size()).type == Crease.MOUNTAIN
                                : n.creases.get((i + j) % n.creases.size()).type == Crease.VALLEY) {
                            // checked for conection ; 
                        } else {
                            matchesConections = false;
                            break;
                        }

                    }
                } else {
                    for (int j = 0; j < beforeGraph.get(0).length; j++) {
                        if ((test.mountainIsMinus ? beforeGraph.get(0)[j] < 0 : beforeGraph.get(0)[j] > 0)
                                ? n.creases.get((i - j + n.creases.size()) % n.creases.size()).type == Crease.MOUNTAIN
                                : n.creases.get((i - j + n.creases.size()) % n.creases.size()).type == Crease.VALLEY) {
                            // checked for conection ; 
                        } else {
                            matchesConections = false;
                            break;
                        }

                    }
                }

                if (matchesConections) {
                    test.creaseMapping[0] = new Crease[beforeGraph.get(0).length];
                    if (rotation == 0) {
                        for (int j = 0; j < beforeGraph.get(0).length; j++) {
                            int k = Math.abs(beforeGraph.get(0)[j]);
                            Crease child = n.creases.get((i + j) % n.creases.size());
                            test.nodeMapping[k] = child.getOpposingNode(n);
                            test.creaseMapping[0][j] = child;
//                            n.equals(n.creases.get((i + j) % n.creases.size()).getNodes()[0])
//                            ? n.creases.get((i + j) % n.creases.size()).getNodes()[1]
//                            : n.creases.get((i + j) % n.creases.size()).getNodes()[0];
                        }
                    } else {
                        for (int j = 0; j < beforeGraph.get(0).length; j++) {
                            int k = Math.abs(beforeGraph.get(0)[j]);
                            Crease child = n.creases.get((i - j + n.creases.size()) % n.creases.size());
                            test.nodeMapping[k] = child.getOpposingNode(n);
                            test.creaseMapping[0][j] = child;
                        }
                    }

                    boolean matchesClisdren = true;
//                    System.out.println(Unfolder.i + "-pivot : " + n.x + ", " + n.y + " " + description);
                    for (int j = 0; j < beforeGraph.get(0).length; j++) {
                        int k = Math.abs(beforeGraph.get(0)[j]);
                        if (!checkChildren(k, test.creaseMapping[0][j])) {
                            matchesClisdren = false;
                            break;
                        }
                    }
//                    System.out.println("matches conection " + matchesClisdren);
                    if (matchesClisdren) {
                        matchings.add(test.clone());
                        test = new Matching(nodesNumber, n);
                        checkedNodes = new boolean[nodesNumber];
                        checkedNodes[0] = true;
//                    return true;
                    }

                }
            }
        }
        return false;
    }

    private boolean checkChildren(int n, Crease parent) {
//        System.out.println("    check children " + n);
        if (checkedNodes[n]) {
            return true;
        }
        if (beforeGraph.get(n) == null) {
            test.terminalCreases.add(parent);
            test.terminalNodes.add(test.nodeMapping[n]);
            return true;
        }
        Node node = test.nodeMapping[n];
        ArrayList<Integer> justMapped = new ArrayList<>();

        for (int i = 0; i < node.creases.size(); i++) {
            Crease crease = node.creases.get(i);
            if (crease.equals(parent)) {
                for (int rotation = 0; rotation < 2; rotation++) {
//                    System.out.println("    rotation: " + rotation);
                    boolean connectionsChecked = true;
                    if (rotation == 0) {
                        for (int j = 0; j < beforeGraph.get(n).length; j++) {
                            if ((test.mountainIsMinus ? beforeGraph.get(n)[j] < 0 : beforeGraph.get(n)[j] > 0)
                                    ? node.creases.get((i - 1 - j + node.creases.size()) % node.creases.size()).type
                                    == Crease.MOUNTAIN
                                    : node.creases.get((i - 1 - j + node.creases.size()) % node.creases.size()).type
                                    == Crease.VALLEY) {
                                //checked connection
                            } else {
                                connectionsChecked = false;
                                break;
                            }
                        }
                    } else {
                        for (int j = 0; j < beforeGraph.get(n).length; j++) {
                            if ((test.mountainIsMinus ? beforeGraph.get(n)[j] < 0 : beforeGraph.get(n)[j] > 0)
                                    ? node.creases.get((i + j + 1) % node.creases.size()).type
                                    == Crease.MOUNTAIN
                                    : node.creases.get((i + j + 1) % node.creases.size()).type
                                    == Crease.VALLEY) {
                                //checked connection
                            } else {
                                connectionsChecked = false;
                                break;
                            }
                        }
                    }
                    if (connectionsChecked) {
//                        System.out.println("    connections checked " + node.x + ", " + node.y);
                        // checkes if the child is already mapped
                        boolean nodeMappingChecked = true;
                        test.creaseMapping[n] = new Crease[beforeGraph.get(n).length];
                        if (rotation == 0) {
                            for (int j = 0; j < beforeGraph.get(n).length; j++) {
                                //now check the node mapping
                                int k = Math.abs(beforeGraph.get(n)[j]);
                                Crease child = node.creases.get((i - 1 - j + node.creases.size()) % node.creases.size());
                                if (test.nodeMapping[k] == null) {
                                    //node not mapped yet
                                    test.nodeMapping[k] = child.getOpposingNode(node);
                                    justMapped.add(k);
                                } else {
                                    //node already mapped
                                    if (!test.nodeMapping[k].equals(child.getOpposingNode(node))) {
                                        nodeMappingChecked = false;
                                        break;
                                    }
                                }
                                test.creaseMapping[n][j] = child;
                            }
                        } else {
                            for (int j = 0; j < beforeGraph.get(n).length; j++) {
                                //now check the node mapping
                                int k = Math.abs(beforeGraph.get(n)[j]);
                                Crease child = node.creases.get((i + j + 1) % node.creases.size());
                                if (test.nodeMapping[k] == null) {
                                    //node not mapped yet
                                    test.nodeMapping[k] = child.getOpposingNode(node);
                                    justMapped.add(k);
                                } else {
                                    //node already mapped
                                    if (!test.nodeMapping[k].equals(child.getOpposingNode(node))) {
                                        nodeMappingChecked = false;
                                        break;
                                    }
                                }
                                test.creaseMapping[n][j] = child;
                            }
                        }
//                        System.out.println("        node mapping " + nodeMappingChecked);
                        checkedNodes[n] = true;
                        if (nodeMappingChecked) {
                            boolean matchesClisdren = true;
                            for (int j = 0; j < beforeGraph.get(n).length; j++) {
                                int k = Math.abs(beforeGraph.get(n)[j]);
                                if (!checkChildren(k, test.creaseMapping[n][j])) {
                                    matchesClisdren = false;
                                    break;
                                }
                            }
                            if (matchesClisdren) {
//                                System.out.println("    returned true :" + node.x + ", " + node.y);
                                return true;
                            }
                        }
                        // if this point is reached, the matching was unsuccessful
                        // and the children must be removed from the mapping
                        checkedNodes[n] = false;
                        for (Integer jM : justMapped) {
                            test.nodeMapping[jM] = null;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    public void unfold(CP cp, Matching matching) {
//        try {
////            System.out.println("cp creases count: " + description + cp.creases.size());
//            oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(cp)), "results/before" + description + ".opx");
//        } catch (Exception ex) {
//        }
//        System.out.println(Unfolder.i + " csize " + matching.removableReflections.size() + " " + matching + " " + matching.reflectionMatching);

        //unfolds the given matching 
        for (int i = 0; i < matching.creaseMapping.length; i++) {
            //for each crease of the matching 
            Crease[] creases = matching.creaseMapping[i];
            if (creases == null) {
                //in case of terminal nodes;
                continue;
            }
            for (int j = 0; j < creases.length; j++) {
                Crease crease = creases[j];
                if (matching.terminalCreases.indexOf(crease) == -1) {
                    if (afterGraph.get(i) == null) {
                        cp.removeCrease(crease);
                    } else {
                        boolean remainsFolded = false;
                        for (int k = 0; k < afterGraph.get(i).length; k++) {
                            if (afterGraph.get(i)[k] == beforeGraph.get(i)[j]) {
                                remainsFolded = true;
                                break;
                            }
                        }
                        if (!remainsFolded) {
                            cp.removeCrease(crease);
                        }
                    }
                }
            }
        }
        if (matching.removableReflections != null) {
            for (ReflectionPath reflPath : matching.removableReflections) {
                for (Crease crease : reflPath.creases) {
//                    System.out.println(Unfolder.i + " " + description + " " + crease + " mountain:" + matching.mountainIsMinus + " " + matching);
                    cp.removeCrease(crease);
                }
            }
        }

        if (matching.isReflection) {
            return;
        }
        //if the matching has a reflection but is not a reflection itself remove
        //also the creases of the reflection
        if (matching.reflectionMatching != null) {
            unfold(cp, matching.reflectionMatching);
        }
//        System.out.println("before adding creases " + matching.reflectionMatching + " " + description);
//        //////
//        try {
//            oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(cp)), "results/errprev.opx");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        ///////

        //add new Creases
        for (int i = 0; i < afterGraph.size(); i++) {
            if (afterGraph.get(i) == null) {
                continue;
            }
            for (int j = 0; j < afterGraph.get(i).length; j++) {
                int k = afterGraph.get(i)[j];
                if (Math.abs(k) >= nodesNumber) {
//                    System.out.println("mountain is minus " + matching.mountainIsMinus);
                    if (matching.mountainIsMinus) {
                        if (matching.reflectionMatching != null) {
                            addNewPairOfCreases(cp.nodes.get(cp.nodes.indexOf(matching.nodeMapping[i])),
                                    cp.nodes.get(cp.nodes.indexOf(matching.reflectionMatching.nodeMapping[i])), cp, (k > 0)
                                    ? Crease.VALLEY : Crease.MOUNTAIN);
                        } else {
                            propagateNewCreases(cp.nodes.get(cp.nodes.indexOf(matching.nodeMapping[i])), cp, (k > 0)
                                    ? Crease.VALLEY : Crease.MOUNTAIN);
                        }
                    } else {
                        if (matching.reflectionMatching != null) {
                            addNewPairOfCreases(cp.nodes.get(cp.nodes.indexOf(matching.nodeMapping[i])),
                                    cp.nodes.get(cp.nodes.indexOf(matching.reflectionMatching.nodeMapping[i])), cp, (k < 0)
                                    ? Crease.VALLEY : Crease.MOUNTAIN);
                        } else {
                            propagateNewCreases(cp.nodes.get(cp.nodes.indexOf(matching.nodeMapping[i])), cp, (k < 0)
                                    ? Crease.VALLEY : Crease.MOUNTAIN);
                        }
                    }
                }
            }

        }
//        try {
////            System.out.println("cp creases count: " + cp.creases.size());
//            oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(cp)), "results/returnednull.opx");
//        } catch (Exception ex) {
//        }
    }

    static void propagateNewCreases(Node n, CP cp, int type) {
//        System.out.println("propagate new crease");
        Node newNode = addNewCrease(n, cp, type);
        for (; !newNode.isEdge(); newNode = addNewCrease(newNode, cp, type)) {
            type = (type == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN);
        }
//        if (!newNode.isEdge()) {
//            propagateNewCreases(newNode, cp, type == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN);
//        }
    }

    static Node addNewCrease(Node n, CP cp, int type) {
        int maxAnglePosition = 0;
        double bestOddSum = 0;
        double[] angles = new double[n.creases.size()];
        for (int i = 0; i < n.creases.size() - 1; i++) {
            angles[i] = Math.abs(n.creases.get((i + 1)).getAngle(n) - n.creases.get(i).getAngle(n));
//            System.out.println(" Angle " + n.creases.get(i).getAngle(n) * 180 / Math.PI + " " + angles[i]);

        }
//        System.out.println("addNewCrease "+n+" "+description);
//                    //////
//                    try {
//                        oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(cp)), "results/err.opx");
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    ///////
//              System.out.println("kkk "+ angles.length+ " "+ n+" "+n.creases);      
        angles[angles.length - 1] = Math.PI * 2
                - Math.abs(n.creases.get((angles.length - 1)).getAngle(n) - n.creases.get(0).getAngle(n));

//        System.out.println(type + " Angle " + n.creases.get(n.creases.size() - 1).getAngle(n) * 180 / Math.PI + " " + angles[n.creases.size() - 1]);

        for (int i = 0; i < angles.length; i++) {
            double oddSum = 0;
            for (int j = 0; j < (angles.length - 1) / 2; j++) {
                oddSum += angles[(i + 1 + j * 2) % angles.length];
            }
//            System.out.println("oddSum: " + oddSum * 180 / Math.PI);
            if (oddSum > bestOddSum && oddSum < Math.PI - Crease.TOLERANCE
                    && angles[i] - Math.PI + oddSum > Crease.TOLERANCE) {
                bestOddSum = oddSum;
                maxAnglePosition = i;
            }
        }

//        System.out.println(" maxAngle " + n.creases.get(maxAnglePosition).getAngle(n) * 180 / Math.PI + " " + maxAnglePosition);
        double newAngle = n.creases.get(maxAnglePosition).getAngle(n) + Math.PI - bestOddSum;
        if (newAngle < -Math.PI) {
            newAngle += Math.PI * 2;
        } else if (newAngle > Math.PI) {
            newAngle -= Math.PI * 2;
        }
//        System.out.println("new angle " + (newAngle * 180 / Math.PI));

        Crease newCrease = new Crease(new Node(n.x + Math.cos(newAngle) * 1000, n.y
                + Math.sin(newAngle) * 1000), n, type);
//        System.out.println("new crease " + newCrease.nodes[0].x + " " + newCrease.nodes[0].y);

        Node newNode = null;
        Crease intercepted = null;
        double minDistance = 1000;
        for (Crease crease : cp.creases) {
            if (crease.type == Crease.AUX) {
                continue;
            }
            Node newNodeCandidate = crease.getCrossPoint(newCrease);
            if (newNodeCandidate != null && !n.equals(newNodeCandidate)) {
//                System.out.println("new node candidate " + newNodeCandidate.x + ", " + newNodeCandidate.y + " " + newNodeCandidate.equals(n));
                double dist = n.distanceFrom(newNodeCandidate);
                if (dist < minDistance) {
                    newNode = newNodeCandidate;
                    intercepted = crease;
                    minDistance = dist;
                }
            }
        }

//        System.out.println("new node " + newNode.x + ", " + newNode.y + " " + type);
        cp.splitCrease(intercepted, newNode);
        cp.addNode(newNode);
        newCrease.nodes[0] = newNode;
        newNode.addCrease(newCrease);
        n.addCrease(newCrease);
        cp.addCrease(newCrease);
        return newNode;
    }

    public String getDescription() {
        return description;
    }

    private void addNewPairOfCreases(Node n1, Node n2, CP cp, int type) {
//        System.out.println("pair of creases : " + n1.x + ", " + n1.y + " " + n2.x + ", " + n2.y);
        Node newNode1 = addNewCrease(n1, cp, type);
        int newType = type;
        for (; !newNode1.isEdge(); newNode1 = addNewCrease(newNode1, cp, newType)) {
//            System.out.println("pair of creases new node: " + newNode1.x + ", " + newNode1.y);
            newType = newType == Crease.MOUNTAIN ? Crease.VALLEY : Crease.MOUNTAIN;
            if (newNode1.equals(n2)) {
                return;
            }
        }

        //if this point is reached, n1 and n2 does not share a reflection
        //so propagate crease from n2;
        propagateNewCreases(n2, cp, type == Crease.MOUNTAIN
                ? Crease.VALLEY : Crease.MOUNTAIN);

    }

    @Override
    public String toString() {
        String returnValue = "Maneuver " + description + "\nbefore";
        for (int[] is : beforeGraph) {
            returnValue += "\n" + (is == null ? "null" : "");
            if (is == null) {
                continue;
            }
            for (int i = 0; i < is.length; i++) {
                returnValue += is[i] + " ";
            }
        }
        returnValue += "\nafter";
        for (int[] is : afterGraph) {
            returnValue += "\n" + (is == null ? "null" : "");
            if (is == null) {
                continue;
            }
            for (int i = 0; i < is.length; i++) {
                returnValue += is[i] + " ";
            }
        }
        return returnValue + "\nend Maneuver";
    }
}
