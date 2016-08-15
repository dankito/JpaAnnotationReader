package net.dankito.jpa.annotationreader.inheritance;


import net.dankito.jpa.annotationreader.DataType;
import net.dankito.jpa.annotationreader.PropertyConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.DiscriminatorType;

public class DiscriminatorColumnConfig extends PropertyConfig {


  protected DiscriminatorColumnConfig() { // for Reflection

  }

  public DiscriminatorColumnConfig(InheritanceEntityConfig entityConfig, String columnName, DiscriminatorType discriminatorType, int length, String columnDefinition) {
    super(entityConfig, columnName);

    setLength(length);
    setColumnDefinition(columnDefinition);

    switch(discriminatorType) {
      case INTEGER:
        this.type = Integer.class;
        setDataType(DataType.INTEGER);
        break;
      case CHAR:
        this.type = Character.class;
        setDataType(DataType.CHAR);
        break;
      default:
        this.type = String.class;
        setDataType(DataType.STRING);
        break;
    }
  }


  @Override
  public String toString() {
    return columnName;
  }
}
