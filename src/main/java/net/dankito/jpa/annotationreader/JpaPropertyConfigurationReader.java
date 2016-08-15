package net.dankito.jpa.annotationreader;

import net.dankito.jpa.annotationreader.config.DataType;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.OrderByConfig;
import net.dankito.jpa.annotationreader.config.Property;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.jointable.JoinTableConfig;
import net.dankito.jpa.annotationreader.reflection.IAnnotationElementsReader;
import net.dankito.jpa.annotationreader.reflection.ReflectionHelper;
import net.dankito.jpa.annotationreader.config.relation.ManyToManyConfig;
import net.dankito.jpa.annotationreader.config.relation.OneToManyConfig;
import net.dankito.jpa.annotationreader.config.relation.OneToOneConfig;
import net.dankito.jpa.annotationreader.util.ConfigRegistry;
import net.dankito.jpa.annotationreader.util.StringHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Created by ganymed on 05/03/15.
 */
public class JpaPropertyConfigurationReader {

  private final static Logger log = LoggerFactory.getLogger(JpaPropertyConfigurationReader.class);


  protected IAnnotationElementsReader annotationElementsReader = null;

  private ConfigRegistry configRegistry;


  public JpaPropertyConfigurationReader() {

  }


  public <T, ID> void readEntityPropertiesConfiguration(EntityConfig<T, ID> entityConfig) throws SQLException {
    // TODO: either remove or implement configureIdProperty
//    Property idProperty = findIdProperty(entityConfig); // advantages of this method is it finds the Id property also in parent @Entity classes - but do i need this?
//    if(idProperty == null)
//      throw new SQLException("@Id not set on any Field or Method of Entity " + entityConfig.getEntityClass() + " or one of its @MappedSuperClass or @Entity super classes");

    // TODO: configure Id property
//    configureIdProperty()

    for(Property entityProperty : ReflectionHelper.getEntityPersistableProperties(entityConfig.getEntityClass())) {
      if(configRegistry.hasPropertyConfiguration(entityConfig.getEntityClass(), entityProperty) == true) // potentially dangerous as Properties on Parent classes
      // can be on multiple Entities, but its EntityConfig value is only set to first Entity's config
        entityConfig.addProperty(configRegistry.getPropertyConfiguration(entityConfig.getEntityClass(), entityProperty));
      else
        entityConfig.addProperty(readPropertyConfiguration(entityConfig, entityProperty));
    }
  }

  public Property findIdProperty(EntityConfig entityConfig) throws SQLException {
    Property foundIdProperty = null;

    for(Property property : ReflectionHelper.getEntityPersistableProperties(entityConfig.getEntityClass())) {
        if(foundIdProperty != null)
          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on property " + property + ". " +
              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");

      foundIdProperty = property;
      if(entityConfig.getAccess() == null) { // only set Access if not already set explicitly through @Access annotation
        if(property.whereIsAnnotationPlaced(Id.class) == Property.AnnotationPlacement.GetMethod)
          entityConfig.setAccess(AccessType.PROPERTY);
        else
          entityConfig.setAccess(AccessType.FIELD);
      }

//      Method fieldGetMethod = ReflectionHelper.findGetMethod(property);
//
//      if(property.isAnnotationPresent(Id.class) && isAnnotatedWithTransient(property) == false) {
//        if(foundIdProperty != null)
//          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on property " + property + ". " +
//              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");
//        if(fieldGetMethod != null && isAnnotatedWithTransient(fieldGetMethod))
//          continue;
//
//        foundIdProperty = new Property(property, fieldGetMethod, ReflectionHelper.findSetMethod(property));
//        if(entityConfig.getAccess() == null) // only set Access if not already set explicitly through @Access annotation
//          entityConfig.setAccess(AccessType.FIELD);
//      }
//      else if(fieldGetMethod != null && fieldGetMethod.isAnnotationPresent(Id.class) && isAnnotatedWithTransient(fieldGetMethod) == false) {
//        if(foundIdProperty != null)
//          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on method " + fieldGetMethod + ". " +
//              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");
//        if(isAnnotatedWithTransient(property))
//          continue;
//
//        foundIdProperty = new Property(property, fieldGetMethod, ReflectionHelper.findSetMethod(property));
//        if(entityConfig.getAccess() == null) // only set Access if not already set explicitly through @Access annotation
//          entityConfig.setAccess(AccessType.PROPERTY);
//      }
    }

    return foundIdProperty;
  }

  protected PropertyConfig readPropertyConfiguration(EntityConfig entityConfig, Property property) throws SQLException {
    PropertyConfig propertyConfig = new PropertyConfig(entityConfig, property, configRegistry);

    configRegistry.registerPropertyConfiguration(entityConfig.getEntityClass(), property, propertyConfig);

    setSqlType(property, propertyConfig);
    readIdConfiguration(property, propertyConfig, entityConfig);
    readVersionConfiguration(property, propertyConfig);
    readBasicAnnotation(property, propertyConfig);
    readColumnAnnotation(property, propertyConfig);

    readRelationConfiguration(property, propertyConfig);

    return propertyConfig;
  }

  protected void setSqlType(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(property.getType() == Date.class || property.getType() == Calendar.class)
      setDateOrCalenderSqlType(property, propertyConfig);
    else if(property.getType().isEnum())
      setEnumSqlType(property, propertyConfig);
//    else if(String.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.STRING);
//    else if(Long.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.LONG_OBJ);
//    else if(Integer.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.INTEGER);
//    else if(Boolean.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.BOOLEAN_OBJ);
//    else if(boolean.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.BOOLEAN);
      // TODO: configure Lob field; set settings according to p. 39/40
    else if(isAnnotationPresent(property, Lob.class)) {
//      DatabaseType databaseType = propertyConfig.getEntityConfig().getDatabaseType();
//      if()
      if(String.class.isAssignableFrom(property.getType()) || char[].class.isAssignableFrom(property.getType()) || Character[].class.isAssignableFrom(property.getType())) {
        propertyConfig.setDataType(DataType.STRING);
        propertyConfig.setColumnDefinition("longvarchar");
      }
      else {
        propertyConfig.setDataType(DataType.BYTE_ARRAY);
        propertyConfig.setColumnDefinition("longvarbinary");
      }
    }
    else {
      for(DataType dataType : DataType.values()) {
        if(property.getType().equals(dataType.getType())) {
          propertyConfig.setDataType(dataType);
          break;
        }
      }
    }

    if(propertyConfig.getDataType() == null) {
      if(isCollectionClass(property.getType())) {
        Class collectionGenericClass = property.getGenericType();
        if(configRegistry.isAnEntityWhichConfigurationShouldBeRead(collectionGenericClass) == false) {
          throwEntityIsNotConfiguredToBeReadException(collectionGenericClass, property);
        }
      }
      else {
        if(isAnnotationPresent(property, OneToOne.class) == false && isAnnotationPresent(property, ManyToOne.class) == false &&
            isAnnotationPresent(property, OneToMany.class) == false && isAnnotationPresent(property, ManyToMany.class) == false) {
          throw new SQLException("Don't know how to serialize Type of Property " + property + ". If it's a relationship, did you forget to set appropriate Annotation (@OneToOne, " +
              "@OneToMany, ...) on its field or get method?");
        }
      }
    }

    // TODO: also set other data type's SQL type
  }

  protected void setDateOrCalenderSqlType(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, Temporal.class) == false) {
      log.warn("@Temporal not set on field " + property + ". According to JPA specification for data types java.util.Date and java.util.Calender @Temporal annotation " +
          "has to be set. Ignoring this java.sql.Timestamp is assumed for " + property.getFieldName());
//      propertyConfig.setSqlType(java.sql.Timestamp.class);
      propertyConfig.setDataType(DataType.DATE_LONG);
    }
    else {
      Temporal temporalAnnotation = getPropertyAnnotation(property, Temporal.class);
      Map<String, Object> elements = annotationElementsReader.getElements(temporalAnnotation);
      switch((TemporalType)elements.get("value")) {
        case DATE:
//          propertyConfig.setSqlType(java.sql.Date.class);
          propertyConfig.setDataType(DataType.DATE);
          break;
        case TIME:
//          propertyConfig.setSqlType(java.sql.Time.class);
          propertyConfig.setDataType(DataType.DATE_LONG);
          break;
        default:
//          propertyConfig.setSqlType(java.sql.Timestamp.class);
          propertyConfig.setDataType(DataType.DATE_LONG);
          break;
      }
    }
  }

  protected void setEnumSqlType(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, Enumerated.class)) {
      Enumerated enumeratedAnnotation = getPropertyAnnotation(property, Enumerated.class);
      Map<String, Object> elements = annotationElementsReader.getElements(enumeratedAnnotation);
      if((EnumType)elements.get("value") == EnumType.STRING) {
//        propertyConfig.setSqlType(String.class);
        propertyConfig.setDataType(DataType.ENUM_STRING);
        return;
      }
    }

//    propertyConfig.setSqlType(Integer.class);
    propertyConfig.setDataType(DataType.ENUM_INTEGER);
  }

  protected void readIdConfiguration(Property property, PropertyConfig propertyConfig, EntityConfig entityConfig) throws SQLException {
    if(isAnnotationPresent(property, Id.class)) {
      propertyConfig.setIsId(true);
      entityConfig.setIdProperty(propertyConfig);

      if(entityConfig.getAccess() == null) { // if access != null than it has been set by @AccessAnnotation
        if (property.whereIsAnnotationPlaced(Id.class) == Property.AnnotationPlacement.GetMethod) // otherwise access is determined where @Id Annotation is placed, on field or get method
          entityConfig.setAccess(AccessType.PROPERTY);
        else
          entityConfig.setAccess(AccessType.FIELD);
      }

      if(isAnnotationPresent(property, GeneratedValue.class)) {
        propertyConfig.setIsGeneratedId(true);
        GeneratedValue generatedValueAnnotation = getPropertyAnnotation(property, GeneratedValue.class);
        Map<String, Object> elements = annotationElementsReader.getElements(generatedValueAnnotation);
        propertyConfig.setGeneratedIdType((GenerationType)elements.get("strategy"));

        if(StringHelper.isNotNullOrEmpty(generatedValueAnnotation.generator()))
          log.warn("Attribute generator of Annotation GeneratedValue (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
      }

      if(isAnnotationPresent(property, SequenceGenerator.class))
        throwAnnotationNotSupportedException("SequenceGenerator", property );
      if(isAnnotationPresent(property, TableGenerator.class))
        throwAnnotationNotSupportedException("TableGenerator", property);
    }
  }

  protected void readVersionConfiguration(Property property, PropertyConfig propertyConfig) {
    if(isAnnotationPresent(property, Version.class)) {
      propertyConfig.setIsVersion(true);
    }
  }

  protected void readBasicAnnotation(Property property, PropertyConfig propertyConfig) throws SQLException{
    if(isAnnotationPresent(property, Basic.class)) {
      Basic basicAnnotation = getPropertyAnnotation(property, Basic.class);
      Map<String, Object> elements = annotationElementsReader.getElements(basicAnnotation);

      propertyConfig.setFetch((FetchType) elements.get("fetch"));
      propertyConfig.setCanBeNull((boolean)elements.get("optional"));
    }
    // no Annotations neither on Field nor on Get-Method - then per default property gets treated as if
    // @Basic(fetch = FetchType.EAGER, optional = true)
    // would be set
    else if((property.getField() == null || property.getField().getAnnotations().length == 0) &&
        (property.getGetMethod() == null || property.getGetMethod().getAnnotations().length == 0)) {
      propertyConfig.setFetch(FetchType.EAGER);
      propertyConfig.setCanBeNull(true);
    }
  }

  protected void readColumnAnnotation(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, Column.class)) {
      Column columnAnnotation = getPropertyAnnotation(property, Column.class);
      Map<String, Object> elements = annotationElementsReader.getElements(columnAnnotation);

      if(StringHelper.isNotNullOrEmpty((String) elements.get("name")))
        propertyConfig.setColumnName((String)elements.get("name"));
      if(StringHelper.isNotNullOrEmpty((String) elements.get("columnDefinition")))
        propertyConfig.setColumnDefinition((String)elements.get("columnDefinition"));

      propertyConfig.setUnique((boolean)elements.get("unique"));
      propertyConfig.setCanBeNull((boolean)elements.get("nullable"));
      propertyConfig.setInsertable((boolean)elements.get("insertable"));
      propertyConfig.setUpdatable((boolean)elements.get("updatable"));
      propertyConfig.setLength((int)elements.get("length"));

      if(StringHelper.isNotNullOrEmpty((String) elements.get("table")))
        throwAttributeNotSupportedException("table", "Column", property);

      if((int)elements.get("precision") > 0 || (int)elements.get("scale") > 0)
        throwAttributeNotSupportedException("precision", "Column", property);
    }
  }


  protected void readRelationConfiguration(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, OneToOne.class))
      readOneToOneConfiguration(property, propertyConfig, getPropertyAnnotation(property, OneToOne.class));
    if(isAnnotationPresent(property, ManyToOne.class))
      readManyToOneConfiguration(property, propertyConfig, getPropertyAnnotation(property, ManyToOne.class));
    if(isAnnotationPresent(property, OneToMany.class))
      readOneToManyConfiguration(property, propertyConfig, getPropertyAnnotation(property, OneToMany.class));
    if(isAnnotationPresent(property, ManyToMany.class))
      readManyToManyConfiguration(property, propertyConfig, getPropertyAnnotation(property, ManyToMany.class));
  }

  protected void readOneToOneConfiguration(Property property, PropertyConfig propertyConfig, OneToOne oneToOneAnnotation) throws SQLException {
    propertyConfig.setIsOneCardinalityRelationshipProperty(true);
    propertyConfig.setIsOneToOneField(true);
    readJoinColumnConfiguration(property, propertyConfig);
    HashMap<String, Object> elements = annotationElementsReader.getElements(oneToOneAnnotation);

    Class targetEntityClass = getTargetEntityClass(property, elements);
    propertyConfig.setTargetEntityClass(targetEntityClass);

    FetchType fetch = (FetchType) elements.get("fetch");
    CascadeType[] cascade = (CascadeType[]) elements.get("cascade");
    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);

    if ((boolean) elements.get("optional") == false) // don't overwrite a may previously set value by JoinColumn
      propertyConfig.setCanBeNull((boolean) elements.get("optional")); // TODO: what's the difference between JoinColumn.nullable() and OneToOne.optional() ?

    if (fetch == FetchType.LAZY)
      log.warn("FetchType.LAZY as on property " + property + " is not supported for @OneToOne relationships as this would require Proxy Generation or Byte code manipulation " +
          "like with JavaAssist,  which is not supported on Android. As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.");

    if ((boolean) elements.get("orphanRemoval") == true)
      throwAttributeNotSupportedException("orphanRemoval", "OneToOne", property);

    String joinColumnName = getJoinColumnName(property, targetEntityClass);

    configureOneToOneTargetProperty(property, propertyConfig, oneToOneAnnotation, targetEntityClass, fetch, cascade, joinColumnName);
  }

  protected void configureOneToOneTargetProperty(Property property, PropertyConfig propertyConfig, OneToOne oneToOneAnnotation, Class targetEntityClass, FetchType fetch, CascadeType[] cascade, String joinColumnName) throws SQLException {
    Map<String, Object> elements = annotationElementsReader.getElements(oneToOneAnnotation);
    String mappedBy = (String)elements.get("mappedBy");
    Property targetProperty = findOneToOneTargetProperty(property, mappedBy, targetEntityClass);

    if(targetProperty == null) { // unidirectional association
      propertyConfig.setIsJoinColumn(true);
      propertyConfig.setIsOwningSide(true);
      propertyConfig.setColumnName(joinColumnName);
      propertyConfig.setOneToOneConfig(new OneToOneConfig(property, targetEntityClass, joinColumnName, fetch, cascade)); // TODO: remove
    }
    else { // bidirectional @OneToOne association
      if(isAnnotationPresent(targetProperty, JoinColumn.class))
        joinColumnName = getPropertyAnnotation(targetProperty, JoinColumn.class).name(); // TODO: this should be false as if otherside has the JoinColumn i don't need to set it here

      Property owningSide;
      Property inverseSide;

      if (StringHelper.isNotNullOrEmpty(mappedBy)) {
        propertyConfig.setIsInverseSide(true);
        propertyConfig.setIsBidirectional(true);
        propertyConfig.setTargetProperty(targetProperty);

        owningSide = targetProperty; // TODO: remove
        inverseSide = property;
        propertyConfig.setOneToOneConfig(new OneToOneConfig(owningSide, inverseSide, joinColumnName, fetch, cascade));
      }
      else if (StringHelper.isNotNullOrEmpty(getPropertyAnnotation(targetProperty, OneToOne.class).mappedBy())) { // TODO: also replace this Annotation member invocation
        propertyConfig.setIsOwningSide(true);
        propertyConfig.setIsJoinColumn(true);
        propertyConfig.setIsBidirectional(true);
        propertyConfig.setColumnName(joinColumnName);
        propertyConfig.setTargetProperty(targetProperty);

        owningSide = property; // TODO: remove
        inverseSide = targetProperty;
        propertyConfig.setOneToOneConfig(new OneToOneConfig(owningSide, inverseSide, joinColumnName, fetch, cascade));
      }
      else { // if on both side mappedBy is not set we have two unidirectional relationships instead of one bidirectional
        log.warn("Just for case that this was not on purpose: On both @OneToOne sides no mappedBy value has been found, so two unidirectional relationships instead of one " +
            "bidirectional one will be created for properties " + property + " and " + targetProperty);
        propertyConfig.setIsOwningSide(true);
        propertyConfig.setIsJoinColumn(true);
      }
    }

    propertyConfig.setColumnName(joinColumnName); // TODO: really set Column Name even if it's not a Join Column?
  }

  protected Property findOneToOneTargetProperty(Property property, String mappedBy, Class targetEntityType) throws SQLException {
    Class classContainingField = property.getDeclaringClass();

    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityType)) { // TODO: search for properties, not for fields
      if(classContainingField.equals(targetProperty.getType())) {
        if (StringHelper.isNotNullOrEmpty(mappedBy) && mappedBy.equals(targetProperty.getFieldName()))
          return targetProperty;
        else if (isAnnotationPresent(targetProperty, OneToOne.class)) {
          OneToOne oneToOneAnnotation = getPropertyAnnotation(targetProperty, OneToOne.class);
          Map<String, Object> elements = annotationElementsReader.getElements(oneToOneAnnotation);
          String targetMappedBy = (String)elements.get("mappedBy");
          if (StringHelper.isNotNullOrEmpty(targetMappedBy) && targetMappedBy.equals(property.getFieldName()))
            return targetProperty;
        }
      }
    }

    return null; // an unidirectional OneToOne association
  }

  protected Property findOneToManyTargetProperty(Property property, String mappedBy, Class targetEntityType) {
    Class classContainingField = property.getDeclaringClass();

    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityType)) {
      if(classContainingField.equals(targetProperty.getType())) {
        if (StringHelper.isNotNullOrEmpty(mappedBy) && mappedBy.equals(targetProperty.getFieldName()))
          return targetProperty;
      }
    }

    return null; // an unidirectional OneToMany association
  }

  protected Property findManyToOneTargetProperty(Property property, Class oneSideClass) throws SQLException {
    for(Property targetClassProperty : ReflectionHelper.getEntityPersistableProperties(oneSideClass)) {
      if(isAnnotationPresent(targetClassProperty, OneToMany.class)) {
        OneToMany oneToManyAnnotation = getPropertyAnnotation(targetClassProperty, OneToMany.class);
        Map<String, Object> elements = annotationElementsReader.getElements(oneToManyAnnotation);
        String mappedBy = (String)elements.get("mappedBy");
        if(property.getFieldName().equals(mappedBy)) {
          // now also check if it's the correct target entity type
          Class oneSideTargetEntity = getTargetEntityClassForOneToMany(targetClassProperty, elements);
          if(property.getDeclaringClass().equals(oneSideTargetEntity))
            return targetClassProperty;
        }
      }
    }

//    throw new SQLException("Could not find @OneToMany field on class " + oneSideClass.getName() + " for @ManyToOne field " + property.toString());
    return null; // TODO: what consequences does it have returning null?
  }

  protected Property findManyToManyTargetProperty(Property property, String mappedBy, Class targetEntityClass) throws SQLException {
    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityClass)) {
      if(isAnnotationPresent(targetProperty, ManyToMany.class)) {
        ManyToMany manyToManyAnnotation = getPropertyAnnotation(targetProperty, ManyToMany.class);
        Map<String, Object> elements = annotationElementsReader.getElements(manyToManyAnnotation);
        String targetMappedBy = (String)elements.get("mappedBy");
        Class targetTargetEntity = (Class)elements.get("targetEntity");

        if (StringHelper.isNotNullOrEmpty(targetMappedBy) && targetMappedBy.equals(property.getFieldName())) {
          if(targetTargetEntity != void.class) {
            if(targetTargetEntity.isAssignableFrom(property.getDeclaringClass()))
              return targetProperty;
          }
          else if(targetProperty.isGenericType() && targetProperty.getGenericType().equals(property.getDeclaringClass()))
            return targetProperty;
        }
      }

      if (StringHelper.isNotNullOrEmpty(mappedBy) && mappedBy.equals(targetProperty.getFieldName()) && isCollectionClass(targetProperty.getType())) {
        if(targetProperty.isGenericType()) {
          if(targetProperty.getGenericType().equals(property.getDeclaringClass()))
            return targetProperty;
        }
        else if(targetEntityClass.equals(targetProperty.getDeclaringClass()))
          return targetProperty;
      }
    }

    return null; // an unidirectional ManyToMany association
  }

  protected void readManyToOneConfiguration(Property property, PropertyConfig propertyConfig, ManyToOne manyToOneAnnotation) throws SQLException {
    propertyConfig.setIsManyToOneField(true);
    propertyConfig.setIsManyCardinalityRelationshipProperty(true);
    readJoinColumnConfiguration(property, propertyConfig);

    Map<String, Object> elements = annotationElementsReader.getElements(manyToOneAnnotation);

    Class targetEntityClass = getTargetEntityClass(property, elements);
    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = (CascadeType[])elements.get("cascade");
    FetchType fetch = (FetchType)elements.get("fetch");
    propertyConfig.setFetch(fetch);
    propertyConfig.setCascade(cascade);

    if((boolean)elements.get("optional") == false) // don't overwrite a may previously set value by JoinColumn
      propertyConfig.setCanBeNull((boolean)elements.get("optional")); // TODO: what's the difference between JoinColumn.nullable() and OneToOne.optional() ?
    if(fetch == FetchType.LAZY)
      log.warn("FetchType.LAZY as on property " + property + " is not supported for @ManyToOne relationships as this would require Proxy Generation or Byte code manipulation " +
          "like with JavaAssist,  which is not supported on Android. As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.");

    propertyConfig.setIsOwningSide(true);
    propertyConfig.setIsJoinColumn(true);
    String joinColumnName = getJoinColumnName(property, targetEntityClass);
    propertyConfig.setColumnName(joinColumnName);

    Property targetProperty = findManyToOneTargetProperty(property, targetEntityClass);
    if(targetProperty != null) {
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setTargetProperty(targetProperty);
      propertyConfig.setOneToManyConfig(new OneToManyConfig(targetProperty, property, joinColumnName, fetch, cascade)); // TODO: remove
    }
    else
      propertyConfig.setOneToManyConfig(new OneToManyConfig(property, joinColumnName, fetch, cascade)); // TODO: remove
  }

  protected String getJoinColumnName(Property property, Class targetEntityClass) throws SQLException {
    String joinColumnName = targetEntityClass.getSimpleName().toLowerCase() + "_id"; // TODO: this is wrong! Don't assume other side's identity column to automatically called 'id', get real column name

    if(isAnnotationPresent(property, JoinColumn.class)) {
      JoinColumn joinColumn = getPropertyAnnotation(property, JoinColumn.class);
      Map<String, Object> elements = annotationElementsReader.getElements(joinColumn);
      String name = (String)elements.get("name");

      if(StringHelper.isNotNullOrEmpty(name))
        joinColumnName = name;
    }

    return joinColumnName;
  }

  protected Class getTargetEntityClass(Property property, Map<String, Object> elements) throws SQLException{
    Class targetEntityClass = property.getType();

    Class targetTargetEntity = (Class)elements.get("targetEntity");
    if(targetTargetEntity != void.class) {
      targetEntityClass = targetTargetEntity;
    }

    checkIfIsValidTargetEntity(targetEntityClass, property);

    return targetEntityClass;
  }

  protected Class getTargetEntityClassForOneToMany(Property property, Map<String, Object> elements) throws SQLException{
    Class targetEntityClass = (Class)elements.get("targetEntity");
    if(targetEntityClass != void.class) {
      checkIfIsValidTargetEntity(targetEntityClass, property);
      return targetEntityClass;
    }

    if(property.isGenericType() == false) {
      throw new SQLException("For @OneToMany property " + property + " either Annotation's targetEntity value has to be set or Field's Datatype has to be a generic " +
          "Collection / Set with  generic type set to target entity's type.");
    }

    targetEntityClass = property.getGenericType();
    checkIfIsValidTargetEntity(targetEntityClass, property);

    return targetEntityClass;
  }

  protected Class getTargetEntityClassForToManyAnnotation(Property property, Map<String, Object> elements, String annotationName) throws SQLException {
    Class targetEntityClass = null;

    if(isCollectionClass(property.getType()) == false) { // property type has to be assignable to Collection
      throw new SQLException("Type of @" + annotationName + " property " + property + " has to be assignable to a java.util.Collection");
    }
    else if ((Class)elements.get("targetEntity") != void.class) { // and either targetEntity value has to be set on @OneToMany Annotation
      targetEntityClass = (Class) elements.get("targetEntity");
    }
    else if (property.isGenericType() == false) { // or generic type has to be set on Collection
      throw new SQLException("Target Type of @" + annotationName + " property " + property + " has be set on targetEntity attribute of @" + annotationName + "" +
          " Annotation or by specifying generic type of " +
          "java.util.Collection derived property data type (e.g. Collection<String>, Set<MyEntity>, ...)");
    }
    else {
      targetEntityClass = property.getGenericType();
    }

    checkIfIsValidTargetEntity(targetEntityClass, property);

    return targetEntityClass;
  }

  protected void checkIfIsValidTargetEntity(Class targetEntityClass, Property property) throws SQLException {
    if(configRegistry.isAnEntityWhichConfigurationShouldBeRead(targetEntityClass) == false) {
      throwEntityIsNotConfiguredToBeReadException(targetEntityClass, property);
    }
  }

  protected void readJoinColumnConfiguration(Property property, PropertyConfig propertyConfig) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);

    if(isAnnotationPresent(property, JoinColumn.class)) {
      propertyConfig.setIsJoinColumn(true);

      JoinColumn joinColumnAnnotation = getPropertyAnnotation(property, JoinColumn.class);
      readJoinColumnConfiguration(property, propertyConfig, joinColumnAnnotation);
    }
  }

  protected void readJoinColumnConfiguration(Property property, PropertyConfig propertyConfig, JoinColumn joinColumnAnnotation) throws SQLException {
    Map<String, Object> elements = annotationElementsReader.getElements(joinColumnAnnotation);
    String name = (String)elements.get("name");
    String columnDefinition = (String)elements.get("columnDefinition");

    if (StringHelper.isNotNullOrEmpty(name))
      propertyConfig.setColumnName(name);
    if(StringHelper.isNotNullOrEmpty(columnDefinition))
      propertyConfig.setColumnDefinition(columnDefinition);

    propertyConfig.setCanBeNull((boolean)elements.get("nullable"));
    propertyConfig.setUnique((boolean) elements.get("unique"));
    propertyConfig.setInsertable((boolean) elements.get("insertable"));
    propertyConfig.setUpdatable((boolean) elements.get("updatable"));

    String referencedColumnName = (String)elements.get("referencedColumnName");
    String table = (String)elements.get("table");
    if(StringHelper.isNotNullOrEmpty(referencedColumnName))
      throwAttributeNotSupportedException("referencedColumnName", "JoinColumn", property);
    if(StringHelper.isNotNullOrEmpty(table))
      throwAttributeNotSupportedException("table", "JoinColumn", property);
  }

  protected void readOneToManyConfiguration(Property property, PropertyConfig propertyConfig, OneToMany oneToManyAnnotation) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);
    propertyConfig.setIsOneCardinalityRelationshipProperty(true);
    propertyConfig.setIsOneToManyField(true);
    propertyConfig.getEntityConfig().addForeignCollection(propertyConfig);

    Map<String, Object> elements = annotationElementsReader.getElements(oneToManyAnnotation);

    Class targetEntityClass = getTargetEntityClassForToManyAnnotation(property, elements, "OneToMany");
    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = (CascadeType[])elements.get("cascade");
    FetchType fetch = (FetchType)elements.get("fetch");
    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);

    if ((boolean)elements.get("orphanRemoval") == true)
      throwAttributeNotSupportedException("orphanRemoval", "OneToOne", property);

    String mappedBy = (String)elements.get("mappedBy");
    if (StringHelper.isNotNullOrEmpty(mappedBy)) {
      try {
        configureBidirectionalOneToManyField(property, propertyConfig, oneToManyAnnotation, fetch, cascade, targetEntityClass);
      } catch (Exception ex) {
        propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
        log.error("Could not configure bidirectional OneToMany field for property " + property, ex);
        throw new SQLException(ex);
      }
    }
    else { // ok, this means relation is not bidirectional
      configureUnidirectionalOneToManyField(property, propertyConfig, targetEntityClass, fetch, cascade);
      // TODO: unidirectional means we have to create a Join Table, this case is not supported yet
      throw new SQLException("Sorry, but unidirectional @OneToMany associations as for property " + property + " are not supported yet by this implementation. Please add a @ManyToOne field on the many side.");
    }

    readOrderByAnnotation(property, propertyConfig, targetEntityClass);
  }

  protected void configureBidirectionalOneToManyField(Property property, PropertyConfig propertyConfig, OneToMany oneToManyAnnotation, FetchType fetchType, CascadeType[] cascade, Class targetEntityClass) throws NoSuchFieldException, SQLException {
    Property targetProperty = findOneToManyTargetProperty(property, oneToManyAnnotation.mappedBy(), targetEntityClass);
    if(targetProperty != null) {
      propertyConfig.setTargetProperty(targetProperty);
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setIsInverseSide(true);
    }

    String joinColumnName = getJoinColumnName(targetProperty, targetEntityClass);
    propertyConfig.setOneToManyConfig(new OneToManyConfig(property, targetProperty, joinColumnName, fetchType, cascade)); // TODO: try to remove
  }

  protected void configureUnidirectionalOneToManyField(Property property, PropertyConfig propertyConfig, Class targetEntityClass, FetchType fetchType, CascadeType[] cascade) throws SQLException {
    propertyConfig.setIsBidirectional(false);
    propertyConfig.setIsOwningSide(true); // TODO: is this correct?

//    String joinColumnName = getOneToManyJoinColumnName(oneSideField);
//    propertyConfig.setOneToManyConfig(new OneToManyConfig(property, joinColumnName, fetchType, cascade));
  }

  protected void readManyToManyConfiguration(Property property, PropertyConfig propertyConfig, ManyToMany manyToManyAnnotation) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);
    propertyConfig.setIsManyCardinalityRelationshipProperty(true);
    propertyConfig.setIsManyToManyField(true);
    propertyConfig.getEntityConfig().addForeignCollection(propertyConfig);

    Map<String, Object> elements = annotationElementsReader.getElements(manyToManyAnnotation);

    Class targetEntityClass = getTargetEntityClassForToManyAnnotation(property, elements, "ManyToMany");

    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = (CascadeType[])elements.get("cascade");
    FetchType fetch = (FetchType)elements.get("fetch");
    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);

    String mappedBy = (String)elements.get("mappedBy");
    Property targetProperty = findManyToManyTargetProperty(property, mappedBy, targetEntityClass);

    ManyToManyConfig manyToManyConfig;

    if(targetProperty == null) {
      propertyConfig.setIsOwningSide(true);
      propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
      readJoinTableAnnotation(property, propertyConfig, targetEntityClass, null);
      manyToManyConfig = new ManyToManyConfig(property, targetEntityClass, fetch, cascade);
    }
    else {
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setTargetProperty(targetProperty);

      Property owningSideProperty;
      Property inverseSideProperty;

      if(StringHelper.isNotNullOrEmpty(mappedBy)) {
        propertyConfig.setIsInverseSide(true);
        inverseSideProperty = property;
        owningSideProperty = targetProperty;
      }
      else {
        propertyConfig.setIsOwningSide(true);
        propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
        readJoinTableAnnotation(property, propertyConfig, targetEntityClass, targetProperty);
        owningSideProperty = property;
        inverseSideProperty = targetProperty;
      }

      manyToManyConfig = new ManyToManyConfig(owningSideProperty, inverseSideProperty, fetch, cascade);
    }

    propertyConfig.setManyToManyConfig(manyToManyConfig); // TODO: try to remove
    configRegistry.registerJoinTableConfiguration(manyToManyConfig.getOwningSideClass(), manyToManyConfig.getInverseSideClass(), manyToManyConfig.getJoinTableConfig());

    readOrderByAnnotation(property, propertyConfig, targetEntityClass);
  }

  protected JoinTableConfig readJoinTableAnnotation(Property owningSideProperty, PropertyConfig owningSidePropertyConfig, Class targetEntityClass, Property inverseSideProperty) throws SQLException {
    String owningSideEntityName = owningSidePropertyConfig.getEntityConfig().getTableName();
    String inverseSideEntityName = JpaEntityConfigurationReader.getEntityTableName(targetEntityClass, annotationElementsReader);
    String joinTableName = owningSideEntityName + "_" + inverseSideEntityName; // TODO: check if table name is unique
    String owningSideJoinColumnNameStub = owningSidePropertyConfig.getEntityConfig().getTableName() + "_"; // if applied id column name has to be appended by calling (expensive) getIdColumnName(owningSidePropertyConfig.getEntityConfig())
    String inverseSideJoinColumnNameStub = owningSidePropertyConfig.getColumnName() + "_"; // if applied id column name has to be appended by calling (expensive) getIdColumnName(targetEntityClass)

    JoinTableConfig joinTableConfig = null;

    if(isAnnotationPresent(owningSideProperty, JoinTable.class) == false) {
      owningSidePropertyConfig.setColumnName(owningSideJoinColumnNameStub + getIdColumnName(owningSidePropertyConfig.getEntityConfig()));
      inverseSideJoinColumnNameStub +=  getIdColumnName(targetEntityClass);
      joinTableConfig = new JoinTableConfig(joinTableName, owningSidePropertyConfig, targetEntityClass, inverseSideJoinColumnNameStub, inverseSideProperty);
      owningSidePropertyConfig.setJoinTable(joinTableConfig);
    }
    else {
      joinTableConfig = createJoinTableConfigFromJoinTableAnnotation(owningSideProperty, owningSidePropertyConfig, targetEntityClass, inverseSideProperty, joinTableName,
          inverseSideJoinColumnNameStub);
    }

    configRegistry.registerJoinTableConfiguration(owningSideProperty.getDeclaringClass(), targetEntityClass, joinTableConfig);

    return joinTableConfig;
  }

  protected JoinTableConfig createJoinTableConfigFromJoinTableAnnotation(Property owningSideProperty, PropertyConfig owningSidePropertyConfig, Class targetEntityClass, Property inverseSideProperty, String joinTableName, String inverseSideJoinColumnNameStub) throws SQLException {
    JoinTable joinTableAnnotation = getPropertyAnnotation(owningSideProperty, JoinTable.class);
    Map<String, Object> elements = annotationElementsReader.getElements(joinTableAnnotation);

    String name = (String)elements.get("name");
    if(StringHelper.isNotNullOrEmpty(name))
      joinTableName = name;

    JoinColumn[] joinColumns = (JoinColumn[])elements.get("joinColumns");
    if(joinColumns.length > 1)
      throw new SQLException("Sorry for the inconvenience, but @JoinTable with more than one @JoinColumn value as on property " + owningSideProperty + " is not supported");
    else if(joinColumns.length == 1)
      readJoinColumnConfiguration(owningSideProperty, owningSidePropertyConfig, joinColumns[0]);
    else
      owningSidePropertyConfig.setColumnName(getJoinColumnName(owningSideProperty, owningSidePropertyConfig.getTargetEntityClass()));

    JoinColumn[] inverseJoinColumns = (JoinColumn[])elements.get("inverseJoinColumns");
    if(inverseJoinColumns.length > 1)
      throw new SQLException("Sorry for the inconvenience, but @JoinTable with more than one @InverseJoinColumn value as on property " + owningSideProperty + " is not supported");
    // TODO:
    else if(inverseJoinColumns.length == 1)
      inverseSideJoinColumnNameStub += inverseJoinColumns[0].name(); // TODO: remove name() method invocation on Annotation as well
    else
      inverseSideJoinColumnNameStub += getIdColumnName(targetEntityClass);

    JoinTableConfig joinTable = new JoinTableConfig(joinTableName, owningSidePropertyConfig, targetEntityClass, inverseSideJoinColumnNameStub, inverseSideProperty);
    owningSidePropertyConfig.setJoinTable(joinTable);

    if(inverseJoinColumns.length == 1)
      readJoinColumnConfiguration(inverseSideProperty, joinTable.getInverseSideJoinColumn(), inverseJoinColumns[0]);

    // TODO: read other JoinTable settings

    return joinTable;
  }

  protected String getIdColumnName(EntityConfig entity) {
    if(entity.getIdProperty() != null)
      return entity.getIdProperty().getColumnName();

    return "id"; // TODO: search for Id column
  }

  protected String getIdColumnName(Class entityClass) {
    return "id"; // TODO: check if Registry contains config for entity class. If so call getIdColumn(EntityConfig). If not search for Id column
  }

  protected void readOrderByAnnotation(Property property, PropertyConfig propertyConfig, Class targetEntityClass) throws SQLException {
    if(isAnnotationPresent(property, OrderBy.class))
      propertyConfig.setOrderColumns(extractOrderColumns(getPropertyAnnotation(property, OrderBy.class), targetEntityClass));
  }

  protected List<OrderByConfig> extractOrderColumns(OrderBy orderByAnnotation, Class targetEntityClass) throws SQLException {
    List<OrderByConfig> orderByConfig = new ArrayList<>();
    Map<String, Object> elements = annotationElementsReader.getElements(orderByAnnotation);
    String value = (String)elements.get("value");

    for(String orderByString : value.split(",")) {
      orderByString = orderByString.trim();
      orderByConfig.add(extractOrderColumn(orderByString, targetEntityClass));
    }

    return orderByConfig;
  }

  protected OrderByConfig extractOrderColumn(String orderByString, Class targetEntityClass) throws SQLException {
    boolean ascending = orderByString.endsWith("DESC") == false;
    String orderColumnFieldName = null;

    if(orderByString.contains("ASC"))
      orderColumnFieldName = orderByString.replace("ASC", "").trim();
    else if(orderByString.contains("DESC"))
      orderColumnFieldName = orderByString.replace("DESC", "").trim();
    else if(orderByString.length() > 0)
      orderColumnFieldName = orderByString.trim();

    if(orderColumnFieldName != null) {
      Property orderByProperty = ReflectionHelper.findPropertyByName(targetEntityClass, orderColumnFieldName);
      if(orderByProperty != null) {
        if(configRegistry.hasPropertyConfiguration(targetEntityClass, orderByProperty)) {
          String columnName = configRegistry.getPropertyConfiguration(targetEntityClass, orderByProperty).getColumnName(); // TODO: this is not completely correct as Database may affords
          // Upper Case column names
          return new OrderByConfig(columnName, ascending);
        }
        else // column not yet configured (that means its Entity configuration hasn't been read yet)
          return new OrderByConfig(targetEntityClass, orderByProperty, ascending, configRegistry); // -> save Property for later column name retrieval // TODO: dito
      }
    }

    Property idProperty = findIdProperty(targetEntityClass); // if column name for OrderBy is not set, entities get per default sorted by their Ids
    if(idProperty != null) { // actually this should never be the case for an entity
      if(configRegistry.hasPropertyConfiguration(targetEntityClass, idProperty)) {
        String columnName = configRegistry.getPropertyConfiguration(targetEntityClass, idProperty).getColumnName();
        return new OrderByConfig(columnName, ascending);
      }
      else
        return new OrderByConfig(targetEntityClass, idProperty, ascending, configRegistry);
    }

    throw new SQLException("Could not find column for OrderBy string '" + orderByString + "' mapped to class " + targetEntityClass);
  }

  public static String getColumnNameForField(Field field, ConfigRegistry configRegistry) throws SQLException {
    if(configRegistry.hasPropertyForField(field))
      return configRegistry.getPropertyConfiguration(field).getColumnName();

    String columnName = null;

    throw new SQLException("TODO: no way found yet to get OrderBy column for a not yet configured field");
//    if(isAnnotationPresent(property, Column.class)) {
//      Column columnAnnotation = (Column)getPropertyAnnotation()
//    }
//
//    Column column = field.getAnnotation(Column.class);
//
//    columnName = field.getName();

    // if not already registered, it's almost impossible to get database type for otherside's entity
    // -> create a Defered loading OrderBy class which gets field's column name on access, not now (ergo on configuration)?
//    if(entityConfig.getDatabaseType().isEntityNamesMustBeUpCase())
//      columnName = columnName.toUpperCase();

//     return columnName;
  }

  protected Property findIdProperty(Class clazz) {
    for(Property property : ReflectionHelper.getEntityPersistableProperties(clazz)) {
      if(isAnnotationPresent(property, Id.class))
        return property;
    }

    return null;
  }

  protected boolean isAnnotationPresent(Property property, Class<? extends Annotation> annotationClass) {
    if(property.hasAnnotationExistenceAlreadyBeenDecided(annotationClass))
      return property.isAnnotatedWithAnnotation(annotationClass);

    if(property.getField() != null && property.getField().isAnnotationPresent(annotationClass)) {
      property.annotationFound(annotationClass, Property.AnnotationPlacement.Field);
      return true;
    }
    else if(property.getGetMethod() != null && property.getGetMethod().isAnnotationPresent(annotationClass)) {
      property.annotationFound(annotationClass, Property.AnnotationPlacement.GetMethod);
      return true;
    }
    else {
      property.annotationNotAvailable(annotationClass);
      return false;
    }
  }

  protected <T extends Annotation> T getPropertyAnnotation(Property property, Class<T> annotationClass) {
    if(property.hasAnnotatedInstanceBeenRetrieved(annotationClass))
      return (T)property.getAnnotatedInstance(annotationClass);

    if(property.getField() != null && property.getField().isAnnotationPresent(annotationClass)) {
      T annotationInstance = property.getField().getAnnotation(annotationClass);
      property.annotatedInstanceExtracted(annotationClass, annotationInstance);
      return annotationInstance;
    }
     else if(property.getGetMethod() != null && property.getGetMethod().isAnnotationPresent(annotationClass)) {
      T annotationInstance = property.getGetMethod().getAnnotation(annotationClass);
      property.annotatedInstanceExtracted(annotationClass, annotationInstance);
      return annotationInstance;
    }

    return null;
  }


  protected boolean isCollectionClass(Class clazz) {
    return Collection.class.isAssignableFrom(clazz);
  }


  protected void throwAnnotationNotSupportedException(String annotationName, Property property) throws SQLException {
    throw new SQLException("Annotation @" + annotationName + " (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
  }

  protected void throwAttributeNotSupportedException(String attributeName, String annotationName, Property property) throws SQLException {
    throw new SQLException("Attribute " + attributeName + " of Annotation @" + annotationName + " (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
  }

  protected void throwEntityIsNotConfiguredToBeReadException(Class entityClass, Property property) throws SQLException {
    throw new SQLException("Target Class " + entityClass + " on Property " + property + " is not an Entity which Configuration should be read.\r\n" +
        "Please add it as parameter to readConfiguration() method of JpaEntityConfigurationReader.");
  }


  public void setAnnotationElementsReader(IAnnotationElementsReader annotationElementsReader) {
    this.annotationElementsReader = annotationElementsReader;
  }

  public void setConfigRegistry(ConfigRegistry configRegistry) {
    this.configRegistry = configRegistry;
  }

}
