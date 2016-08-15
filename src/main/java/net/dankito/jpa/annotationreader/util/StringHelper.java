package net.dankito.jpa.annotationreader.util;

/**
 * Created by ganymed on 05/03/15.
 */
public class StringHelper {

  public static boolean isNotNullOrEmpty(String value) {
    return (value != null && value.length() > 0);
  }

}
