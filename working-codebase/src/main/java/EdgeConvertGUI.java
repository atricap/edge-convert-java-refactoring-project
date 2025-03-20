import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class EdgeConvertGUI {
   
   public static final int HORIZ_SIZE = 635;
   public static final int VERT_SIZE = 400;
   public static final int HORIZ_LOC = 100;
   public static final int VERT_LOC = 100;
   public static final String DEFINE_TABLES = "Define Tables";
   public static final String DEFINE_RELATIONS = "Define Relations";
   public static final String CANCELLED = "CANCELLED";

   private JFileChooser jfcEdge, jfcOutputDir;
   private ExampleFileFilter effEdge, effSave;
   private File parseFile, saveFile, outputDir;
   private String truncatedFilename;
   private String databaseName;
   EdgeRadioButtonListener radioListener;
   EdgeWindowListener edgeWindowListener;
   CreateDDLButtonListener createDDLListener;
   EdgeConvertModel ecModel;
   private static boolean readSuccess = true; //this tells GUI whether to populate JList components or not
   private boolean dataSaved = true;
   private ArrayList<Object> alSubclasses;
   private ArrayList<String> alProductNames;
   private String[] productNames;
   private Object[] objSubclasses;

   //Define Tables screen objects
   JFrame jfDT;
   JPanel jpDTMove;
   JButton jbDTCreateDDL, jbDTDefineRelations, jbDTVarchar, jbDTDefaultValue, jbDTMoveUp, jbDTMoveDown;
   ButtonGroup bgDTDataType;
   JRadioButton[] jrbDataType;
   String[] strDataType;
   JCheckBox jcheckDTDisallowNull, jcheckDTPrimaryKey;
   JTextField jtfDTVarchar, jtfDTDefaultValue;
   JLabel jlabDTTables, jlabDTFields;
   JScrollPane jspDTTablesAll, jspDTFieldsTablesAll;
   JList<String> jlDTTablesAll, jlDTFieldsTablesAll;
   DefaultListModel<String> dlmDTTablesAll, dlmDTFieldsTablesAll;
   JMenuItem jmiDTOpenEdge, jmiDTOpenSave, jmiDTSave, jmiDTSaveAs, jmiDTExit, jmiDTOptionsOutputLocation, jmiDTOptionsShowProducts, jmiDTHelpAbout;
   
   //Define Relations screen objects
   JFrame jfDR;
   JButton jbDRCreateDDL, jbDRDefineTables, jbDRBindRelation;
   JList<String> jlDRTablesRelations, jlDRTablesRelatedTo, jlDRFieldsTablesRelations, jlDRFieldsTablesRelatedTo;
   DefaultListModel<String> dlmDRTablesRelations, dlmDRTablesRelatedTo, dlmDRFieldsTablesRelations, dlmDRFieldsTablesRelatedTo;
   JLabel jlabDRTablesRelations, jlabDRTablesRelatedTo, jlabDRFieldsTablesRelations, jlabDRFieldsTablesRelatedTo;
   JScrollPane jspDRTablesRelations, jspDRTablesRelatedTo, jspDRFieldsTablesRelations, jspDRFieldsTablesRelatedTo;
   JMenuItem jmiDROpenEdge, jmiDROpenSave, jmiDRSave, jmiDRSaveAs, jmiDRExit, jmiDROptionsOutputLocation, jmiDROptionsShowProducts, jmiDRHelpAbout;

   public static EdgeConvertGUI launch(String... args) {
      EdgeConvertGUI gui = new EdgeConvertGUI();
      return gui;
   }
   
   public EdgeConvertGUI() {
      EdgeMenuListener menuListener = new EdgeMenuListener();
      radioListener = new EdgeRadioButtonListener();
      edgeWindowListener = new EdgeWindowListener();
      createDDLListener = new CreateDDLButtonListener();
      ecModel = new EdgeConvertModel();
      DTBuilder dtBuilder = new DTBuilder(this, createDDLListener, ecModel, edgeWindowListener, radioListener, menuListener);
      DRBuilder drBuilder = new DRBuilder(this, createDDLListener, edgeWindowListener, menuListener);

      setUpLookAndFeel();
      jfDT = dtBuilder.build();
      jfDR = drBuilder.build();
   }

   private static void setUpLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //use the OS native LAF, as opposed to default Java LAF
      } catch (Exception e) {
         System.out.println("Error setting native LAF: " + e);
      }
   }

   private interface Builder<T> {
      T build();
   }

   /**
    * Builds Define Tables screen.
    */
   private static class DTBuilder implements Builder<JFrame> {
      private final EdgeConvertGUI gui;
      private final CreateDDLButtonListener createDDLListener;
      private final EdgeConvertModel ecModel;
      private final EdgeWindowListener edgeWindowListener;
      private final EdgeRadioButtonListener radioListener;
      private final EdgeMenuListener menuListener;

      public DTBuilder(EdgeConvertGUI gui,
                       CreateDDLButtonListener createDDLListener,
                       EdgeConvertModel ecModel,
                       EdgeWindowListener edgeWindowListener,
                       EdgeRadioButtonListener radioListener,
                       EdgeMenuListener menuListener) {
         this.gui = gui;
         this.createDDLListener = createDDLListener;
         this.ecModel = ecModel;
         this.edgeWindowListener = edgeWindowListener;
         this.radioListener = radioListener;
         this.menuListener = menuListener;
      }

      @Override
      public JFrame build() {
         return createDTScreen();
      }

      private JFrame createDTScreen() {
         JFrame f = new JFrame(DEFINE_TABLES);
         f.setLocation(HORIZ_LOC, VERT_LOC);
         f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         f.addWindowListener(edgeWindowListener);
         Container cp = f.getContentPane();
         cp.setLayout(new BorderLayout());
         f.setVisible(true);
         f.setSize(HORIZ_SIZE + 150, VERT_SIZE);

         f.setJMenuBar(createDTMenuBar());

         cp.add(createDTBottom(), BorderLayout.SOUTH);
         cp.add(createDTCenter(), BorderLayout.CENTER);

         f.validate();
         return f;
      }

      private JMenuBar createDTMenuBar() {
         JMenuBar mb = new JMenuBar();

         JMenu jmDTFile = new JMenu("File");
         jmDTFile.setMnemonic(KeyEvent.VK_F);
         mb.add(jmDTFile);
         gui.jmiDTOpenEdge = new JMenuItem("Open Edge File");
         gui.jmiDTOpenEdge.setMnemonic(KeyEvent.VK_E);
         gui.jmiDTOpenEdge.addActionListener(menuListener);
         gui.jmiDTOpenSave = new JMenuItem("Open Save File");
         gui.jmiDTOpenSave.setMnemonic(KeyEvent.VK_V);
         gui.jmiDTOpenSave.addActionListener(menuListener);
         gui.jmiDTSave = new JMenuItem("Save");
         gui.jmiDTSave.setMnemonic(KeyEvent.VK_S);
         gui.jmiDTSave.setEnabled(false);
         gui.jmiDTSave.addActionListener(menuListener);
         gui.jmiDTSaveAs = new JMenuItem("Save As...");
         gui.jmiDTSaveAs.setMnemonic(KeyEvent.VK_A);
         gui.jmiDTSaveAs.setEnabled(false);
         gui.jmiDTSaveAs.addActionListener(menuListener);
         gui.jmiDTExit = new JMenuItem("Exit");
         gui.jmiDTExit.setMnemonic(KeyEvent.VK_X);
         gui.jmiDTExit.addActionListener(menuListener);
         jmDTFile.add(gui.jmiDTOpenEdge);
         jmDTFile.add(gui.jmiDTOpenSave);
         jmDTFile.add(gui.jmiDTSave);
         jmDTFile.add(gui.jmiDTSaveAs);
         jmDTFile.add(gui.jmiDTExit);

         JMenu jmDTOptions = new JMenu("Options");
         jmDTOptions.setMnemonic(KeyEvent.VK_O);
         mb.add(jmDTOptions);
         gui.jmiDTOptionsOutputLocation = new JMenuItem("Set Output File Definition Location");
         gui.jmiDTOptionsOutputLocation.setMnemonic(KeyEvent.VK_S);
         gui.jmiDTOptionsOutputLocation.addActionListener(menuListener);
         gui.jmiDTOptionsShowProducts = new JMenuItem("Show Database Products Available");
         gui.jmiDTOptionsShowProducts.setMnemonic(KeyEvent.VK_H);
         gui.jmiDTOptionsShowProducts.setEnabled(false);
         gui.jmiDTOptionsShowProducts.addActionListener(menuListener);
         jmDTOptions.add(gui.jmiDTOptionsOutputLocation);
         jmDTOptions.add(gui.jmiDTOptionsShowProducts);

         JMenu jmDTHelp = new JMenu("Help");
         jmDTHelp.setMnemonic(KeyEvent.VK_H);
         mb.add(jmDTHelp);
         gui.jmiDTHelpAbout = new JMenuItem("About");
         gui.jmiDTHelpAbout.setMnemonic(KeyEvent.VK_A);
         gui.jmiDTHelpAbout.addActionListener(menuListener);
         jmDTHelp.add(gui.jmiDTHelpAbout);

         gui.jfcEdge = new JFileChooser();
         gui.jfcOutputDir = new JFileChooser();
         gui.effEdge = new ExampleFileFilter("edg", "Edge Diagrammer Files");
         gui.effSave = new ExampleFileFilter("sav", "Edge Convert Save Files");
         gui.jfcOutputDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

         return mb;
      }

      private JComponent createDTBottom() {
         JPanel bottom = new JPanel(new GridLayout(1, 2));

         gui.jbDTCreateDDL = new JButton("Create DDL");
         gui.jbDTCreateDDL.setEnabled(false);
         gui.jbDTCreateDDL.addActionListener(createDDLListener);

         gui.jbDTDefineRelations = new JButton (DEFINE_RELATIONS);
         gui.jbDTDefineRelations.setEnabled(false);
         gui.jbDTDefineRelations.addActionListener((ActionEvent ae) -> gui.showDRScreen());

         bottom.add(gui.jbDTDefineRelations);
         bottom.add(gui.jbDTCreateDDL);

         return bottom;
      }

      private JComponent createDTCenter() {
         JComponent center = new JPanel(new GridLayout(1, 3));
         JComponent centerRight = new JPanel(new GridLayout(1, 2));
         gui.dlmDTTablesAll = new DefaultListModel<>();
         gui.jlDTTablesAll = new JList<>(gui.dlmDTTablesAll);
         gui.jlDTTablesAll.addListSelectionListener((ListSelectionEvent lse) -> onDTTablesAllSelection(lse, gui, ecModel));

         gui.dlmDTFieldsTablesAll = new DefaultListModel<>();
         gui.jlDTFieldsTablesAll = new JList<>(gui.dlmDTFieldsTablesAll);
         gui.jlDTFieldsTablesAll.addListSelectionListener((ListSelectionEvent lse) -> onDTFieldsTablesAllSelection(lse, gui, ecModel));

         gui.jpDTMove = new JPanel(new GridLayout(2, 1));
         gui.jbDTMoveUp = new JButton("^");
         gui.jbDTMoveUp.setEnabled(false);
         gui.jbDTMoveUp.addActionListener((ActionEvent ae) -> onDTMoveUp(ae, gui, ecModel));
         gui.jbDTMoveDown = new JButton("v");
         gui.jbDTMoveDown.setEnabled(false);
         gui.jbDTMoveDown.addActionListener((ActionEvent ae) -> onDTMoveDown(ae, gui, ecModel));
         gui.jpDTMove.add(gui.jbDTMoveUp);
         gui.jpDTMove.add(gui.jbDTMoveDown);

         gui.jspDTTablesAll = new JScrollPane(gui.jlDTTablesAll);
         gui.jspDTFieldsTablesAll = new JScrollPane(gui.jlDTFieldsTablesAll);
         JComponent center1 = new JPanel(new BorderLayout());
         JComponent center2 = new JPanel(new BorderLayout());
         gui.jlabDTTables = new JLabel("All Tables", SwingConstants.CENTER);
         gui.jlabDTFields = new JLabel("Fields List", SwingConstants.CENTER);
         center1.add(gui.jlabDTTables, BorderLayout.NORTH);
         center2.add(gui.jlabDTFields, BorderLayout.NORTH);
         center1.add(gui.jspDTTablesAll, BorderLayout.CENTER);
         center2.add(gui.jspDTFieldsTablesAll, BorderLayout.CENTER);
         center2.add(gui.jpDTMove, BorderLayout.EAST);
         center.add(center1);
         center.add(center2);
         center.add(centerRight);

         gui.strDataType = EdgeField.getStrDataType(); //get the list of currently supported data types
         gui.jrbDataType = new JRadioButton[gui.strDataType.length]; //create array of JRadioButtons, one for each supported data type
         gui.bgDTDataType = new ButtonGroup();
         JComponent centerRight1 = new JPanel(new GridLayout(gui.strDataType.length, 1));
         for (int i = 0; i < gui.strDataType.length; i++) {
            gui.jrbDataType[i] = new JRadioButton(gui.strDataType[i]); //assign label for radio button from String array
            gui.jrbDataType[i].setEnabled(false);
            gui.jrbDataType[i].addActionListener(radioListener);
            gui.bgDTDataType.add(gui.jrbDataType[i]);
            centerRight1.add(gui.jrbDataType[i]);
         }
         centerRight.add(centerRight1);

         gui.jcheckDTDisallowNull = new JCheckBox("Disallow Null");
         gui.jcheckDTDisallowNull.setEnabled(false);
         gui.jcheckDTDisallowNull.addItemListener(gui::onDisallowNullItemStateChanged);

         gui.jcheckDTPrimaryKey = new JCheckBox("Primary Key");
         gui.jcheckDTPrimaryKey.setEnabled(false);
         gui.jcheckDTPrimaryKey.addItemListener(gui::onPrimaryKeyItemStateChanged);

         gui.jbDTDefaultValue = new JButton("Set Default Value");
         gui.jbDTDefaultValue.setEnabled(false);
         gui.jbDTDefaultValue.addActionListener(gui::onSetDefaultValueActionPerformed);
         gui.jtfDTDefaultValue = new JTextField();
         gui.jtfDTDefaultValue.setEditable(false);

         gui.jbDTVarchar = new JButton("Set Varchar Length");
         gui.jbDTVarchar.setEnabled(false);
         gui.jbDTVarchar.addActionListener(gui::onSetVarcharLengthActionPerformed);
         gui.jtfDTVarchar = new JTextField();
         gui.jtfDTVarchar.setEditable(false);

         JComponent centerRight2 = new JPanel(new GridLayout(6, 1));
         centerRight2.add(gui.jbDTVarchar);
         centerRight2.add(gui.jtfDTVarchar);
         centerRight2.add(gui.jcheckDTPrimaryKey);
         centerRight2.add(gui.jcheckDTDisallowNull);
         centerRight2.add(gui.jbDTDefaultValue);
         centerRight2.add(gui.jtfDTDefaultValue);
         centerRight.add(centerRight1);
         centerRight.add(centerRight2);
         center.add(centerRight);

         return center;
      }
   }

   /**
    * Builds Define Relations screen.
    */
   private static class DRBuilder implements Builder<JFrame> {
      private final EdgeConvertGUI gui;
      private final CreateDDLButtonListener createDDLListener;
      private final EdgeWindowListener edgeWindowListener;
      private final EdgeMenuListener menuListener;

      public DRBuilder(EdgeConvertGUI gui,
                       CreateDDLButtonListener createDDLListener,
                       EdgeWindowListener edgeWindowListener,
                       EdgeMenuListener menuListener) {
         this.gui = gui;
         this.createDDLListener = createDDLListener;
         this.edgeWindowListener = edgeWindowListener;
         this.menuListener = menuListener;
      }

      @Override
      public JFrame build() {
         return createDRScreen();
      }

      private JFrame createDRScreen() {
         JFrame f = new JFrame(DEFINE_RELATIONS);
         f.setSize(HORIZ_SIZE, VERT_SIZE);
         f.setLocation(HORIZ_LOC, VERT_LOC);
         f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         f.addWindowListener(edgeWindowListener);
         Container cp = f.getContentPane();
         cp.setLayout(new BorderLayout());

         f.setJMenuBar(createDRMenuBar());

         cp.add(createDRCenter(), BorderLayout.CENTER);
         cp.add(createDRBottom(), BorderLayout.SOUTH);

         return f;
      }

      private JMenuBar createDRMenuBar() {
         JMenuBar mb = new JMenuBar();

         JMenu jmDRFile = new JMenu("File");
         jmDRFile.setMnemonic(KeyEvent.VK_F);
         mb.add(jmDRFile);
         gui.jmiDROpenEdge = new JMenuItem("Open Edge File");
         gui.jmiDROpenEdge.setMnemonic(KeyEvent.VK_E);
         gui.jmiDROpenEdge.addActionListener(menuListener);
         gui.jmiDROpenSave = new JMenuItem("Open Save File");
         gui.jmiDROpenSave.setMnemonic(KeyEvent.VK_V);
         gui.jmiDROpenSave.addActionListener(menuListener);
         gui.jmiDRSave = new JMenuItem("Save");
         gui.jmiDRSave.setMnemonic(KeyEvent.VK_S);
         gui.jmiDRSave.setEnabled(false);
         gui.jmiDRSave.addActionListener(menuListener);
         gui.jmiDRSaveAs = new JMenuItem("Save As...");
         gui.jmiDRSaveAs.setMnemonic(KeyEvent.VK_A);
         gui.jmiDRSaveAs.setEnabled(false);
         gui.jmiDRSaveAs.addActionListener(menuListener);
         gui.jmiDRExit = new JMenuItem("Exit");
         gui.jmiDRExit.setMnemonic(KeyEvent.VK_X);
         gui.jmiDRExit.addActionListener(menuListener);
         jmDRFile.add(gui.jmiDROpenEdge);
         jmDRFile.add(gui.jmiDROpenSave);
         jmDRFile.add(gui.jmiDRSave);
         jmDRFile.add(gui.jmiDRSaveAs);
         jmDRFile.add(gui.jmiDRExit);

         JMenu jmDROptions = new JMenu("Options");
         jmDROptions.setMnemonic(KeyEvent.VK_O);
         mb.add(jmDROptions);
         gui.jmiDROptionsOutputLocation = new JMenuItem("Set Output File Definition Location");
         gui.jmiDROptionsOutputLocation.setMnemonic(KeyEvent.VK_S);
         gui.jmiDROptionsOutputLocation.addActionListener(menuListener);
         gui.jmiDROptionsShowProducts = new JMenuItem("Show Database Products Available");
         gui.jmiDROptionsShowProducts.setMnemonic(KeyEvent.VK_H);
         gui.jmiDROptionsShowProducts.setEnabled(false);
         gui.jmiDROptionsShowProducts.addActionListener(menuListener);
         jmDROptions.add(gui.jmiDROptionsOutputLocation);
         jmDROptions.add(gui.jmiDROptionsShowProducts);

         JMenu jmDRHelp = new JMenu("Help");
         jmDRHelp.setMnemonic(KeyEvent.VK_H);
         mb.add(jmDRHelp);
         gui.jmiDRHelpAbout = new JMenuItem("About");
         gui.jmiDRHelpAbout.setMnemonic(KeyEvent.VK_A);
         gui.jmiDRHelpAbout.addActionListener(menuListener);
         jmDRHelp.add(gui.jmiDRHelpAbout);

         return mb;
      }

      private JComponent createDRCenter() {
         JComponent center = new JPanel(new GridLayout(2, 2));
         JComponent center1 = new JPanel(new BorderLayout());
         JComponent center2 = new JPanel(new BorderLayout());
         JComponent center3 = new JPanel(new BorderLayout());
         JComponent center4 = new JPanel(new BorderLayout());

         gui.dlmDRTablesRelations = new DefaultListModel<>();
         gui.jlDRTablesRelations = new JList<>(gui.dlmDRTablesRelations);
         gui.jlDRTablesRelations.addListSelectionListener(gui::onDRTablesRelationsSelection);

         gui.dlmDRFieldsTablesRelations = new DefaultListModel<>();
         gui.jlDRFieldsTablesRelations = new JList<>(gui.dlmDRFieldsTablesRelations);
         gui.jlDRFieldsTablesRelations.addListSelectionListener(gui::onDRFieldsTablesRelationsSelection);

         gui.dlmDRTablesRelatedTo = new DefaultListModel<>();
         gui.jlDRTablesRelatedTo = new JList<>(gui.dlmDRTablesRelatedTo);
         gui.jlDRTablesRelatedTo.addListSelectionListener(gui::onDRTablesRelatedToSelection);

         gui.dlmDRFieldsTablesRelatedTo = new DefaultListModel<>();
         gui.jlDRFieldsTablesRelatedTo = new JList<>(gui.dlmDRFieldsTablesRelatedTo);
         gui.jlDRFieldsTablesRelatedTo.addListSelectionListener(gui::onDRFieldsTablesRelatedToSelection);

         gui.jspDRTablesRelations = new JScrollPane(gui.jlDRTablesRelations);
         gui.jspDRFieldsTablesRelations = new JScrollPane(gui.jlDRFieldsTablesRelations);
         gui.jspDRTablesRelatedTo = new JScrollPane(gui.jlDRTablesRelatedTo);
         gui.jspDRFieldsTablesRelatedTo = new JScrollPane(gui.jlDRFieldsTablesRelatedTo);
         gui.jlabDRTablesRelations = new JLabel("Tables With Relations", SwingConstants.CENTER);
         gui.jlabDRFieldsTablesRelations = new JLabel("Fields in Tables with Relations", SwingConstants.CENTER);
         gui.jlabDRTablesRelatedTo = new JLabel("Related Tables", SwingConstants.CENTER);
         gui.jlabDRFieldsTablesRelatedTo = new JLabel("Fields in Related Tables", SwingConstants.CENTER);
         center1.add(gui.jlabDRTablesRelations, BorderLayout.NORTH);
         center2.add(gui.jlabDRFieldsTablesRelations, BorderLayout.NORTH);
         center3.add(gui.jlabDRTablesRelatedTo, BorderLayout.NORTH);
         center4.add(gui.jlabDRFieldsTablesRelatedTo, BorderLayout.NORTH);
         center1.add(gui.jspDRTablesRelations, BorderLayout.CENTER);
         center2.add(gui.jspDRFieldsTablesRelations, BorderLayout.CENTER);
         center3.add(gui.jspDRTablesRelatedTo, BorderLayout.CENTER);
         center4.add(gui.jspDRFieldsTablesRelatedTo, BorderLayout.CENTER);
         center.add(center1);
         center.add(center2);
         center.add(center3);
         center.add(center4);

         return center;
      }

      private JComponent createDRBottom() {
         JComponent bottom = new JPanel(new GridLayout(1, 3));

         gui.jbDRDefineTables = new JButton(DEFINE_TABLES);
         gui.jbDRDefineTables.addActionListener((ActionEvent ae) -> gui.showDTScreen());

         gui.jbDRBindRelation = new JButton("Bind/Unbind Relation");
         gui.jbDRBindRelation.setEnabled(false);
         gui.jbDRBindRelation.addActionListener((ActionEvent ae) -> gui.onDRBindRelation());

         gui.jbDRCreateDDL = new JButton("Create DDL");
         gui.jbDRCreateDDL.setEnabled(false);
         gui.jbDRCreateDDL.addActionListener(createDDLListener);

         bottom.add(gui.jbDRDefineTables);
         bottom.add(gui.jbDRBindRelation);
         bottom.add(gui.jbDRCreateDDL);

         return bottom;
      }
   }

   private void showDRScreen() {
      jfDT.setVisible(false);
      jfDR.setVisible(true);
      clearDTControls();
      dlmDTFieldsTablesAll.removeAllElements();
   }

   private void showDTScreen() {
      jfDT.setVisible(true);
      jfDR.setVisible(false);
      clearDRControls();
      depopulateLists();
      populateLists();
   }

   private static void onDTTablesAllSelection(ListSelectionEvent e, EdgeConvertGUI gui, EdgeConvertModel ecModel) {
      int selIndex = gui.jlDTTablesAll.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = gui.dlmDTTablesAll.getElementAt(selIndex);
         ecModel.setCurrentDTTable(selText); //set pointer to the selected table
         int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
         gui.jlDTFieldsTablesAll.clearSelection();
         gui.dlmDTFieldsTablesAll.removeAllElements();
         gui.jbDTMoveUp.setEnabled(false);
         gui.jbDTMoveDown.setEnabled(false);
         for (int currentNativeField : currentNativeFields) {
            gui.dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
         }
      }
      gui.disableControls();
   }

   private static void onDTFieldsTablesAllSelection(ListSelectionEvent e, EdgeConvertGUI gui, EdgeConvertModel ecModel) {
      int selIndex = gui.jlDTFieldsTablesAll.getSelectedIndex();
      if (selIndex >= 0) {
         if (selIndex == 0) {
            gui.jbDTMoveUp.setEnabled(false);
         } else {
            gui.jbDTMoveUp.setEnabled(true);
         }
         if (selIndex == (gui.dlmDTFieldsTablesAll.getSize() - 1)) {
            gui.jbDTMoveDown.setEnabled(false);
         } else {
            gui.jbDTMoveDown.setEnabled(true);
         }
         String selText = gui.dlmDTFieldsTablesAll.getElementAt(selIndex);
         ecModel.setCurrentDTField(selText); //set pointer to the selected field
         gui.enableControls();
         gui.jrbDataType[ecModel.currentDTField.getDataType()].setSelected(true); //select the appropriate radio button, based on value of dataType
         if (gui.jrbDataType[0].isSelected()) { //this is the Varchar radio button
            gui.jbDTVarchar.setEnabled(true); //enable the Varchar button
            gui.jtfDTVarchar.setText(Integer.toString(ecModel.currentDTField.getVarcharValue())); //fill text field with varcharValue
         } else { //some radio button other than Varchar is selected
            gui.jtfDTVarchar.setText(""); //clear the text field
            gui.jbDTVarchar.setEnabled(false); //disable the button
         }
         gui.jcheckDTPrimaryKey.setSelected(ecModel.currentDTField.getIsPrimaryKey()); //clear or set Primary Key checkbox
         gui.jcheckDTDisallowNull.setSelected(ecModel.currentDTField.getDisallowNull()); //clear or set Disallow Null checkbox
         gui.jtfDTDefaultValue.setText(ecModel.currentDTField.getDefaultValue()); //fill text field with defaultValue
      }
   }

   private static void onDTMoveUp(ActionEvent e, EdgeConvertGUI gui, EdgeConvertModel ecModel) {
      int selection = gui.jlDTFieldsTablesAll.getSelectedIndex();
      ecModel.currentDTTable.moveFieldUp(selection);
      //repopulate Fields List
      int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
      gui.jlDTFieldsTablesAll.clearSelection();
      gui.dlmDTFieldsTablesAll.removeAllElements();
      for (int currentNativeField : currentNativeFields) {
         gui.dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
      }
      gui.jlDTFieldsTablesAll.setSelectedIndex(selection - 1);
      gui.dataSaved = false;
   }

   private static void onDTMoveDown(ActionEvent e, EdgeConvertGUI gui, EdgeConvertModel ecModel) {
      int selection = gui.jlDTFieldsTablesAll.getSelectedIndex(); //the original selected index
      ecModel.currentDTTable.moveFieldDown(selection);
      //repopulate Fields List
      int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
      gui.jlDTFieldsTablesAll.clearSelection();
      gui.dlmDTFieldsTablesAll.removeAllElements();
      for (int currentNativeField : currentNativeFields) {
         gui.dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
      }
      gui.jlDTFieldsTablesAll.setSelectedIndex(selection + 1);
      gui.dataSaved = false;
   }

   private void onDRTablesRelationsSelection(ListSelectionEvent event) {
      int selIndex = jlDRTablesRelations.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = dlmDRTablesRelations.getElementAt(selIndex);
         ecModel.setCurrentDRTable1(selText);
         int[] currentNativeFields, currentRelatedTables, currentRelatedFields;
         currentNativeFields = ecModel.currentDRTable1.getNativeFieldsArray();
         currentRelatedTables = ecModel.currentDRTable1.getRelatedTablesArray();
         jlDRFieldsTablesRelations.clearSelection();
         jlDRTablesRelatedTo.clearSelection();
         jlDRFieldsTablesRelatedTo.clearSelection();
         dlmDRFieldsTablesRelations.removeAllElements();
         dlmDRTablesRelatedTo.removeAllElements();
         dlmDRFieldsTablesRelatedTo.removeAllElements();
         for (int currentNativeField : currentNativeFields) {
            dlmDRFieldsTablesRelations.addElement(ecModel.getFieldName(currentNativeField));
         }
         for (int currentRelatedTable : currentRelatedTables) {
            dlmDRTablesRelatedTo.addElement(ecModel.getTableName(currentRelatedTable));
         }
      }
   }

   private void onDRFieldsTablesRelationsSelection(ListSelectionEvent e) {
      int selIndex = jlDRFieldsTablesRelations.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = dlmDRFieldsTablesRelations.getElementAt(selIndex);
         ecModel.setCurrentDRField1(selText);
         if (ecModel.currentDRField1.getFieldBound() == 0) {
            jlDRTablesRelatedTo.clearSelection();
            jlDRFieldsTablesRelatedTo.clearSelection();
            dlmDRFieldsTablesRelatedTo.removeAllElements();
         } else {
            jlDRTablesRelatedTo.setSelectedValue(ecModel.getTableName(ecModel.currentDRField1.getTableBound()), true);
            jlDRFieldsTablesRelatedTo.setSelectedValue(ecModel.getFieldName(ecModel.currentDRField1.getFieldBound()), true);
         }
      }
   }

   private void onDRTablesRelatedToSelection(ListSelectionEvent e) {
      int selIndex = jlDRTablesRelatedTo.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = dlmDRTablesRelatedTo.getElementAt(selIndex);
         ecModel.setCurrentDRTable2(selText);
         int[] currentNativeFields = ecModel.currentDRTable2.getNativeFieldsArray();
         dlmDRFieldsTablesRelatedTo.removeAllElements();
         for (int currentNativeField : currentNativeFields) {
            dlmDRFieldsTablesRelatedTo.addElement(ecModel.getFieldName(currentNativeField));
         }
      }
   }

   private void onDRFieldsTablesRelatedToSelection(ListSelectionEvent e) {
      int selIndex = jlDRFieldsTablesRelatedTo.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = dlmDRFieldsTablesRelatedTo.getElementAt(selIndex);
         ecModel.setCurrentDRField2(selText);
         jbDRBindRelation.setEnabled(true);
      } else {
         jbDRBindRelation.setEnabled(false);
      }
   }

   private void onDRBindRelation() {
      int nativeIndex = jlDRFieldsTablesRelations.getSelectedIndex();
      int relatedField = ecModel.currentDRField2.getNumFigure();
      if (ecModel.currentDRField1.getFieldBound() == relatedField) { //the selected fields are already bound to each other
         int answer = JOptionPane.showConfirmDialog(null, "Do you wish to unbind the relation on field " +
                                                    ecModel.currentDRField1.getName() + "?",
                                                    "Are you sure?", JOptionPane.YES_NO_OPTION);
         if (answer == JOptionPane.YES_OPTION) {
            ecModel.currentDRTable1.setRelatedField(nativeIndex, 0); //clear the related field
            ecModel.currentDRField1.setTableBound(0); //clear the bound table
            ecModel.currentDRField1.setFieldBound(0); //clear the bound field
            jlDRFieldsTablesRelatedTo.clearSelection(); //clear the listbox selection
         }
         return;
      }
      if (ecModel.currentDRField1.getFieldBound() != 0) { //field is already bound to a different field
         int answer = JOptionPane.showConfirmDialog(null, "There is already a relation defined on field " +
                                                    ecModel.currentDRField1.getName() + ", do you wish to overwrite it?",
                                                    "Are you sure?", JOptionPane.YES_NO_OPTION);
         if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
            jlDRTablesRelatedTo.setSelectedValue(ecModel.getTableName(ecModel.currentDRField1.getTableBound()), true); //revert selections to saved settings
            jlDRFieldsTablesRelatedTo.setSelectedValue(ecModel.getFieldName(ecModel.currentDRField1.getFieldBound()), true); //revert selections to saved settings
            return;
         }
      }
      if (ecModel.currentDRField1.getDataType() != ecModel.currentDRField2.getDataType()) {
         JOptionPane.showMessageDialog(null, "The datatypes of " + ecModel.currentDRTable1.getName() + "." +
                                       ecModel.currentDRField1.getName() + " and " + ecModel.currentDRTable2.getName() +
                                       "." + ecModel.currentDRField2.getName() + " do not match.  Unable to bind this relation.");
         return;
      }
      if ((ecModel.currentDRField1.getDataType() == 0) && (ecModel.currentDRField2.getDataType() == 0)) {
         if (ecModel.currentDRField1.getVarcharValue() != ecModel.currentDRField2.getVarcharValue()) {
            JOptionPane.showMessageDialog(null, "The varchar lengths of " + ecModel.currentDRTable1.getName() + "." +
                                          ecModel.currentDRField1.getName() + " and " + ecModel.currentDRTable2.getName() +
                                          "." + ecModel.currentDRField2.getName() + " do not match.  Unable to bind this relation.");
            return;
         }
      }
      ecModel.currentDRTable1.setRelatedField(nativeIndex, relatedField);
      ecModel.currentDRField1.setTableBound(ecModel.currentDRTable2.getNumFigure());
      ecModel.currentDRField1.setFieldBound(ecModel.currentDRField2.getNumFigure());
      JOptionPane.showMessageDialog(null, "Table " + ecModel.currentDRTable1.getName() + ": native field " +
                                    ecModel.currentDRField1.getName() + " bound to table " + ecModel.currentDRTable2.getName() +
                                    " on field " + ecModel.currentDRField2.getName());
      dataSaved = false;
   }

   public static void setReadSuccess(boolean value) {
      readSuccess = value;
   }
   
   public static boolean getReadSuccess() {
      return readSuccess;
   }

   private void enableControls() {
      for (int i = 0; i < strDataType.length; i++) {
         jrbDataType[i].setEnabled(true);
      }
      jcheckDTPrimaryKey.setEnabled(true);
      jcheckDTDisallowNull.setEnabled(true);
      jbDTVarchar.setEnabled(true);
      jbDTDefaultValue.setEnabled(true);
   }
   
   private void disableControls() {
      for (int i = 0; i < strDataType.length; i++) {
         jrbDataType[i].setEnabled(false);
      }
      jcheckDTPrimaryKey.setEnabled(false);
      jcheckDTDisallowNull.setEnabled(false);
      jbDTDefaultValue.setEnabled(false);
      jtfDTVarchar.setText("");
      jtfDTDefaultValue.setText("");
   }
   
   private void clearDTControls() {
      jlDTTablesAll.clearSelection();
      jlDTFieldsTablesAll.clearSelection();
   }
   
   private void clearDRControls() {
      jlDRTablesRelations.clearSelection();
      jlDRTablesRelatedTo.clearSelection();
      jlDRFieldsTablesRelations.clearSelection();
      jlDRFieldsTablesRelatedTo.clearSelection();
   }
   
   private void depopulateLists() {
      dlmDTTablesAll.clear();
      dlmDTFieldsTablesAll.clear();
      dlmDRTablesRelations.clear();
      dlmDRFieldsTablesRelations.clear();
      dlmDRTablesRelatedTo.clear();
      dlmDRFieldsTablesRelatedTo.clear();
   }
   
   private void populateLists() {
      if (readSuccess) {
         jfDT.setVisible(true);
         jfDR.setVisible(false);
         disableControls();
         depopulateLists();
         for (EdgeTable table : ecModel.tables) {
            String tempName = table.getName();
            dlmDTTablesAll.addElement(tempName);
            int[] relatedTables = table.getRelatedTablesArray();
            if (relatedTables.length > 0) {
               dlmDRTablesRelations.addElement(tempName);
            }
         }
      }
      readSuccess = true;
   }
   
   private void saveAs() {
      int returnVal;
      jfcEdge.addChoosableFileFilter(effSave);
      returnVal = jfcEdge.showSaveDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         saveFile = jfcEdge.getSelectedFile();
         if (saveFile.exists ()) {
             int response = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirm Overwrite",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
             if (response == JOptionPane.CANCEL_OPTION) {
                return;
             }
         }
         if (!saveFile.getName().endsWith("sav")) {
            String temp = saveFile.getAbsolutePath() + ".sav";
            saveFile = new File(temp);
         }
         jmiDTSave.setEnabled(true);
         truncatedFilename = saveFile.getName().substring(saveFile.getName().lastIndexOf(File.separator) + 1);
         jfDT.setTitle(DEFINE_TABLES + " - " + truncatedFilename);
         jfDR.setTitle(DEFINE_RELATIONS + " - " + truncatedFilename);
      } else {
         return;
      }
      writeSave();
   }
   
   private void writeSave() {
      if (saveFile == null) {
         return;
      }
      ecModel.writeSave(saveFile);
      dataSaved = true;
   }

   private void setOutputDir() {
      int returnVal;
      File outputDirOld = outputDir;
      alSubclasses = new ArrayList<>();
      alProductNames = new ArrayList<>();

      returnVal = jfcOutputDir.showOpenDialog(null);
      
      if (returnVal == JFileChooser.CANCEL_OPTION) {
         return;
      }

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         outputDir = jfcOutputDir.getSelectedFile();
      }
      
      getOutputClasses();

      if (alProductNames.isEmpty()) {
         JOptionPane.showMessageDialog(null, "The path:\n" + outputDir + "\ncontains no valid output definition files.");
         outputDir = outputDirOld;
         return;
      }
      
      if ((parseFile != null || saveFile != null) && outputDir != null) {
         jbDTCreateDDL.setEnabled(true);
         jbDRCreateDDL.setEnabled(true);
      }

      JOptionPane.showMessageDialog(null, "The available products to create DDL statements are:\n" + displayProductNames());
      jmiDTOptionsShowProducts.setEnabled(true);
      jmiDROptionsShowProducts.setEnabled(true);
   }
   
   private String displayProductNames() {
      StringBuilder sb = new StringBuilder();
      for (String productName : productNames) {
         sb.append(productName).append('\n');
      }
      return sb.toString();
   }
   
   private void getOutputClasses() {
      File[] resultFiles;
      Class<?> resultClass = null;
      Class<?>[] paramTypes = {EdgeTable[].class, EdgeField[].class};
      Class<?>[] paramTypesNull = {};
      Constructor<?> conResultClass;
      Object[] args = {ecModel.tables, ecModel.fields};
      Object objOutput = null;

      resultFiles = outputDir.listFiles();
      alProductNames.clear();
      alSubclasses.clear();
      try {
          for (File resultFile : resultFiles) {
             final String resultFileName = resultFile.getName();
             System.out.println(resultFileName);
              if (!resultFileName.endsWith(".class")) {
                  continue; //ignore all files that are not .class files
              }
              resultClass = Class.forName(resultFileName.substring(0, resultFileName.lastIndexOf(".")));
              if (resultClass.getSuperclass().getName().equals("EdgeConvertCreateDDL")) { //only interested in classes that extend EdgeConvertCreateDDL
                  if (parseFile == null && saveFile == null) {
                      conResultClass = resultClass.getConstructor(paramTypesNull);
                  } else {
                      conResultClass = resultClass.getConstructor(paramTypes);
                      objOutput = conResultClass.newInstance(args);
                  }
                  alSubclasses.add(objOutput);
                  Method getProductName = resultClass.getMethod("getProductName", null);
                  String productName = (String) getProductName.invoke(objOutput, null);
                  alProductNames.add(productName);
              }
          }
      } catch (InstantiationException |
               ClassNotFoundException |
               IllegalAccessException |
               NoSuchMethodException |
               InvocationTargetException ex) {
         ex.printStackTrace();
      }
      if (!alProductNames.isEmpty() && !alSubclasses.isEmpty()) { //do not recreate productName and objSubClasses arrays if the new path is empty of valid files
         productNames = alProductNames.toArray(new String[0]);
         objSubclasses = alSubclasses.toArray(new Object[0]);
      }
   }
   
   private String getSQLStatements() {
      String strSQLString = "";
      String response = (String)JOptionPane.showInputDialog(
                    null,
                    "Select a product:",
                    "Create DDL",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    productNames,
                    null);
                    
      if (response == null) {
         return EdgeConvertGUI.CANCELLED;
      }
      
      int selected;
      for (selected = 0; selected < productNames.length; selected++) {
         if (response.equals(productNames[selected])) {
            break;
         }
      }

      try {
         Class<?> selectedSubclass = objSubclasses[selected].getClass();
         Method getSQLString = selectedSubclass.getMethod("getSQLString", null);
         Method getDatabaseName = selectedSubclass.getMethod("getDatabaseName", null);
         strSQLString = (String)getSQLString.invoke(objSubclasses[selected], null);
         databaseName = (String)getDatabaseName.invoke(objSubclasses[selected], null);
      } catch (IllegalAccessException |
               NoSuchMethodException |
               InvocationTargetException ex) {
         ex.printStackTrace();
      }

      return strSQLString;
   }

   private void writeSQL(String output) {
      jfcEdge.resetChoosableFileFilters();
      File fileToUse = parseFile != null ? parseFile : saveFile;
      String prefix = fileToUse.getAbsolutePath().substring(
              0,
              (fileToUse.getAbsolutePath().lastIndexOf(File.separator) + 1));
      File outputFile = new File(prefix + databaseName + ".sql");
      if (databaseName.isEmpty()) {
         return;
      }
      jfcEdge.setSelectedFile(outputFile);
      int returnVal = jfcEdge.showSaveDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         outputFile = jfcEdge.getSelectedFile();
         if (outputFile.exists ()) {
             int response = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirm Overwrite",
                                                         JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
             if (response == JOptionPane.CANCEL_OPTION) {
                return;
             }
         }
         try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, false)));
            //write the SQL statements
            pw.println(output);
            //close the file
            pw.close();
         } catch (IOException ioe) {
            System.out.println(ioe);
         }
      }
   }

   private void onDisallowNullItemStateChanged(ItemEvent ie) {
      ecModel.currentDTField.setDisallowNull(jcheckDTDisallowNull.isSelected());
      dataSaved = false;
   }

   private void onPrimaryKeyItemStateChanged(ItemEvent ie) {
      ecModel.currentDTField.setIsPrimaryKey(jcheckDTPrimaryKey.isSelected());
      dataSaved = false;
   }

   private void onSetDefaultValueActionPerformed(ActionEvent ae) {
      String prev = jtfDTDefaultValue.getText();
      boolean goodData = false;
      int i = ecModel.currentDTField.getDataType();
      do {
         String result = (String) JOptionPane.showInputDialog(
                 null,
                 "Enter the default value:",
                 "Default Value",
                 JOptionPane.PLAIN_MESSAGE,
                 null,
                 null,
                 prev);

         if ((result == null)) {
            jtfDTDefaultValue.setText(prev);
            return;
         }
         switch (i) {
            case 0: //varchar
               if (result.length() <= Integer.parseInt(jtfDTVarchar.getText())) {
                  jtfDTDefaultValue.setText(result);
                  goodData = true;
               } else {
                  JOptionPane.showMessageDialog(null, "The length of this value must be less than or equal to the Varchar length specified.");
               }
               break;
            case 1: //boolean
               String newResult = result.toLowerCase();
               if (newResult.equals("true") || newResult.equals("false")) {
                  jtfDTDefaultValue.setText(newResult);
                  goodData = true;
               } else {
                  JOptionPane.showMessageDialog(null, "You must input a valid boolean value (\"true\" or \"false\").");
               }
               break;
            case 2: //Integer
               try {
                  int intResult = Integer.parseInt(result);
                  jtfDTDefaultValue.setText(result);
                  goodData = true;
               } catch (NumberFormatException nfe) {
                  JOptionPane.showMessageDialog(null, "\"" + result + "\" is not an integer or is outside the bounds of valid integer values.");
               }
               break;
            case 3: //Double
               try {
                  double doubleResult = Double.parseDouble(result);
                  jtfDTDefaultValue.setText(result);
                  goodData = true;
               } catch (NumberFormatException nfe) {
                  JOptionPane.showMessageDialog(null, "\"" + result + "\" is not a double or is outside the bounds of valid double values.");
               }
               break;
            case 4: //Timestamp
               try {
                  jtfDTDefaultValue.setText(result);
                  goodData = true;
               } catch (Exception e) {

               }
               break;
         }
      } while (!goodData);
      int selIndex = jlDTFieldsTablesAll.getSelectedIndex();
      if (selIndex >= 0) {
         String selText = dlmDTFieldsTablesAll.getElementAt(selIndex);
         ecModel.setCurrentDTField(selText);
         ecModel.currentDTField.setDefaultValue(jtfDTDefaultValue.getText());
      }
      dataSaved = false;
   }

   private void onSetVarcharLengthActionPerformed(ActionEvent ae) {
      String prev = jtfDTVarchar.getText();
      String result = (String) JOptionPane.showInputDialog(
              null,
              "Enter the varchar length:",
              "Varchar Length",
              JOptionPane.PLAIN_MESSAGE,
              null,
              null,
              prev);
      if ((result == null)) {
         jtfDTVarchar.setText(prev);
         return;
      }
      int selIndex = jlDTFieldsTablesAll.getSelectedIndex();
      int varchar;
      try {
         if (result.length() > 5) {
            JOptionPane.showMessageDialog(null, "Varchar length must be greater than 0 and less than or equal to 65535.");
            jtfDTVarchar.setText(Integer.toString(EdgeField.VARCHAR_DEFAULT_LENGTH));
            return;
         }
         varchar = Integer.parseInt(result);
         if (varchar > 0 && varchar <= 65535) { // max length of varchar is 255 before v5.0.3
            jtfDTVarchar.setText(Integer.toString(varchar));
            ecModel.currentDTField.setVarcharValue(varchar);
         } else {
            JOptionPane.showMessageDialog(null, "Varchar length must be greater than 0 and less than or equal to 65535.");
            jtfDTVarchar.setText(Integer.toString(EdgeField.VARCHAR_DEFAULT_LENGTH));
            return;
         }
      } catch (NumberFormatException nfe) {
         JOptionPane.showMessageDialog(null, "\"" + result + "\" is not a number");
         jtfDTVarchar.setText(Integer.toString(EdgeField.VARCHAR_DEFAULT_LENGTH));
         return;
      }
      dataSaved = false;
   }

   class EdgeRadioButtonListener implements ActionListener {
      @Override public void actionPerformed(ActionEvent ae) {
         for (int i = 0; i < jrbDataType.length; i++) {
            if (jrbDataType[i].isSelected()) {
               ecModel.currentDTField.setDataType(i);
               break;
            }
         }
         if (jrbDataType[0].isSelected()) {
            jtfDTVarchar.setText(Integer.toString(EdgeField.VARCHAR_DEFAULT_LENGTH));
            jbDTVarchar.setEnabled(true);
         } else {
            jtfDTVarchar.setText("");
            jbDTVarchar.setEnabled(false);
         }
         jtfDTDefaultValue.setText("");
         ecModel.currentDTField.setDefaultValue("");
         dataSaved = false;
      }
   }
   
   class EdgeWindowListener implements WindowListener {
      @Override public void windowActivated(WindowEvent we) {}
      @Override public void windowClosed(WindowEvent we) {}
      @Override public void windowDeactivated(WindowEvent we) {}
      @Override public void windowDeiconified(WindowEvent we) {}
      @Override public void windowIconified(WindowEvent we) {}
      @Override public void windowOpened(WindowEvent we) {}
      
      @Override public void windowClosing(WindowEvent we) {
         if (!dataSaved) {
            int answer = JOptionPane.showOptionDialog(null,
                "You currently have unsaved data. Would you like to save?",
                "Are you sure?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (answer == JOptionPane.YES_OPTION) {
               if (saveFile == null) {
                  saveAs();
               }
               writeSave();
            }
            if ((answer == JOptionPane.CANCEL_OPTION) || (answer == JOptionPane.CLOSED_OPTION)) {
               if (we.getSource() == jfDT) {
                  jfDT.setVisible(true);
               }
               if (we.getSource() == jfDR) {
                  jfDR.setVisible(true);
               }
               return;
            }
         }
         System.exit(0); //No was selected
      }
   }
   
   class CreateDDLButtonListener implements ActionListener {
      @Override public void actionPerformed(ActionEvent ae) {
         while (outputDir == null) {
            JOptionPane.showMessageDialog(null, "You have not selected a path that contains valid output definition files yet.\nPlease select a path now.");
            setOutputDir();
         }
         getOutputClasses(); //in case outputDir was set before a file was loaded and EdgeTable/EdgeField objects created
         final String sqlString = getSQLStatements();
         if (sqlString.equals(EdgeConvertGUI.CANCELLED)) {
            return;
         }
         writeSQL(sqlString);
      }
   }

   class EdgeMenuListener implements ActionListener {
      @Override public void actionPerformed(ActionEvent ae) {
         if ((ae.getSource() == jmiDTOpenEdge) || (ae.getSource() == jmiDROpenEdge)) {
            openEdgeFile();
         }
         
         if ((ae.getSource() == jmiDTOpenSave) || (ae.getSource() == jmiDROpenSave)) {
            openSaveFile();
         }
         
         if ((ae.getSource() == jmiDTSaveAs) || (ae.getSource() == jmiDRSaveAs) ||
             (ae.getSource() == jmiDTSave) || (ae.getSource() == jmiDRSave)) {
            if ((ae.getSource() == jmiDTSaveAs) || (ae.getSource() == jmiDRSaveAs)) {
               saveAs();
            } else {
               writeSave();
            }
         }
         
         if ((ae.getSource() == jmiDTExit) || (ae.getSource() == jmiDRExit)) {
            if (!dataSaved) {
               int answer = JOptionPane.showOptionDialog(null,
                   "You currently have unsaved data. Would you like to save?",
                   "Are you sure?",
                   JOptionPane.YES_NO_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE,
                   null, null, null);
               if (answer == JOptionPane.YES_OPTION) {
                  if (saveFile == null) {
                     saveAs();
                  }
               }
               if ((answer == JOptionPane.CANCEL_OPTION) || (answer == JOptionPane.CLOSED_OPTION)) {
                  return;
               }
            }
            System.exit(0); //No was selected
         }
         
         if ((ae.getSource() == jmiDTOptionsOutputLocation) || (ae.getSource() == jmiDROptionsOutputLocation)) {
            setOutputDir();
         }

         if ((ae.getSource() == jmiDTOptionsShowProducts) || (ae.getSource() == jmiDROptionsShowProducts)) {
            JOptionPane.showMessageDialog(null, "The available products to create DDL statements are:\n" + displayProductNames());
         }
         
         if ((ae.getSource() == jmiDTHelpAbout) || (ae.getSource() == jmiDRHelpAbout)) {
            JOptionPane.showMessageDialog(null, "EdgeConvert ERD To DDL Conversion Tool\n" +
                                                "by Stephen A. Capperell\n" +
                                                "© 2007-2008");
         }
      } // EdgeMenuListener.actionPerformed()
   } // EdgeMenuListener

   private void openEdgeFile() {
      Optional<File> optParseFile = showOpenEdgeFile();
      if (!optParseFile.isPresent()) {
         return;
      }
      parseFile = optParseFile.get();

      EdgeConvertFileParser ecfp = new EdgeConvertFileParser(parseFile);
      ecfp.openAndParse();
      ecModel.tables = ecfp.getEdgeTables();
      for (EdgeTable table : ecModel.tables) {
         table.makeArrays();
      }
      ecModel.fields = ecfp.getEdgeFields();
      populateLists();
      saveFile = null;
      jmiDTSave.setEnabled(false);
      jmiDRSave.setEnabled(false);
      jmiDTSaveAs.setEnabled(true);
      jmiDRSaveAs.setEnabled(true);
      jbDTDefineRelations.setEnabled(true);

      jbDTCreateDDL.setEnabled(true);
      jbDRCreateDDL.setEnabled(true);

      truncatedFilename = parseFile.getName().substring(parseFile.getName().lastIndexOf(File.separator) + 1);
      jfDT.setTitle(DEFINE_TABLES + " - " + truncatedFilename);
      jfDR.setTitle(DEFINE_RELATIONS + " - " + truncatedFilename);
      dataSaved = true;
   }

   private void openSaveFile() {
      Optional<File> optSaveFile = showOpenSaveFile();
      if (!optSaveFile.isPresent()) {
         return;
      }
      saveFile = optSaveFile.get();

      EdgeConvertFileParser ecfp = new EdgeConvertFileParser(saveFile);
      ecfp.openAndParse();
      ecModel.tables = ecfp.getEdgeTables();
      ecModel.fields = ecfp.getEdgeFields();
      populateLists();
      parseFile = null;
      jmiDTSave.setEnabled(true);
      jmiDRSave.setEnabled(true);
      jmiDTSaveAs.setEnabled(true);
      jmiDRSaveAs.setEnabled(true);
      jbDTDefineRelations.setEnabled(true);

      jbDTCreateDDL.setEnabled(true);
      jbDRCreateDDL.setEnabled(true);

      truncatedFilename = saveFile.getName().substring(saveFile.getName().lastIndexOf(File.separator) + 1);
      jfDT.setTitle(DEFINE_TABLES + " - " + truncatedFilename);
      jfDR.setTitle(DEFINE_RELATIONS + " - " + truncatedFilename);
      dataSaved = true;
   }

   protected Optional<File> showOpenEdgeFile() {
      if (!dataSaved) {
         int answer = JOptionPane.showConfirmDialog(null, "You currently have unsaved data. Continue?",
                 "Are you sure?", JOptionPane.YES_NO_OPTION);
         if (answer != JOptionPane.YES_OPTION) {
            return Optional.empty();
         }
      }
      jfcEdge.addChoosableFileFilter(effEdge);
      int returnVal = jfcEdge.showOpenDialog(null);
      if (returnVal != JFileChooser.APPROVE_OPTION) {
         return Optional.empty();
      }
      return Optional.ofNullable(jfcEdge.getSelectedFile());
   }

   protected Optional<File> showOpenSaveFile() {
      if (!dataSaved) {
         int answer = JOptionPane.showConfirmDialog(null, "You currently have unsaved data. Continue?",
                 "Are you sure?", JOptionPane.YES_NO_OPTION);
         if (answer != JOptionPane.YES_OPTION) {
            return Optional.empty();
         }
      }
      jfcEdge.addChoosableFileFilter(effSave);
      int returnVal = jfcEdge.showOpenDialog(null);
      if (returnVal != JFileChooser.APPROVE_OPTION) {
         return Optional.empty();
      }
      return Optional.ofNullable(jfcEdge.getSelectedFile());
   }
}
