package net.dankito.jpa.annotationreader.reflection;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.HashMap;


public interface IAnnotationElementsReader {

  HashMap<String, Object> getElements(Annotation annotation) throws SQLException;

}
