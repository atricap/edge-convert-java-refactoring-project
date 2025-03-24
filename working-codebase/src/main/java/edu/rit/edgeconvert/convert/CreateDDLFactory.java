package edu.rit.edgeconvert.convert;

import java.util.Arrays;
import java.util.List;

public interface CreateDDLFactory {

   CreateDDL create(EdgeTable[] inputTables, EdgeField[] inputFields);
   CreateDDL create();
}
