import java.io.*;

public class EdgeConvertModel {

   EdgeTable[] tables; //master copy of EdgeTable objects
   EdgeField[] fields; //master copy of EdgeField objects
   EdgeTable currentDTTable, currentDRTable1, currentDRTable2; //pointers to currently selected table(s) on Define Tables (DT) and Define Relations (DR) screens
   EdgeField currentDTField, currentDRField1, currentDRField2; //pointers to currently selected field(s) on Define Tables (DT) and Define Relations (DR) screens

    void setCurrentDTTable(String selText) {
      for (EdgeTable table : tables) {
         if (selText.equals(table.getName())) {
            currentDTTable = table;
            return;
         }
      }
   }

   void setCurrentDTField(String selText) {
      for (EdgeField field : fields) {
         if (selText.equals(field.getName()) &&
               field.getTableID() == currentDTTable.getNumFigure()) {
            currentDTField = field;
            return;
         }
      }
   }

   void setCurrentDRTable1(String selText) {
      for (EdgeTable table : tables) {
         if (selText.equals(table.getName())) {
            currentDRTable1 = table;
            return;
         }
      }
   }

   void setCurrentDRTable2(String selText) {
      for (EdgeTable table : tables) {
         if (selText.equals(table.getName())) {
            currentDRTable2 = table;
            return;
         }
      }
   }

   void setCurrentDRField1(String selText) {
      for (EdgeField field : fields) {
         if (selText.equals(field.getName()) &&
               field.getTableID() == currentDRTable1.getNumFigure()) {
            currentDRField1 = field;
            return;
         }
      }
   }

   void setCurrentDRField2(String selText) {
      for (EdgeField field : fields) {
         if (selText.equals(field.getName()) &&
               field.getTableID() == currentDRTable2.getNumFigure()) {
            currentDRField2 = field;
            return;
         }
      }
   }

   String getTableName(int numFigure) {
      for (EdgeTable table : tables) {
         if (table.getNumFigure() == numFigure) {
            return table.getName();
         }
      }
      return "";
   }

   String getFieldName(int numFigure) {
      for (EdgeField field : fields) {
         if (field.getNumFigure() == numFigure) {
            return field.getName();
         }
      }
      return "";
   }

   void writeSave(File saveFile) {
      try {
         PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile, false)));
         //write the identification line
         pw.println(EdgeConvertFileParser.SAVE_ID);
         //write the tables
         pw.println("#Tables#");
         for (EdgeTable table : tables) {
            pw.println(table);
         }
         //write the fields
         pw.println("#Fields#");
         for (EdgeField field : fields) {
            pw.println(field);
         }
         //close the file
         pw.close();
      } catch (IOException ioe) {
         System.out.println(ioe);
      }
   }
}
