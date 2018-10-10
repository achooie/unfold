/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package origamid;

import Simplification.ReflectionPath;
import Simplification.ExecutionNode;
import Simplification.CP;
import Simplification.Crease;
import Simplification.Matching;
import Simplification.Maneuver;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import oripa.DataSet;
import translations.FromToOripa;
import translations.ManeuverIO;
import oripa.ExporterXML;

/**
 *
 * @author akitaya
 */
public class Unfolder {

    ExecutionNode lastSetep;
    ExecutionNode firstSetep;
    ArrayList<ExecutionNode> steps;
    public int nodeNumeber, arcNumber;
    //experimental
    static ArrayList<Maneuver> maneuvers;
    public static int i;
    private ExporterXML exporter = new ExporterXML();

    public Unfolder(String filePath) {

        lastSetep = new ExecutionNode(FromToOripa.loadCP(filePath));
        steps = new ArrayList<>();
        steps.add(lastSetep);
        unfold(lastSetep);
    }

    public Unfolder(String filePath, boolean developerView) {
        lastSetep = new ExecutionNode(FromToOripa.loadCP(filePath));

    }

    public ExecutionNode getFirstStep() {
        return firstSetep;
    }

    public ExecutionNode getLastStep() {
        return lastSetep;
    }

    private ExecutionNode unfold(ExecutionNode currentStep) {
        if (currentStep.stepCp.isUnfolded()) {
            firstSetep = currentStep;
            return currentStep;
        }
        boolean simp = false;
        ExecutionNode returnValue = null;
        ArrayList<ReflectionPath> reflectionPaths =
                MountainValey.getReflectionPaths(currentStep.stepCp);

//        ArrayList<CP> prevSteps = new ArrayList<>();
        for (ReflectionPath path : reflectionPaths) {
//            System.out.println("reflection"+c1.getNodes()[0].getX()+" "+c1.getNodes()[0].getY()+"___ "+c1.getNodes()[1].getX()+" "+c1.getNodes()[1].getY());
            //check if there is an unfoldable valley/mountain
            if (path.isComplete()) {

                CP prev = currentStep.stepCp.clone();
                for (Crease removableCrease : path.creases) {
//                    System.out.println("prev " + prev.getCreases().size());
                    prev.removeCrease(removableCrease);
                }
                prev.removeRedundantNodes();

                simp = true;
//                System.out.println("curr " + currentStep.stepCp.getCreases().size());
//                System.out.println("prev " + prev.getCreases().size());
//                prevSteps.add(prev);

//                try {
//                    if(i==7){
//                        System.out.println("");
//                    }
//                    oripa.ExporterSVG.exportDotted(FromToOripa.getDoc(prev), "igual"+i+++".svg");
//                } catch (Exception ex) {
//                    Logger.getLogger(Unfolder.class.getName()).log(Level.SEVERE, null, ex);
//                }
                for (ExecutionNode execNode : steps) {
                    if (execNode.stepCp.equals(prev)) {
//                        System.out.println("deu igual " + prev.getCreases().size() + " " + execNode.stepCp.getCreases().size());
                        execNode.addNext(currentStep, null);
                        arcNumber++;
                        prev = null;
                        break;
                    }
                }
                if (prev != null) {
//                    System.out.println("n deu igual " + prev.getCreases().size());

                    ////
//                    try {
//                        oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(prev)), "results/" + i++ + "returned-vm.opx");
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
                    /////
                    i++;
                    ExecutionNode prevStep = new ExecutionNode(prev);
                    prevStep.addNext(currentStep, null);
                    prevStep.id = i - 1;
                    arcNumber++;
                    nodeNumeber++;
                    steps.add(prevStep);
                    if (returnValue == null) {
                        returnValue = unfold(prevStep);
                    } else {
                        unfold(prevStep);
                    }
                }
            }
        }



//        // experimental use of maneuver reverse fold
//        if (!simp) {
//            ArrayList<Matching> matchings = reverseFold.findMatch(currentStep.stepCp, reflectionPaths);
//            if (matchings.size()>0) {
//                System.out.println("matchings number: "+matchings.size());
//                for (int j = 0; j < matchings.size(); j++) {
//                    CP prev = currentStep.stepCp.clone();
//                    reverseFold.unfold(prev, matchings.get(j));
//
//                    prev.removeRedundantNodes();
//                    for (ExecutionNode execNode : steps) {
//                        if (execNode.stepCp.equals(prev)) {
//                            System.out.println("deu igual " + prev.getCreases().size() + " " + execNode.stepCp.getCreases().size());
//                            execNode.addNext(currentStep, reverseFold);
//                            arcNumber++;
//                            prev = null;
//                            break;
//                        }
//                    }
//                    if (prev != null) {
//                        System.out.println("n deu igual");
//
//
//
//
//                        ExecutionNode prevStep = new ExecutionNode(prev);
//                        prevStep.addNext(currentStep, reverseFold);
//                        arcNumber++;
//                        nodeNumeber++;
//                        steps.add(prevStep);
//                        if (returnValue == null) {
//                            returnValue = unfold(prevStep);
//                        } else {
//                            unfold(prevStep);
//                        }
//                    }
//                }
//            }
//        }        

        // experimental use of outside maneuver reverse fold
        if (!simp) {
            for (Maneuver maneuver : maneuvers) {


                ArrayList<Matching> matchings = maneuver.findMatch(currentStep.stepCp, reflectionPaths);
                if (matchings.size() > 0) {
                    for (int j = 0; j < matchings.size(); j++) {
                        CP prev = currentStep.stepCp.clone();
                        if (matchings.get(j).isReflection()) {
                            continue;
                        }

                        maneuver.unfold(prev, matchings.get(j));

                        prev.removeRedundantNodes();
                        for (ExecutionNode execNode : steps) {
                            if (execNode.stepCp.equals(prev)) {
//                                System.out.println("deu igual " + prev.getCreases().size() + " " + execNode.stepCp.getCreases().size());
                                if (execNode.next.indexOf(currentStep) == -1) {
                                    execNode.addNext(currentStep, maneuver);
                                    arcNumber++;
                                }
                                prev = null;
                                break;
                            }
                        }
                        if (prev != null) {

//                            System.out.println("n deu igual " + prev.getCreases().size());

                            ////
//                            try {
//                                oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(prev)), "results/" + i++ + "returned-" + maneuver.getDescription() + ".opx");
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
                            /////
                            ExecutionNode prevStep = new ExecutionNode(prev);
                            prevStep.addNext(currentStep, maneuver);
                            i++;
                            prevStep.id = i - 1;
                            arcNumber++;
                            nodeNumeber++;
                            steps.add(prevStep);
                            if (returnValue == null) {
                                returnValue = unfold(prevStep);
                            } else {
                                unfold(prevStep);
                            }
                        }
                    }
                }
            }
        }
        //end experimental


//        //////
//        try {
//            oripa.ExporterXML.export(new DataSet(FromToOripa.getDoc(currentStep.stepCp)), "results/returned" + i++ + ".opx");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        ///////
        return returnValue;
    }

    public static void readManeuvers(String filepath) {
        try {
            maneuvers = ManeuverIO.importManeuvers(filepath);
//            for (Maneuver maneuver : maneuvers) {
//                System.out.println(maneuver);
//            }
//            System.exit(-1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<ExecutionNode> getPrevious(ExecutionNode currentStep) {
        if (currentStep.stepCp.isUnfolded()) {
            return null;
        }
        ArrayList<ExecutionNode> previousSteps = new ArrayList<>();

        currentStep.stepCp.calculateComplexity();
        ArrayList<ReflectionPath> reflectionPaths = currentStep.stepCp.reflectionPaths;

        for (ReflectionPath path : reflectionPaths) {
            //check if there is an unfoldable valley/mountain
            if (path.isComplete()) {
                System.out.println(path);
                CP prev = currentStep.stepCp.clone();
                for (Crease removableCrease : path.creases) {
                    prev.removeCrease(removableCrease);
                }
                prev.removeRedundantNodes();

                i++;
                prev.calculateComplexity();
                ExecutionNode prevStep = new ExecutionNode(prev);
                prevStep.addNext(currentStep, null);
                prevStep.id = i - 1;
                previousSteps.add(prevStep);
                //////
                try {
                    exporter.export(new DataSet(FromToOripa.getDoc(prev)), "results/returned" + i++ + ".opx");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ///////
            }
        }


        for (Maneuver maneuver : maneuvers) {


//            double time = System.currentTimeMillis();

            ArrayList<Matching> matchings = maneuver.findMatch(currentStep.stepCp, reflectionPaths);

//            JOptionPane.showMessageDialog(
//                    null, System.currentTimeMillis() - time + " " + time + " maneuver: " + maneuver.getDescription()
//                    + " complexity: " + currentStep.stepCp.localComplexity + " crease number: " +currentStep.stepCp.getCreases().size(), "computation time",
//                    JOptionPane.PLAIN_MESSAGE);

            if (matchings.size() > 0) {
                for (int j = 0; j < matchings.size(); j++) {
                    CP prev = currentStep.stepCp.clone();
                    if (matchings.get(j).isReflection()) {
                        continue;
                    }

                    maneuver.unfold(prev, matchings.get(j));

                    prev.removeRedundantNodes();

                    prev.calculateComplexity();
                    ExecutionNode prevStep = new ExecutionNode(prev);
                    prevStep.addNext(currentStep, maneuver);
                    i++;
                    prevStep.id = i - 1;
                    previousSteps.add(prevStep);
                    //////
                    try {
                        exporter.export(new DataSet(FromToOripa.getDoc(prev)), "results/returned" + i++ + ".opx");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    ///////

                }

            }

        }
        //end experimental

//        if (previousSteps.isEmpty()) {
        for (int j = 0; j < reflectionPaths.size(); j++) {
            ReflectionPath reflectionPath = reflectionPaths.get(j);
            CP prev = currentStep.stepCp.clone();
            if (reflectionPath.changeDirection(prev)) {
                prev.removeRedundantNodes();
                prev.calculateComplexity();
                if (prev.localComplexity > currentStep.stepCp.localComplexity) {
                    continue;
                }
                ExecutionNode prevStep = new ExecutionNode(prev);
                prevStep.addNext(currentStep, null);
                i++;
                prevStep.id = i - 1;
                previousSteps.add(prevStep);
                //////
                try {
                    exporter.export(new DataSet(FromToOripa.getDoc(prev)), "results/returned" + i++ + ".opx");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ///////
            } else if (reflectionPath.split(prev)) {
                prev.removeRedundantNodes();
                prev.calculateComplexity();
                if (prev.localComplexity > currentStep.stepCp.localComplexity) {
                    continue;
                }
                ExecutionNode prevStep = new ExecutionNode(prev);
                prevStep.addNext(currentStep, null);
                i++;
                prevStep.id = i - 1;
                previousSteps.add(prevStep);
                //////
                try {
                    exporter.export(new DataSet(FromToOripa.getDoc(prev)), "results/returned" + i++ + ".opx");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ///////
            }
        }
//        }

        return previousSteps;
    }
}
