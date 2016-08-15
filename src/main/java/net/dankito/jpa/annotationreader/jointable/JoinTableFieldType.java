package net.dankito.jpa.annotationreader.jointable;

import net.dankito.jpa.annotationreader.Property;
import net.dankito.jpa.annotationreader.PropertyConfig;
import net.dankito.jpa.annotationreader.relationconfig.ManyToManyConfig;

import java.sql.SQLException;

/**
 * Created by ganymed on 02/11/14.
 */
public class JoinTableFieldType extends PropertyConfig {


  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType) throws SQLException {
    super(manyToManyConfig.getJoinTableConfig(), fieldName);

    setColumnName(fieldName);
    assignType(dataType);
  }

  // for JoinTable's id column
  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType, boolean isIdColumn, boolean isGeneratedId) throws SQLException {
    this(manyToManyConfig, fieldName, dataType);

    // TODO: what about isIdColumn and isGeneratedId ?
    this.isId = isIdColumn;
    this.isGeneratedId = isGeneratedId;
  }

  @Override
  public String toString() {
    return columnName;
  }
}
