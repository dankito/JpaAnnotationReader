package net.dankito.jpa.annotationreader.config.inheritance;


import net.dankito.jpa.annotationreader.config.DataType;
import net.dankito.jpa.annotationreader.config.PropertyConfig;

import javax.persistence.DiscriminatorType;

public class DiscriminatorColumnConfig extends PropertyConfig {


  protected DiscriminatorColumnConfig() { // for Reflection

  }

  public DiscriminatorColumnConfig(InheritanceEntityConfig entityConfig, String columnName, DiscriminatorType discriminatorType, int length, String columnDefinition) {
    super(entityConfig, columnName);

    setLength(length);
    setColumnDefinition(columnDefinition);

    determineDataType(discriminatorType);
  }

  private void determineDataType(DiscriminatorType discriminatorType) {
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


  public String getDiscriminatorValue(Object object) {
    return ((InheritanceEntityConfig)this.entityConfig).getDiscriminatorValueForEntityClass(object.getClass());
  }


  @Override
  public String toString() {
    return columnName;
  }
}
