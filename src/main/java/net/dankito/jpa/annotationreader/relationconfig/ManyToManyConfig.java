package net.dankito.jpa.annotationreader.relationconfig;

import net.dankito.jpa.annotationreader.EntityConfig;
import net.dankito.jpa.annotationreader.Property;
import net.dankito.jpa.annotationreader.PropertyConfig;
import net.dankito.jpa.annotationreader.jointable.JoinTableConfig;
import net.dankito.jpa.annotationreader.jointable.JoinTableFieldType;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;

/**
 * Created by ganymed on 01/11/14.
 */
public class ManyToManyConfig extends AssociationConfig {

  protected Class owningSideClass;
  protected Field owningSideField;
  protected Property owningSideProperty;

  protected Class inverseSideClass;
  protected Field inverseSideField;
  protected Property inverseSideProperty;

  protected String joinTableName = null;
  protected String joinTableIdColumnName = "id";
  protected String joinTableOwningSideColumnName = null;
  protected String joinTableInverseSideColumnName = null;

  protected JoinTableFieldType idFieldType = null;
  protected JoinTableFieldType owningSideFieldType = null;
  protected JoinTableFieldType inverseSideFieldType = null;
  protected JoinTableFieldType[] fieldTypes = null;

  protected EntityConfig entityConfig = null;


  public ManyToManyConfig(Property owningSideProperty, Class inverseSideClass, FetchType fetch, CascadeType[] cascade) throws SQLException {
    this(owningSideProperty, null, inverseSideClass, fetch, cascade);
  }

  public ManyToManyConfig(Property owningSideProperty, Property inverseSideProperty, FetchType fetch, CascadeType[] cascade) throws SQLException {
    this(owningSideProperty, inverseSideProperty, inverseSideProperty.getDeclaringClass(), fetch, cascade);
  }

  protected ManyToManyConfig(Property owningSideProperty, Property inverseSideProperty, Class inverseSideClass, FetchType fetch,
                             CascadeType[] cascade) throws SQLException {
    super(fetch, cascade, true);

    setOwningSideMembers(owningSideProperty);

    setInverseSideMembers(inverseSideProperty, inverseSideClass);

    createFieldTypes();

    this.isBidirectional = inverseSideProperty != null;

    this.entityConfig = new JoinTableConfig(this);

    determineJoinTableSettings();
  }

  private void setOwningSideMembers(Property owningSideProperty) {
    this.owningSideProperty = owningSideProperty;

    this.owningSideClass = owningSideProperty.getDeclaringClass();
    this.owningSideField = owningSideProperty.getField();
  }

  private void setInverseSideMembers(Property inverseSideProperty, Class inverseSideClass) {
    this.inverseSideClass = inverseSideClass;

    if(inverseSideProperty != null) {
      this.inverseSideProperty = inverseSideProperty;
      this.inverseSideField = inverseSideProperty.getField();
    }
  }

  private void createFieldTypes() throws SQLException {
    this.idFieldType = new JoinTableFieldType(this, getJoinTableIdColumnName(), Long.class, true, true);

    this.owningSideFieldType = new JoinTableFieldType(this, getJoinTableOwningSideColumnName(), Long.class);

    this.inverseSideFieldType = new JoinTableFieldType(this, getJoinTableInverseSideColumnName(), Long.class);

    this.fieldTypes = new JoinTableFieldType[] { getIdFieldType(), getOwningSideFieldType(), getInverseSideFieldType() };
  }


  public boolean isOwningSideField(Field field) {
    return owningSideField.equals(field);
  }

  public Class getOtherSideClass(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return inverseSideClass;
    return owningSideClass;
  }

  public Field getOtherSideField(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return inverseSideField;
    return owningSideField;
  }

  public String getThisSideColumnName(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return getJoinTableOwningSideColumnName();
    return getJoinTableInverseSideColumnName();
  }

  public String getOtherSideColumnName(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return getJoinTableInverseSideColumnName();
    return getJoinTableOwningSideColumnName();
  }


  public Class getOwningSideClass() {
    return owningSideClass;
  }

  public Field getOwningSideField() {
    return owningSideField;
  }

  public Class getInverseSideClass() {
    return inverseSideClass;
  }

  public Field getInverseSideField() {
    return inverseSideField;
  }

  public String getJoinTableName() {
    return joinTableName;
  }

  public String getJoinTableIdColumnName() {
    return joinTableIdColumnName;
  }

  public String getJoinTableOwningSideColumnName() {
    return joinTableOwningSideColumnName;
  }

  public String getJoinTableInverseSideColumnName() {
    return joinTableInverseSideColumnName;
  }

  protected void determineJoinTableSettings() {
    if(owningSideField.isAnnotationPresent(JoinTable.class)) {
      JoinTable joinTableAnnotation = owningSideField.getAnnotation(JoinTable.class);
      joinTableName = joinTableAnnotation.name();
      if(joinTableAnnotation.joinColumns().length != 0)
        joinTableOwningSideColumnName = joinTableAnnotation.joinColumns()[0].name();
      if(joinTableAnnotation.inverseJoinColumns().length != 0)
        joinTableInverseSideColumnName = joinTableAnnotation.inverseJoinColumns()[0].name();
    }
    else
      joinTableName = owningSideClass.getSimpleName().toLowerCase() + "_" + inverseSideClass.getSimpleName().toLowerCase() + "_join_table";

    if(joinTableOwningSideColumnName == null)
      joinTableOwningSideColumnName = owningSideClass.getSimpleName().toLowerCase() + "_id";
    if(joinTableInverseSideColumnName == null)
      joinTableInverseSideColumnName = inverseSideClass.getSimpleName().toLowerCase() + "_id";
  }

  public JoinTableFieldType getIdFieldType() {
    return idFieldType;
  }

  public JoinTableFieldType getOwningSideFieldType() throws SQLException {
    return owningSideFieldType;
  }

  public JoinTableFieldType getInverseSideFieldType() throws SQLException {
    return inverseSideFieldType;
  }

  public PropertyConfig[] getFieldTypes() throws SQLException {
    return fieldTypes;
  }

  public EntityConfig getEntityConfig() throws SQLException {
    return entityConfig;
  }


  @Override
  public String toString() {
    return getJoinTableName();
  }
}
