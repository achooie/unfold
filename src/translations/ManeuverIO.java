/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translations;

import Simplification.Maneuver;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Hugo
 */
public class ManeuverIO {

    public static ArrayList<Maneuver> importManeuvers(String filepath) throws IOException {
        FileReader fr = new FileReader(filepath);
        ArrayList<Maneuver> maneuvers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tk = new StringTokenizer(line);
                if (tk.hasMoreTokens() && tk.nextToken().equals("Maneuver")) {
                    String description = br.readLine().replaceAll("description: ", "");
                    int nodesNumber = Integer.parseInt(br.readLine());
                    br.readLine();
                    ArrayList<int[]> before = new ArrayList<>();
                    for (int i = 0; i < nodesNumber; i++) {
                        tk = new StringTokenizer(br.readLine());
                        int aux[] = new int[tk.countTokens()];
                        for (int j = 0; j < aux.length; j++) {
                            line = tk.nextToken();
                            if (line.equals("null")) {
                                aux = null;
                                break;
                            }
//                            System.out.println(line + " " + tk.countTokens());
                            aux[j] = Integer.parseInt(line);
                        }
                        before.add(aux);
                    }
                    br.readLine();
                    ArrayList<int[]> after = new ArrayList<>();
                    for (int i = 0; i < nodesNumber; i++) {
                        tk = new StringTokenizer(br.readLine());
                        int aux[] = new int[tk.countTokens()];
                        for (int j = 0; j < aux.length; j++) {
                            line = tk.nextToken();
                            if (line.equals("null")) {
                                aux = null;
                                break;
                            }
//                            System.out.println(line + " " + tk.countTokens());
                            aux[j] = Integer.parseInt(line);
                        }
                        after.add(aux);
                    }
                    maneuvers.add(new Maneuver(nodesNumber, before, after, description));
                }
//                System.out.println(line);
            }
        }
        return maneuvers;
    }
}
