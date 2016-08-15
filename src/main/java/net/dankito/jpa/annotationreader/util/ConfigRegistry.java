package net.dankito.jpa.annotationreader.util;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.Property;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.jointable.JoinTableConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 15/08/16.
 */
public class ConfigRegistry {

  protected List<Class> entitiesToRead = new ArrayList<>();


  protected Map<Class, EntityConfig> mapClassToEntityConfig = new HashMap<>();


  protected Map<Class, Map<Class, JoinTableConfig>> mapOwningSideClassToJoinTableConfig = new HashMap<>();


  protected Map<Class, Map<Property, PropertyConfig>> mapPropertyToPropertyConfig = new HashMap<>();
//  protected Map<Property, PropertyConfig> mapPropertyToPropertyConfig = new HashMap<>();

  protected Map<Field, Property> mapFieldToProperty = new HashMap<>();
  protected Map<Method, Property> mapGetMethodToProperty = new HashMap<>();



  public ConfigRegistry() {
    this(new ArrayList<Class>());
  }

  public ConfigRegistry(List<Class> entitiesToRead) {
    this.entitiesToRead = entitiesToRead;
  }


  public boolean isAnEntityWhichConfigurationShouldBeRead(Class clazz) {
    return entitiesToRead.contains(clazz);
  }


  public boolean hasEntityConfiguration(Class entityClass) {
    return mapClassToEntityConfig.containsKey(entityClass);
  }

  public boolean registerEntityConfiguration(Class entityClass, EntityConfig entityConfiguration) {
    if(hasEntityConfiguration(entityClass) == false) {
      mapClassToEntityConfig.put(entityClass, entityConfiguration);
      return true;
    }

    return false;
  }

  public EntityConfig getEntityConfiguration(Class entityClass) {
    return mapClassToEntityConfig.get(entityClass);
  }


  public boolean hasJoinTableConfiguration(Class owningSideClass, Class inverseSideClass) {
    if(mapOwningSideClassToJoinTableConfig.containsKey(owningSideClass) &&
        mapOwningSideClassToJoinTableConfig.get(owningSideClass).containsKey(inverseSideClass)) {
      return true;
    }

    return false;
  }

  public boolean registerJoinTableConfiguration(Class owningSideClass, Class inverseSideClass, JoinTableConfig joinTableConfig) {
    if(mapOwningSideClassToJoinTableConfig.containsKey(owningSideClass) == false) {
      mapOwningSideClassToJoinTableConfig.put(owningSideClass, new HashMap<Class, JoinTableConfig>());
    }

    if(mapOwningSideClassToJoinTableConfig.get(owningSideClass).containsKey(inverseSideClass) == false) {
      mapOwningSideClassToJoinTableConfig.get(owningSideClass).put(inverseSideClass, joinTableConfig);
      return true;
    }

    return false;
  }

  public JoinTableConfig getJoinTableConfiguration(Class owningSideClass, Class inverseSideClass) {
    if(hasJoinTableConfiguration(owningSideClass, inverseSideClass)) {
      return mapOwningSideClassToJoinTableConfig.get(owningSideClass).get(inverseSideClass);
    }

    return null;
  }


  public boolean hasPropertyConfiguration(Class declaringClass, Property property) {
//    return mapPropertyToPropertyConfig.containsKey(property);
    return mapPropertyToPropertyConfig.containsKey(declaringClass) && mapPropertyToPropertyConfig.get(declaringClass).containsKey(property);
  }

  public boolean registerPropertyConfiguration(Class declaringClass, Property property, PropertyConfig propertyConfiguration) {
    if(hasPropertyConfiguration(declaringClass, property) == false) {
      if(mapPropertyToPropertyConfig.containsKey(declaringClass) == false)
        mapPropertyToPropertyConfig.put(declaringClass, new HashMap<Property, PropertyConfig>());

      mapPropertyToPropertyConfig.get(declaringClass).put(property, propertyConfiguration);

      if(property.getField() != null)
        mapFieldToProperty.put(property.getField(), property);
      if(property.getGetMethod() != null)
        mapGetMethodToProperty.put(property.getGetMethod(), property);

      return true;
    }

    return false;
  }

  public PropertyConfig getPropertyConfiguration(Class declaringClass, Property property) {
    Map<Property, PropertyConfig> classProperties = mapPropertyToPropertyConfig.get(declaringClass);
    if(classProperties != null)
      return classProperties.get(property);

    return null;
  }


  public boolean hasPropertyForField(Field field) {
    return mapFieldToProperty.containsKey(field);
  }

  public boolean hasPropertyConfiguration(Field field) {
    if(hasPropertyForField(field))
      return hasPropertyConfiguration(field.getDeclaringClass(), getPropertyForField(field));
    return false;
  }

  public Property getPropertyForField(Field field) {
    return mapFieldToProperty.get(field);
  }

  public PropertyConfig getPropertyConfiguration(Field field) {
    if(hasPropertyForField(field))
      return getPropertyConfiguration(field.getDeclaringClass(), getPropertyForField(field));
    return null;
  }


  public boolean hasPropertyForGetMethod(Method getMethod) {
    return mapGetMethodToProperty.containsKey(getMethod);
  }

  public boolean hasPropertyConfiguration(Method getMethod) {
    if(hasPropertyForGetMethod(getMethod))
      return hasPropertyConfiguration(getMethod.getDeclaringClass(), getPropertyForGetMethod(getMethod));
    return false;
  }

  public Property getPropertyForGetMethod(Method getMethod) {
    return mapGetMethodToProperty.get(getMethod);
  }

  public PropertyConfig getPropertyConfiguration(Method getMethod) {
    if(hasPropertyForGetMethod(getMethod))
      return getPropertyConfiguration(getMethod.getDeclaringClass(), getPropertyForGetMethod(getMethod));
    return null;
  }


  public void clear() { // TODO: only needed for testing purposes, try to remove again
    mapClassToEntityConfig.clear();

    mapOwningSideClassToJoinTableConfig.clear();

    mapPropertyToPropertyConfig.clear();
    mapFieldToProperty.clear();
    mapGetMethodToProperty.clear();
  }

  public void setEntitiesToRead(List<Class> entitiesToRead) {
    this.entitiesToRead = entitiesToRead;
  }
}
