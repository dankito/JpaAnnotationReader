package net.dankito.jpa.annotationreader.config.jointable;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.relation.ManyToManyConfig;

import java.sql.SQLException;


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
