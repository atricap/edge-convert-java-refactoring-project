package edu.rit.edgeconvert.convert;

public class ParserException extends Exception {

   private final boolean isShowMessageDialog;

   public ParserException(String message) {
      this(message, false);
   }

   public ParserException(String message, boolean isShowMessageDialog) {
      super(message);
      this.isShowMessageDialog = isShowMessageDialog;
   }

   public boolean isShowMessageDialog() {
      return isShowMessageDialog;
   }
}
