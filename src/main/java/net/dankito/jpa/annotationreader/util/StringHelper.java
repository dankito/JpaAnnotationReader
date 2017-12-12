package net.dankito.jpa.annotationreader.util;


public class StringHelper {

  public static boolean isNotNullOrEmpty(String value) {
    return (value != null && value.length() > 0);
  }

}
