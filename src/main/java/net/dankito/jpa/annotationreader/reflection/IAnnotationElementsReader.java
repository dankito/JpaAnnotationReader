package net.dankito.jpa.annotationreader.reflection;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by ganymed on 15/08/16.
 */
public interface IAnnotationElementsReader {

  HashMap<String, Object> getElements(Annotation annotation) throws SQLException;

}
