/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Hugo
 */
public class LastPathIO {

    public static String read() {
        FileReader fr = null;
        try {
            fr = new FileReader("lastPath.txt");
        } catch (FileNotFoundException ex) {
            return "";
        }
        BufferedReader br = new BufferedReader(fr);
        try {
            return br.readLine();
        } catch (IOException ex) {
            return "";
        }
    }

    public static void write(String lastPath) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("lastPath.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(lastPath + "\n");
            System.out.println("writting " + lastPath);
            bw.close();
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
