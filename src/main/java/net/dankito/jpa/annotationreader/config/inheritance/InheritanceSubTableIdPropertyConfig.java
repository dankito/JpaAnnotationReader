package net.dankito.jpa.annotationreader.config.inheritance;


import net.dankito.jpa.annotationreader.config.DataType;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class InheritanceSubTableIdPropertyConfig extends PropertyConfig {

  protected PropertyConfig inheritanceHierarchyTopLevelIdProperty;

  protected EntityConfig inheritanceHierarchyEntity;


  protected InheritanceSubTableIdPropertyConfig() { // for Reflection

  }

  public InheritanceSubTableIdPropertyConfig(EntityConfig subEntity, InheritanceEntityConfig inheritanceHierarchyEntity, PropertyConfig inheritanceHierarchyTopLevelIdProperty) {
    super(subEntity, "");

    this.inheritanceHierarchyEntity = inheritanceHierarchyEntity;
    this.inheritanceHierarchyTopLevelIdProperty = inheritanceHierarchyTopLevelIdProperty;
  }


  @Override
  public String getColumnName() {
    if(getInheritanceHierarchyTopLevelIdProperty() == null)
      return "id"; // TODO: this is a very bad work around (as on addProperty top level id property is forseeable still null), try to find a better solution
    return getInheritanceHierarchyTopLevelIdProperty().getColumnName();
  }

  @Override
  public String getColumnDefinition() {
    return getInheritanceHierarchyTopLevelIdProperty().getColumnDefinition();
  }

  @Override
  public Class<?> getType() {
    return getInheritanceHierarchyTopLevelIdProperty().getType();
  }

  @Override
  public DataType getDataType() {
    return getInheritanceHierarchyTopLevelIdProperty().getDataType();
  }

  @Override
  public boolean isId() {
//    return getInheritanceHierarchyTopLevelIdProperty().isId();
    return true;
  }

  @Override
  public boolean isGeneratedId() {
    return getInheritanceHierarchyTopLevelIdProperty().isGeneratedIdSequence();
  }

  @Override
  public boolean isGeneratedIdSequence() {
    return getInheritanceHierarchyTopLevelIdProperty().isGeneratedIdSequence();
  }

  @Override
  public String getGeneratedIdSequence() {
    return getInheritanceHierarchyTopLevelIdProperty().getGeneratedIdSequence();
  }

  @Override
  public Field getField() {
    return getInheritanceHierarchyTopLevelIdProperty().getField();
  }

  @Override
  public Method getFieldGetMethod() {
    return getInheritanceHierarchyTopLevelIdProperty().getFieldGetMethod();
  }

  @Override
  public Method getFieldSetMethod() {
    return getInheritanceHierarchyTopLevelIdProperty().getFieldSetMethod();
  }


  public PropertyConfig getInheritanceHierarchyTopLevelIdProperty() {
    if(inheritanceHierarchyTopLevelIdProperty == null)
      this.inheritanceHierarchyTopLevelIdProperty = inheritanceHierarchyEntity.getIdProperty();
    return inheritanceHierarchyTopLevelIdProperty;
  }

  @Override
  public String toString() {
    return "Inherited ID column for Entity " + getEntityConfig();
  }
}
