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
   EdgeMenuListener menuListener;
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
      menuListener = new EdgeMenuListener();
      radioListener = new EdgeRadioButtonListener();
      edgeWindowListener = new EdgeWindowListener();
      createDDLListener = new CreateDDLButtonListener();
      ecModel = new EdgeConvertModel();
      
      setUpLookAndFeel();
      jfDT = createDTScreen();
      jfDR = createDRScreen();
   }

   private static void setUpLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //use the OS native LAF, as opposed to default Java LAF
      } catch (Exception e) {
         System.out.println("Error setting native LAF: " + e);
      }
   }

   /** Creates Define Tables screen. */
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
      jmiDTOpenEdge = new JMenuItem("Open Edge File");
      jmiDTOpenEdge.setMnemonic(KeyEvent.VK_E);
      jmiDTOpenEdge.addActionListener(menuListener);
      jmiDTOpenSave = new JMenuItem("Open Save File");
      jmiDTOpenSave.setMnemonic(KeyEvent.VK_V);
      jmiDTOpenSave.addActionListener(menuListener);
      jmiDTSave = new JMenuItem("Save");
      jmiDTSave.setMnemonic(KeyEvent.VK_S);
      jmiDTSave.setEnabled(false);
      jmiDTSave.addActionListener(menuListener);
      jmiDTSaveAs = new JMenuItem("Save As...");
      jmiDTSaveAs.setMnemonic(KeyEvent.VK_A);
      jmiDTSaveAs.setEnabled(false);
      jmiDTSaveAs.addActionListener(menuListener);
      jmiDTExit = new JMenuItem("Exit");
      jmiDTExit.setMnemonic(KeyEvent.VK_X);
      jmiDTExit.addActionListener(menuListener);
      jmDTFile.add(jmiDTOpenEdge);
      jmDTFile.add(jmiDTOpenSave);
      jmDTFile.add(jmiDTSave);
      jmDTFile.add(jmiDTSaveAs);
      jmDTFile.add(jmiDTExit);

      JMenu jmDTOptions = new JMenu("Options");
      jmDTOptions.setMnemonic(KeyEvent.VK_O);
      mb.add(jmDTOptions);
      jmiDTOptionsOutputLocation = new JMenuItem("Set Output File Definition Location");
      jmiDTOptionsOutputLocation.setMnemonic(KeyEvent.VK_S);
      jmiDTOptionsOutputLocation.addActionListener(menuListener);
      jmiDTOptionsShowProducts = new JMenuItem("Show Database Products Available");
      jmiDTOptionsShowProducts.setMnemonic(KeyEvent.VK_H);
      jmiDTOptionsShowProducts.setEnabled(false);
      jmiDTOptionsShowProducts.addActionListener(menuListener);
      jmDTOptions.add(jmiDTOptionsOutputLocation);
      jmDTOptions.add(jmiDTOptionsShowProducts);

      JMenu jmDTHelp = new JMenu("Help");
      jmDTHelp.setMnemonic(KeyEvent.VK_H);
      mb.add(jmDTHelp);
      jmiDTHelpAbout = new JMenuItem("About");
      jmiDTHelpAbout.setMnemonic(KeyEvent.VK_A);
      jmiDTHelpAbout.addActionListener(menuListener);
      jmDTHelp.add(jmiDTHelpAbout);

      jfcEdge = new JFileChooser();
      jfcOutputDir = new JFileChooser();
      effEdge = new ExampleFileFilter("edg", "Edge Diagrammer Files");
      effSave = new ExampleFileFilter("sav", "Edge Convert Save Files");
      jfcOutputDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      return mb;
   }

   private JComponent createDTBottom() {
      JPanel bottom = new JPanel(new GridLayout(1, 2));

      jbDTCreateDDL = new JButton("Create DDL");
      jbDTCreateDDL.setEnabled(false);
      jbDTCreateDDL.addActionListener(createDDLListener);

      jbDTDefineRelations = new JButton (DEFINE_RELATIONS);
      jbDTDefineRelations.setEnabled(false);
      jbDTDefineRelations.addActionListener(
            (ActionEvent ae) -> {
               jfDT.setVisible(false);
               jfDR.setVisible(true); //show the Define Relations screen
               clearDTControls();
               dlmDTFieldsTablesAll.removeAllElements();
            }
      );

      bottom.add(jbDTDefineRelations);
      bottom.add(jbDTCreateDDL);

      return bottom;
   }

   private JComponent createDTCenter() {
      JComponent center = new JPanel(new GridLayout(1, 3));
      JComponent centerRight = new JPanel(new GridLayout(1, 2));
      dlmDTTablesAll = new DefaultListModel<>();
      jlDTTablesAll = new JList<>(dlmDTTablesAll);
      jlDTTablesAll.addListSelectionListener(
            (ListSelectionEvent lse) -> {
               int selIndex = jlDTTablesAll.getSelectedIndex();
               if (selIndex >= 0) {
                  String selText = dlmDTTablesAll.getElementAt(selIndex);
                  ecModel.setCurrentDTTable(selText); //set pointer to the selected table
                  int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
                  jlDTFieldsTablesAll.clearSelection();
                  dlmDTFieldsTablesAll.removeAllElements();
                  jbDTMoveUp.setEnabled(false);
                  jbDTMoveDown.setEnabled(false);
                  for (int currentNativeField : currentNativeFields) {
                     dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
                  }
               }
               disableControls();
            }
      );

      dlmDTFieldsTablesAll = new DefaultListModel<>();
      jlDTFieldsTablesAll = new JList<>(dlmDTFieldsTablesAll);
      jlDTFieldsTablesAll.addListSelectionListener(
            (ListSelectionEvent lse) -> {
               int selIndex = jlDTFieldsTablesAll.getSelectedIndex();
               if (selIndex >= 0) {
                  if (selIndex == 0) {
                     jbDTMoveUp.setEnabled(false);
                  } else {
                     jbDTMoveUp.setEnabled(true);
                  }
                  if (selIndex == (dlmDTFieldsTablesAll.getSize() - 1)) {
                     jbDTMoveDown.setEnabled(false);
                  } else {
                     jbDTMoveDown.setEnabled(true);
                  }
                  String selText = dlmDTFieldsTablesAll.getElementAt(selIndex);
                  ecModel.setCurrentDTField(selText); //set pointer to the selected field
                  enableControls();
                  jrbDataType[ecModel.currentDTField.getDataType()].setSelected(true); //select the appropriate radio button, based on value of dataType
                  if (jrbDataType[0].isSelected()) { //this is the Varchar radio button
                     jbDTVarchar.setEnabled(true); //enable the Varchar button
                     jtfDTVarchar.setText(Integer.toString(ecModel.currentDTField.getVarcharValue())); //fill text field with varcharValue
                  } else { //some radio button other than Varchar is selected
                     jtfDTVarchar.setText(""); //clear the text field
                     jbDTVarchar.setEnabled(false); //disable the button
                  }
                  jcheckDTPrimaryKey.setSelected(ecModel.currentDTField.getIsPrimaryKey()); //clear or set Primary Key checkbox
                  jcheckDTDisallowNull.setSelected(ecModel.currentDTField.getDisallowNull()); //clear or set Disallow Null checkbox
                  jtfDTDefaultValue.setText(ecModel.currentDTField.getDefaultValue()); //fill text field with defaultValue
               }
            }
      );

      jpDTMove = new JPanel(new GridLayout(2, 1));
      jbDTMoveUp = new JButton("^");
      jbDTMoveUp.setEnabled(false);
      jbDTMoveUp.addActionListener(
            (ActionEvent ae) -> {
               int selection = jlDTFieldsTablesAll.getSelectedIndex();
               ecModel.currentDTTable.moveFieldUp(selection);
               //repopulate Fields List
               int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
               jlDTFieldsTablesAll.clearSelection();
               dlmDTFieldsTablesAll.removeAllElements();
               for (int currentNativeField : currentNativeFields) {
                  dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
               }
               jlDTFieldsTablesAll.setSelectedIndex(selection - 1);
               dataSaved = false;
            }
      );
      jbDTMoveDown = new JButton("v");
      jbDTMoveDown.setEnabled(false);
      jbDTMoveDown.addActionListener(
            (ActionEvent ae) -> {
               int selection = jlDTFieldsTablesAll.getSelectedIndex(); //the original selected index
               ecModel.currentDTTable.moveFieldDown(selection);
               //repopulate Fields List
               int[] currentNativeFields = ecModel.currentDTTable.getNativeFieldsArray();
               jlDTFieldsTablesAll.clearSelection();
               dlmDTFieldsTablesAll.removeAllElements();
               for (int currentNativeField : currentNativeFields) {
                  dlmDTFieldsTablesAll.addElement(ecModel.getFieldName(currentNativeField));
               }
               jlDTFieldsTablesAll.setSelectedIndex(selection + 1);
               dataSaved = false;
            }
      );
      jpDTMove.add(jbDTMoveUp);
      jpDTMove.add(jbDTMoveDown);

      jspDTTablesAll = new JScrollPane(jlDTTablesAll);
      jspDTFieldsTablesAll = new JScrollPane(jlDTFieldsTablesAll);
      JComponent center1 = new JPanel(new BorderLayout());
      JComponent center2 = new JPanel(new BorderLayout());
      jlabDTTables = new JLabel("All Tables", SwingConstants.CENTER);
      jlabDTFields = new JLabel("Fields List", SwingConstants.CENTER);
      center1.add(jlabDTTables, BorderLayout.NORTH);
      center2.add(jlabDTFields, BorderLayout.NORTH);
      center1.add(jspDTTablesAll, BorderLayout.CENTER);
      center2.add(jspDTFieldsTablesAll, BorderLayout.CENTER);
      center2.add(jpDTMove, BorderLayout.EAST);
      center.add(center1);
      center.add(center2);
      center.add(centerRight);

      strDataType = EdgeField.getStrDataType(); //get the list of currently supported data types
      jrbDataType = new JRadioButton[strDataType.length]; //create array of JRadioButtons, one for each supported data type
      bgDTDataType = new ButtonGroup();
      JComponent centerRight1 = new JPanel(new GridLayout(strDataType.length, 1));
      for (int i = 0; i < strDataType.length; i++) {
         jrbDataType[i] = new JRadioButton(strDataType[i]); //assign label for radio button from String array
         jrbDataType[i].setEnabled(false);
         jrbDataType[i].addActionListener(radioListener);
         bgDTDataType.add(jrbDataType[i]);
         centerRight1.add(jrbDataType[i]);
      }
      centerRight.add(centerRight1);

      jcheckDTDisallowNull = new JCheckBox("Disallow Null");
      jcheckDTDisallowNull.setEnabled(false);
      jcheckDTDisallowNull.addItemListener(this::onDisallowNullItemStateChanged);

      jcheckDTPrimaryKey = new JCheckBox("Primary Key");
      jcheckDTPrimaryKey.setEnabled(false);
      jcheckDTPrimaryKey.addItemListener(this::onPrimaryKeyItemStateChanged);

      jbDTDefaultValue = new JButton("Set Default Value");
      jbDTDefaultValue.setEnabled(false);
      jbDTDefaultValue.addActionListener(this::onSetDefaultValueActionPerformed);
      jtfDTDefaultValue = new JTextField();
      jtfDTDefaultValue.setEditable(false);

      jbDTVarchar = new JButton("Set Varchar Length");
      jbDTVarchar.setEnabled(false);
      jbDTVarchar.addActionListener(this::onSetVarcharLengthActionPerformed);
      jtfDTVarchar = new JTextField();
      jtfDTVarchar.setEditable(false);

      JComponent centerRight2 = new JPanel(new GridLayout(6, 1));
      centerRight2.add(jbDTVarchar);
      centerRight2.add(jtfDTVarchar);
      centerRight2.add(jcheckDTPrimaryKey);
      centerRight2.add(jcheckDTDisallowNull);
      centerRight2.add(jbDTDefaultValue);
      centerRight2.add(jtfDTDefaultValue);
      centerRight.add(centerRight1);
      centerRight.add(centerRight2);
      center.add(centerRight);

      return center;
   }

   /** Creates Define Relations screen. */
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
      jmiDROpenEdge = new JMenuItem("Open Edge File");
      jmiDROpenEdge.setMnemonic(KeyEvent.VK_E);
      jmiDROpenEdge.addActionListener(menuListener);
      jmiDROpenSave = new JMenuItem("Open Save File");
      jmiDROpenSave.setMnemonic(KeyEvent.VK_V);
      jmiDROpenSave.addActionListener(menuListener);
      jmiDRSave = new JMenuItem("Save");
      jmiDRSave.setMnemonic(KeyEvent.VK_S);
      jmiDRSave.setEnabled(false);
      jmiDRSave.addActionListener(menuListener);
      jmiDRSaveAs = new JMenuItem("Save As...");
      jmiDRSaveAs.setMnemonic(KeyEvent.VK_A);
      jmiDRSaveAs.setEnabled(false);
      jmiDRSaveAs.addActionListener(menuListener);
      jmiDRExit = new JMenuItem("Exit");
      jmiDRExit.setMnemonic(KeyEvent.VK_X);
      jmiDRExit.addActionListener(menuListener);
      jmDRFile.add(jmiDROpenEdge);
      jmDRFile.add(jmiDROpenSave);
      jmDRFile.add(jmiDRSave);
      jmDRFile.add(jmiDRSaveAs);
      jmDRFile.add(jmiDRExit);

      JMenu jmDROptions = new JMenu("Options");
      jmDROptions.setMnemonic(KeyEvent.VK_O);
      mb.add(jmDROptions);
      jmiDROptionsOutputLocation = new JMenuItem("Set Output File Definition Location");
      jmiDROptionsOutputLocation.setMnemonic(KeyEvent.VK_S);
      jmiDROptionsOutputLocation.addActionListener(menuListener);
      jmiDROptionsShowProducts = new JMenuItem("Show Database Products Available");
      jmiDROptionsShowProducts.setMnemonic(KeyEvent.VK_H);
      jmiDROptionsShowProducts.setEnabled(false);
      jmiDROptionsShowProducts.addActionListener(menuListener);
      jmDROptions.add(jmiDROptionsOutputLocation);
      jmDROptions.add(jmiDROptionsShowProducts);

      JMenu jmDRHelp = new JMenu("Help");
      jmDRHelp.setMnemonic(KeyEvent.VK_H);
      mb.add(jmDRHelp);
      jmiDRHelpAbout = new JMenuItem("About");
      jmiDRHelpAbout.setMnemonic(KeyEvent.VK_A);
      jmiDRHelpAbout.addActionListener(menuListener);
      jmDRHelp.add(jmiDRHelpAbout);

      return mb;
   }

   private JComponent createDRCenter() {
      JComponent center = new JPanel(new GridLayout(2, 2));
      JComponent center1 = new JPanel(new BorderLayout());
      JComponent center2 = new JPanel(new BorderLayout());
      JComponent center3 = new JPanel(new BorderLayout());
      JComponent center4 = new JPanel(new BorderLayout());

      dlmDRTablesRelations = new DefaultListModel<>();
      jlDRTablesRelations = new JList<>(dlmDRTablesRelations);
      jlDRTablesRelations.addListSelectionListener(
            (ListSelectionEvent lse) -> {
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
      );

      dlmDRFieldsTablesRelations = new DefaultListModel<>();
      jlDRFieldsTablesRelations = new JList<>(dlmDRFieldsTablesRelations);
      jlDRFieldsTablesRelations.addListSelectionListener(
            (ListSelectionEvent lse) -> {
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
      );

      dlmDRTablesRelatedTo = new DefaultListModel<>();
      jlDRTablesRelatedTo = new JList<>(dlmDRTablesRelatedTo);
      jlDRTablesRelatedTo.addListSelectionListener(
            (ListSelectionEvent lse) -> {
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
      );

      dlmDRFieldsTablesRelatedTo = new DefaultListModel<>();
      jlDRFieldsTablesRelatedTo = new JList<>(dlmDRFieldsTablesRelatedTo);
      jlDRFieldsTablesRelatedTo.addListSelectionListener(
            (ListSelectionEvent lse) -> {
               int selIndex = jlDRFieldsTablesRelatedTo.getSelectedIndex();
               if (selIndex >= 0) {
                  String selText = dlmDRFieldsTablesRelatedTo.getElementAt(selIndex);
                  ecModel.setCurrentDRField2(selText);
                  jbDRBindRelation.setEnabled(true);
               } else {
                  jbDRBindRelation.setEnabled(false);
               }
            }
      );

      jspDRTablesRelations = new JScrollPane(jlDRTablesRelations);
      jspDRFieldsTablesRelations = new JScrollPane(jlDRFieldsTablesRelations);
      jspDRTablesRelatedTo = new JScrollPane(jlDRTablesRelatedTo);
      jspDRFieldsTablesRelatedTo = new JScrollPane(jlDRFieldsTablesRelatedTo);
      jlabDRTablesRelations = new JLabel("Tables With Relations", SwingConstants.CENTER);
      jlabDRFieldsTablesRelations = new JLabel("Fields in Tables with Relations", SwingConstants.CENTER);
      jlabDRTablesRelatedTo = new JLabel("Related Tables", SwingConstants.CENTER);
      jlabDRFieldsTablesRelatedTo = new JLabel("Fields in Related Tables", SwingConstants.CENTER);
      center1.add(jlabDRTablesRelations, BorderLayout.NORTH);
      center2.add(jlabDRFieldsTablesRelations, BorderLayout.NORTH);
      center3.add(jlabDRTablesRelatedTo, BorderLayout.NORTH);
      center4.add(jlabDRFieldsTablesRelatedTo, BorderLayout.NORTH);
      center1.add(jspDRTablesRelations, BorderLayout.CENTER);
      center2.add(jspDRFieldsTablesRelations, BorderLayout.CENTER);
      center3.add(jspDRTablesRelatedTo, BorderLayout.CENTER);
      center4.add(jspDRFieldsTablesRelatedTo, BorderLayout.CENTER);
      center.add(center1);
      center.add(center2);
      center.add(center3);
      center.add(center4);

      return center;
   }

   private JComponent createDRBottom() {
      JComponent bottom = new JPanel(new GridLayout(1, 3));

      jbDRDefineTables = new JButton(DEFINE_TABLES);
      jbDRDefineTables.addActionListener(
              (ActionEvent ae) -> {
                 jfDT.setVisible(true); //show the Define Tables screen
                 jfDR.setVisible(false);
                 clearDRControls();
                 depopulateLists();
                 populateLists();
              }
      );

      jbDRBindRelation = new JButton("Bind/Unbind Relation");
      jbDRBindRelation.setEnabled(false);
      jbDRBindRelation.addActionListener(
              (ActionEvent ae) -> {
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
      );

      jbDRCreateDDL = new JButton("Create DDL");
      jbDRCreateDDL.setEnabled(false);
      jbDRCreateDDL.addActionListener(createDDLListener);

      bottom.add(jbDRDefineTables);
      bottom.add(jbDRBindRelation);
      bottom.add(jbDRCreateDDL);

      return bottom;
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
      public void actionPerformed(ActionEvent ae) {
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
      public void windowActivated(WindowEvent we) {}
      public void windowClosed(WindowEvent we) {}
      public void windowDeactivated(WindowEvent we) {}
      public void windowDeiconified(WindowEvent we) {}
      public void windowIconified(WindowEvent we) {}
      public void windowOpened(WindowEvent we) {}
      
      public void windowClosing(WindowEvent we) {
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
      public void actionPerformed(ActionEvent ae) {
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
      public void actionPerformed(ActionEvent ae) {
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
} // EdgeConvertGUI
