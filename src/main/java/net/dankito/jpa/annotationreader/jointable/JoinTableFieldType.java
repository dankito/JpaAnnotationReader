package net.dankito.jpa.annotationreader.jointable;

import net.dankito.jpa.annotationreader.Property;
import net.dankito.jpa.annotationreader.PropertyConfig;
import net.dankito.jpa.annotationreader.relationconfig.ManyToManyConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Created by ganymed on 02/11/14.
 */
public class JoinTableFieldType extends PropertyConfig {

  protected Class dataType;


  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, Property property, String fieldName, Class dataType) throws SQLException {
    super(manyToManyConfig.getEntityConfig(), property);

    setColumnName(fieldName);
    this.dataType = dataType;
  }

  // for JoinTable's id column
  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType, boolean isIdColumn, boolean isGeneratedId) throws SQLException {
    super(manyToManyConfig.getEntityConfig(), fieldName);

    setColumnName(fieldName);
    this.dataType = dataType;

    // TODO: what about isIdColumn and isGeneratedId ?
    this.isId = isIdColumn;
    this.isGeneratedId = isGeneratedId;
  }

  @Override
  public Class<?> getType() {
    return dataType;
  }

  @Override
  public String toString() {
    return columnName;
  }
}
