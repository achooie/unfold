/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import animation.SimulationFrame;
import Simplification.ExecutionNode;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import origamid.GeomUtilD;
import origamid.Unfolder;
import oripa.Constants;
import oripa.Doc;
import oripa.ORIPA;
import oripa.geom.OriEdge;
import translations.ExporterSVG;
import translations.FromToOripa;

/**
 *
 * @author akitaya
 */
public class MainDeveloperFrame extends javax.swing.JFrame {

    private String lastPath = "";
    JPanel jPanelPreviousStep;
    JPanel jPanelNextStep;
    GridBagConstraints previousStepLayout;
    ArrayList<ExecutionNode> choosedSequence;
    Unfolder unfolder;
    ExecutionNode currentStep;
    boolean isViewingNextStep;
    MouseListener mouseListenerFoldedFrame;

    /**
     * Creates new form NewJFrame
     */
    public MainDeveloperFrame() {
        ORIPA.doc = new Doc(Constants.DEFAULT_PAPER_SIZE);
        initComponents();

        FoldedModelScreen screen = new FoldedModelScreen(
                new ExecutionNode(FromToOripa.getCP(ORIPA.doc)), true, "", true);
        screen.setPreferredSize(jPanelCurrentStep.getSize());
        jPanelCurrentStep.add(screen);
        jPanelPreviousStep = new JPanel();
        jPanelPreviousStep.setBackground(Color.LIGHT_GRAY);
        jPanelPreviousStep.setLayout(new GridBagLayout());
        previousStepLayout = new GridBagConstraints();
        previousStepLayout.gridy = 0;
        previousStepLayout.insets = new Insets(5, 0, 0, 0);
        jScrollPanePreviousSteps.getViewport().add(jPanelPreviousStep);
        jPanelNextStep = new JPanel();
        jPanelNextStep.setBackground(Color.LIGHT_GRAY);
        jScrollPaneNextStep.getViewport().add(jPanelNextStep);
        choosedSequence = new ArrayList<>();
        jScrollPaneNextStep.getHorizontalScrollBar().setUnitIncrement(16);
        jScrollPanePreviousSteps.getVerticalScrollBar().setUnitIncrement(16);
        Unfolder.readManeuvers("Maneuvers.oriD");
        mouseListenerFoldedFrame = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                setMainView((FoldedModelScreen) e.getSource());
                if (((FoldedModelScreen) e.getSource()).getParent().equals(jPanelNextStep)) {
                    isViewingNextStep = true;
                } else {
                    isViewingNextStep = false;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
    }

    private void fileOpen() {
        JFileChooser fileChooser = new JFileChooser(lastPath);
        fileChooser.setFileFilter(
                new FileFilterEx(new String[]{".opx", ".xml"}, "(*.opx, *.xml) "));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            try {
                clearViews();
                String filePath = fileChooser.getSelectedFile().getPath();
                lastPath = filePath;
                double time = System.currentTimeMillis();
                unfolder = new Unfolder(filePath, jCheckBoxDeveloper.isSelected());
                currentStep = unfolder.getLastStep();
                JOptionPane.showMessageDialog(
                        this, System.currentTimeMillis() - time + " node: " + (1 + unfolder.nodeNumeber) + " arc: " + unfolder.arcNumber, "computation time",
                        JOptionPane.PLAIN_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, e.toString(), "Error_FileLoadFailed",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void exportSVG() {
        JFileChooser fileChooser = new JFileChooser(lastPath);
        fileChooser.addChoosableFileFilter(new FileFilterEx(new String[]{".svg"},
                "(*.svg) "));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
            try {
                String filePath = fileChooser.getSelectedFile().getPath();
                if (!filePath.endsWith(".svg")) {
                    filePath += ".svg";
                }
                File file = new File(filePath);
                if (file.exists()) {
                    if (JOptionPane.showConfirmDialog(
                            null, "Warning SameNameFileExist",
                            "SVG Export",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                ExporterSVG.export(jPanelPreviousStep.getComponents(), filePath);
                lastPath = filePath;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, e.toString(), "Error FileSaveFailed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public void clearViews() {
        jPanelPreviousStep.removeAll();
        ORIPA.doc = new Doc(Constants.DEFAULT_PAPER_SIZE);
        jPanelCurrentStep.removeAll();
        FoldedModelScreen screen = new FoldedModelScreen(
                new ExecutionNode(FromToOripa.getCP(ORIPA.doc)), true, "", true);
        screen.setPreferredSize(jPanelCurrentStep.getSize());
        jPanelCurrentStep.add(screen);
        choosedSequence.clear();
        paintAll(this.getGraphics());
    }

    public void setMainView(FoldedModelScreen screen) {
//        ORIPA.doc = screen.doc;
//        ORIPA.doc.sortedFaces.clear();
//        ORIPA.doc.buildOrigami3(false);
//        Folder folder = new Folder(ORIPA.doc);
//        int answerNum = folder.fold();
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).copy(screen);
        jLabelAnswNumber.setText("Folded model [" + (screen.doc.currentORmatIndex + 1) + "/"
                + ORIPA.doc.overlapRelations.size() + "]");
        jPanelPreviousStep.repaint();
//        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).setScale(.25);
//        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).setTranslate(-225,-225);
//        screen.setScale(.7);
//        paintAll(this.getGraphics());
//        repaint();
//        ((RenderScreen2) jPanelCurrentStep.getComponent(0)).redrawOrigami();
    }

    public void setNextSteps() {
        jPanelNextStep.removeAll();
        FoldedModelScreen mainView =
                ((FoldedModelScreen) jPanelPreviousStep.getComponent(jPanelPreviousStep.getComponentCount() - 1));
        ArrayList<ExecutionNode> previousSteps = unfolder.getPrevious(currentStep);
        for (int i = 0; i < previousSteps.size(); i++) {
            ExecutionNode executionNode = previousSteps.get(i);
            try {
                GeomUtilD.foldCP(executionNode.stepCp);
            } catch (Exception e) {
                continue;
            }
            FoldedModelScreen a = new FoldedModelScreen(executionNode, false,
                    (executionNode.maneuvers.get(0) == null ? ""
                    : executionNode.maneuvers.get(0).getDescription()) + " " + executionNode.id
                    + " " + executionNode.stepCp.generalComplexity + " " + executionNode.stepCp.localComplexity,
                    jCheckBoxFlip.isSelected());
            //a.setTranslate(-900, -900);
            a.setScale(.25);
            a.setPreferredSize(new Dimension(150, 150));
            jPanelNextStep.add(a);
            a.addMouseListener(mouseListenerFoldedFrame);
//            for (OriVertex oriVertex : mainView.doc.vertices) {
            a.addClickableOriVertex(mainView.doc);
//                System.out.println(oriVertex.preP + " " + oriVertex.p);
//            }
            for (OriEdge oriEdge : mainView.doc.edges) {
                a.addLineSymbol(oriEdge);
            }
            if (executionNode.maneuvers.get(0) != null) {
                a.addPushSymbols(mainView.step.stepCp);
            }
        }
        ORIPA.doc = mainView.doc;
        jScrollPaneNextStep.paintAll(jScrollPaneNextStep.getGraphics());
    }

    public void setPrevSteps() {
        FoldedModelScreen a = new FoldedModelScreen(currentStep, false,
                ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).description,
                jCheckBoxFlip.isSelected());
        a.copy((FoldedModelScreen) jPanelCurrentStep.getComponent(0));
        //a.setTranslate(-900, -900);
        a.setScale(.25);
        a.setPreferredSize(new Dimension(150, 150));
        a.addMouseListener(mouseListenerFoldedFrame);
//        a.isFlipped = jCheckBoxFlip.isSelected();
        if (jPanelPreviousStep.getComponentCount() > 2) {
            jPanelPreviousStep.setPreferredSize(new Dimension(150, 10 + (jPanelPreviousStep.getComponentCount() + 1) * 155));
        }
        jPanelPreviousStep.add(a, previousStepLayout);
        previousStepLayout.gridy++;
        jScrollPanePreviousSteps.paintAll(jScrollPanePreviousSteps.getGraphics());
        jScrollPanePreviousSteps.getVerticalScrollBar().setValue(
                jScrollPanePreviousSteps.getVerticalScrollBar().getMaximum());
        isViewingNextStep = false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanelCurrentStep = new javax.swing.JPanel();
        jCheckBoxFlip = new javax.swing.JCheckBox();
        jScrollPanePreviousSteps = new javax.swing.JScrollPane();
        jScrollPaneNextStep = new javax.swing.JScrollPane();
        jToggleButtonMountain = new javax.swing.JToggleButton();
        jToggleButtonValley = new javax.swing.JToggleButton();
        jButtonClear = new javax.swing.JButton();
        jToggleButtonDotted = new javax.swing.JToggleButton();
        jToggleButtonSelect = new javax.swing.JToggleButton();
        jCheckBoxShowCP = new javax.swing.JCheckBox();
        jButtonNextFoldedForm = new javax.swing.JButton();
        jButtonPrevFoldedForm = new javax.swing.JButton();
        jLabelAnswNumber = new javax.swing.JLabel();
        jButtonConfirmStep = new javax.swing.JButton();
        jButtonDeleteLastStep = new javax.swing.JButton();
        jButtonSimulate = new javax.swing.JButton();
        jCheckBoxDeveloper = new javax.swing.JCheckBox();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemOpen = new javax.swing.JMenuItem();
        jMenuItemExportSVG = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OrigamiD");

        jCheckBoxFlip.setText("Flip");
        jCheckBoxFlip.setFocusable(false);
        jCheckBoxFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFlipActionPerformed(evt);
            }
        });

        jScrollPanePreviousSteps.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jScrollPaneNextStep.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        buttonGroup1.add(jToggleButtonMountain);
        jToggleButtonMountain.setText("M");
        jToggleButtonMountain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonMountainActionPerformed(evt);
            }
        });

        buttonGroup1.add(jToggleButtonValley);
        jToggleButtonValley.setText("V");

        jButtonClear.setText("Clear");
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });

        buttonGroup1.add(jToggleButtonDotted);
        jToggleButtonDotted.setText("D");

        buttonGroup1.add(jToggleButtonSelect);
        jToggleButtonSelect.setText("Select");
        jToggleButtonSelect.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jToggleButtonSelectFocusLost(evt);
            }
        });
        jToggleButtonSelect.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jToggleButtonSelectKeyPressed(evt);
            }
        });

        jCheckBoxShowCP.setText("Show CP");
        jCheckBoxShowCP.setFocusable(false);
        jCheckBoxShowCP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowCPActionPerformed(evt);
            }
        });

        jButtonNextFoldedForm.setText("next>");
        jButtonNextFoldedForm.setFocusable(false);
        jButtonNextFoldedForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextFoldedFormActionPerformed(evt);
            }
        });

        jButtonPrevFoldedForm.setText("<prev");
        jButtonPrevFoldedForm.setFocusable(false);
        jButtonPrevFoldedForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrevFoldedFormActionPerformed(evt);
            }
        });

        jLabelAnswNumber.setText("Folded Model");

        jButtonConfirmStep.setText("Confirm Step");
        jButtonConfirmStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfirmStepActionPerformed(evt);
            }
        });

        jButtonDeleteLastStep.setText("Delete Last Step");
        jButtonDeleteLastStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteLastStepActionPerformed(evt);
            }
        });

        jButtonSimulate.setText("Simulate");
        jButtonSimulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSimulateActionPerformed(evt);
            }
        });

        jCheckBoxDeveloper.setText("DeveloperView");
        jCheckBoxDeveloper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDeveloperActionPerformed(evt);
            }
        });

        jMenuFile.setText("File");

        jMenuItemOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemOpen.setText("Open");
        jMenuItemOpen.setToolTipText("");
        jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpen);

        jMenuItemExportSVG.setText("Export SVG");
        jMenuItemExportSVG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportSVGActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExportSVG);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuEdit.setText("Edit");
        jMenuBar.add(jMenuEdit);

        jMenuHelp.setText("Help");
        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonPrevFoldedForm)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonNextFoldedForm))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jToggleButtonMountain)
                                .addGap(7, 7, 7)
                                .addComponent(jToggleButtonValley))
                            .addComponent(jToggleButtonDotted)
                            .addComponent(jToggleButtonSelect)
                            .addComponent(jButtonClear)
                            .addComponent(jCheckBoxShowCP)
                            .addComponent(jCheckBoxFlip)
                            .addComponent(jLabelAnswNumber)
                            .addComponent(jButtonConfirmStep)
                            .addComponent(jButtonDeleteLastStep)
                            .addComponent(jButtonSimulate)
                            .addComponent(jCheckBoxDeveloper))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelCurrentStep, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPanePreviousSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPaneNextStep)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanelCurrentStep, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                        .addComponent(jScrollPanePreviousSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButtonMountain)
                            .addComponent(jToggleButtonValley))
                        .addGap(10, 10, 10)
                        .addComponent(jToggleButtonDotted)
                        .addGap(29, 29, 29)
                        .addComponent(jToggleButtonSelect)
                        .addGap(6, 6, 6)
                        .addComponent(jButtonClear)
                        .addGap(45, 45, 45)
                        .addComponent(jCheckBoxDeveloper)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelAnswNumber)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addComponent(jCheckBoxFlip))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonNextFoldedForm)
                                .addComponent(jButtonPrevFoldedForm)))
                        .addComponent(jCheckBoxShowCP)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonConfirmStep)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDeleteLastStep)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSimulate)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPaneNextStep, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenActionPerformed
        fileOpen();
        if (currentStep == null) {
            JOptionPane.showMessageDialog(
                    this, "CP have patterns that this software cannot solve", "Error_FileLoadFailed",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                GeomUtilD.foldCP(currentStep.stepCp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setMainView(new FoldedModelScreen(currentStep, false, "", jCheckBoxFlip.isSelected()));
            setPrevSteps();
            setNextSteps();
            choosedSequence.add(currentStep);
        }
    }//GEN-LAST:event_jMenuItemOpenActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jCheckBoxFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFlipActionPerformed
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).flipFaces(jCheckBoxFlip.isSelected());
//        for (Component component : jPanelNextStep.getComponents()) {
//            ((FoldedModelScreen) component).flipFaces(jCheckBoxFlip.isSelected());
//        }
    }//GEN-LAST:event_jCheckBoxFlipActionPerformed

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).clearSymbols();
    }//GEN-LAST:event_jButtonClearActionPerformed

    private void jToggleButtonSelectKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jToggleButtonSelectKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE
                || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).deleteSelectedSymbol();
        }
    }//GEN-LAST:event_jToggleButtonSelectKeyPressed

    private void jToggleButtonSelectFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jToggleButtonSelectFocusLost
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).unSelected();
    }//GEN-LAST:event_jToggleButtonSelectFocusLost

    private void jCheckBoxShowCPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowCPActionPerformed

//        System.out.println("asdf" + jPanelCurrentStep.getComponent(0).getClass());
        if (jCheckBoxShowCP.isSelected()) {
            CPScreen screen = new CPScreen((FoldedModelScreen) jPanelCurrentStep.getComponent(0));
            screen.setPreferredSize(jPanelCurrentStep.getSize());
            jPanelCurrentStep.removeAll();
            jPanelCurrentStep.add(screen);
            screen.setScale(1.1);
            jPanelCurrentStep.paintAll(jPanelCurrentStep.getGraphics());
            jButtonClear.setEnabled(false);
            jButtonConfirmStep.setEnabled(false);
            jButtonNextFoldedForm.setEnabled(false);
            jButtonPrevFoldedForm.setEnabled(false);
            jCheckBoxFlip.setEnabled(false);
            jToggleButtonDotted.setEnabled(false);
            jToggleButtonMountain.setEnabled(false);
            jToggleButtonSelect.setEnabled(false);
            jToggleButtonValley.setEnabled(false);
            repaint();
        } else {
            FoldedModelScreen screen = ((CPScreen) jPanelCurrentStep.getComponent(0)).getFolded();
            jPanelCurrentStep.removeAll();
            jPanelCurrentStep.add(screen);
            jButtonClear.setEnabled(true);
            jButtonConfirmStep.setEnabled(true);
            jButtonNextFoldedForm.setEnabled(true);
            jButtonPrevFoldedForm.setEnabled(true);
            jCheckBoxFlip.setEnabled(true);
            jToggleButtonDotted.setEnabled(true);
            jToggleButtonMountain.setEnabled(true);
            jToggleButtonSelect.setEnabled(true);
            jToggleButtonValley.setEnabled(true);
            repaint();
//            System.out.println("fdsa");
        }
    }//GEN-LAST:event_jCheckBoxShowCPActionPerformed

    private void jButtonPrevFoldedFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrevFoldedFormActionPerformed
        ORIPA.doc.setPrevORMat();
        jLabelAnswNumber.setText("Folded model [" + (ORIPA.doc.currentORmatIndex + 1) + "/"
                + ORIPA.doc.overlapRelations.size() + "]");
        try {
            ORIPA.doc.sortedFaces = GeomUtilD.getSortedFaces(ORIPA.doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).redrawOrigami();
    }//GEN-LAST:event_jButtonPrevFoldedFormActionPerformed

    private void jButtonNextFoldedFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextFoldedFormActionPerformed
        ORIPA.doc.setNextORMat();
        jLabelAnswNumber.setText("Folded model [" + (ORIPA.doc.currentORmatIndex + 1) + "/"
                + ORIPA.doc.overlapRelations.size() + "]");
        try {
            ORIPA.doc.sortedFaces = GeomUtilD.getSortedFaces(ORIPA.doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).redrawOrigami();
    }//GEN-LAST:event_jButtonNextFoldedFormActionPerformed

    private void jButtonConfirmStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfirmStepActionPerformed
        if (isViewingNextStep) {
            currentStep = ((FoldedModelScreen) jPanelCurrentStep.getComponent(0)).step;
            choosedSequence.add(currentStep);
            setPrevSteps();
            setNextSteps();
        }
    }//GEN-LAST:event_jButtonConfirmStepActionPerformed

    private void jButtonDeleteLastStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteLastStepActionPerformed
        if (choosedSequence.size() > 1) {
            choosedSequence.remove(choosedSequence.size() - 1);
            currentStep = choosedSequence.get(choosedSequence.size() - 1);
            jPanelPreviousStep.remove(jPanelPreviousStep.getComponentCount() - 1);
            setMainView((FoldedModelScreen) jPanelPreviousStep.getComponent(jPanelPreviousStep.getComponentCount() - 1));
            setNextSteps();
            jPanelPreviousStep.repaint();
        }
    }//GEN-LAST:event_jButtonDeleteLastStepActionPerformed
    private void jToggleButtonMountainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonMountainActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonMountainActionPerformed

    private void jMenuItemExportSVGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportSVGActionPerformed
        exportSVG();
    }//GEN-LAST:event_jMenuItemExportSVGActionPerformed

    private void jButtonSimulateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimulateActionPerformed
        SimulationFrame bb = new SimulationFrame(GeomUtilD.reverse(jPanelPreviousStep.getComponents()));
        bb.addKeyListener(bb);
        bb.setVisible(true);
    }//GEN-LAST:event_jButtonSimulateActionPerformed

    private void jCheckBoxDeveloperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDeveloperActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDeveloperActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;


                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainDeveloperFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainDeveloperFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonConfirmStep;
    private javax.swing.JButton jButtonDeleteLastStep;
    private javax.swing.JButton jButtonNextFoldedForm;
    private javax.swing.JButton jButtonPrevFoldedForm;
    private javax.swing.JButton jButtonSimulate;
    private javax.swing.JCheckBox jCheckBoxDeveloper;
    private javax.swing.JCheckBox jCheckBoxFlip;
    private javax.swing.JCheckBox jCheckBoxShowCP;
    private javax.swing.JLabel jLabelAnswNumber;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemExportSVG;
    private javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JPanel jPanelCurrentStep;
    private javax.swing.JScrollPane jScrollPaneNextStep;
    private javax.swing.JScrollPane jScrollPanePreviousSteps;
    public static javax.swing.JToggleButton jToggleButtonDotted;
    public static javax.swing.JToggleButton jToggleButtonMountain;
    public static javax.swing.JToggleButton jToggleButtonSelect;
    public static javax.swing.JToggleButton jToggleButtonValley;
    // End of variables declaration//GEN-END:variables
}
