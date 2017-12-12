package net.dankito.jpa.annotationreader.config.jointable;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.Property;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.relation.ManyToManyConfig;

import java.sql.SQLException;


public class JoinTableConfig extends EntityConfig {

  protected PropertyConfig owningSideJoinColumn;
  protected PropertyConfig inverseSideJoinColumn;

  protected boolean isBidirectional = false;

  protected JoinTableConfig() { // for Reflection

  }

  public JoinTableConfig(ManyToManyConfig manyToManyConfig) throws SQLException {
    super(manyToManyConfig.getJoinTableName(), manyToManyConfig.getFieldTypes());
  }

  public JoinTableConfig(String joinTableName, PropertyConfig owningSideProperty, Class inverseSideClass, String inverseSideJoinColumnName, Property inverseSideProperty) throws SQLException {
    super(joinTableName);

    this.owningSideJoinColumn = new PropertyConfig(this, owningSideProperty.getColumnName());
    owningSideJoinColumn.setTargetEntityClass(owningSideProperty.getTargetEntityClass());
    owningSideJoinColumn.setTargetPropertyConfig(owningSideProperty);
    this.addProperty(owningSideJoinColumn);

    this.inverseSideJoinColumn = new PropertyConfig(this, inverseSideJoinColumnName);
    inverseSideJoinColumn.setTargetEntityClass(inverseSideClass);
    inverseSideJoinColumn.setTargetProperty(inverseSideProperty);
    this.addProperty(inverseSideJoinColumn);

    this.isBidirectional = inverseSideProperty != null;
  }


  @Override
  public boolean isJoinTable() {
    return true;
  }

  public PropertyConfig getInverseSideJoinColumn() {
    return inverseSideJoinColumn;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public String getOwningSideJoinColumnName() {
    return owningSideJoinColumn.getColumnName();
  }

  public String getInverseSideJoinColumnName() {
    return inverseSideJoinColumn.getColumnName();
  }

}
