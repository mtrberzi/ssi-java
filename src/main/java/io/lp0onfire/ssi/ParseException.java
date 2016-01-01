package io.lp0onfire.ssi;

public class ParseException extends Exception {

  private final String message;
  
  public ParseException(String message){
    this.message = message;
  }
  
  @Override
  public String getMessage() {
    return "parse exception: " + message;
  }
  
}
