package edu.rit.edgeconvert.convert;

import java.util.Arrays;
import java.util.List;

public interface CreateDDLFactory {

   List<Class<? extends CreateDDLFactory>> implementations =
         Arrays.asList(CreateDDLMySQLFactory.class);

   CreateDDL create(EdgeTable[] inputTables, EdgeField[] inputFields);
   CreateDDL create();
}
