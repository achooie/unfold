/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Simplification.CP;
import Simplification.Crease;
import Simplification.Node;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import origamid.MountainValey;
import translations.FromToOripa;

/**
 *
 * @author akitaya
 */
public class MainFrame1 extends JFrame implements ActionListener {

    JMenuBar menuBar;
    JMenu menuFile;
    JMenu menuEdit;
    JMenu menuHelp;
    JMenuItem menuItemOpen;
    CP cp;
    private String lastPath = "";

    public MainFrame1(String title) throws HeadlessException {
        super(title);
        this.setSize(250, 300);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        menuBar.add(menuFile);
        menuItemOpen = new JMenuItem("Open");
        menuItemOpen.addActionListener(this);
        menuFile.add(menuItemOpen);

        getContentPane().setLayout(new BorderLayout());


        setJMenuBar(menuBar);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemOpen) {
            fileOpen();
        }

    }

    private void fileOpen() {
        JFileChooser fileChooser = new JFileChooser(lastPath);
        fileChooser.addChoosableFileFilter(
                new FileFilterEx(new String[]{".opx", ".xml"}, "(*.opx, *.xml) "));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            try {
                String filePath = fileChooser.getSelectedFile().getPath();
                lastPath = filePath;
                FromToOripa translator = new FromToOripa();
                cp = translator.loadCP(filePath);
                System.out.println(cp.getNodes().size());
                for (int i = 0; i < cp.getNodes().size(); i++) {
                    Node n = cp.getNodes().get(i);
                    System.out.println("nodo " + n.getId() + " x: " + n.getX() + ", y: " + n.getY());
                    if (i == 6) {
                        for (int j = 0; j < n.getCreases().size(); j++) {
                            System.out.println(n.getCreases().get(j).getAngle(n) * 180 / Math.PI);
                        }
                    }
                }

                System.out.println(cp.getCreases().size());
                for (int i = 0; i < cp.getCreases().size(); i++) {
                    Crease c = cp.getCreases().get(i);
                    System.out.println("crease " + (i + 1) + " n1: " + c.getNodes()[0].getId() + " n2: " + c.getNodes()[1].getId());
                }

                File dir = new File("./results");
                String[] children = dir.list();
                if (children != null) {
                    for (int j = 0; j < children.length; j++) {
                        (new File(dir, children[j])).delete();
                    }
                }
                dir.delete();
                dir.mkdir();
                int i = 0;
                while (MountainValey.removeFold(cp)) {
                    translator.writeCP(cp, "./results/result_" + (++i) + ".opx");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, e.toString(), "Error_FileLoadFailed",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
