package edu.rit.edgeconvert.convert;

public class CreateDDLMySQLFactory implements CreateDDLFactory {

   @Override
   public CreateDDLMySQL create(EdgeTable[] inputTables, EdgeField[] inputFields) {
      return new CreateDDLMySQL(inputTables, inputFields);
   }

   @Override
   public CreateDDLMySQL create() {
      return new CreateDDLMySQL();
   }
}
