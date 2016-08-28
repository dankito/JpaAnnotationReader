package net.dankito.jpa.annotationreader.config.inheritance;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.util.StringHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public abstract class InheritanceEntityConfig<T, ID> extends EntityConfig<T, ID> {

  public final static InheritanceType DefaultInheritanceType = InheritanceType.SINGLE_TABLE;
  public final static DiscriminatorType DefaultDiscriminatorColumnType = DiscriminatorType.STRING; // JPA default values
  public final static String DefaultDiscriminatorColumnName = "DTYPE";
  public final static int DefaultDiscriminatorColumnLength = 31;
  public final static String DefaultDiscriminatorColumnDefinition = null;


  protected DiscriminatorType discriminatorType = DefaultDiscriminatorColumnType;
  protected PropertyConfig discriminatorPropertyConfig = null;

  protected List<EntityConfig> subEntities = new ArrayList<>();
  protected Map<String, EntityConfig> mapDiscriminatorValuesToSubEntities = new HashMap<>();
  protected Map<EntityConfig, String> mapSubEntitiesToDiscriminatorValues = new HashMap<>();
  protected Map<Class, String> mapClassToDiscriminatorValues = new HashMap<>();


  protected InheritanceEntityConfig() { // for Reflection

  }

  public InheritanceEntityConfig(Class<T> entityClass, String tableName, List<EntityConfig> subEntities, InheritanceType inheritance) throws SQLException {
    super(entityClass);

    this.inheritance = inheritance;
    this.inheritanceTopLevelEntityConfig = this;

    setTableName(tableName);

    this.discriminatorPropertyConfig = createDiscriminatorColumn(entityClass);
    addProperty(this.discriminatorPropertyConfig);

    findAndStoreDiscriminatorValueForEntity(this);

    addInheritanceLevelSubEntities(subEntities);
  }



  @Override
  public void setIdProperty(PropertyConfig idProperty) throws SQLException {
    super.setIdProperty(idProperty);

    for(EntityConfig subClassEntityConfig : getSubClassEntityConfigs()) {
      if(subClassEntityConfig.getIdProperty() == null) {
        setIdOnSubClassEntityConfig(subClassEntityConfig, idProperty);
      }
    }
  }

  protected void setIdOnSubClassEntityConfig(EntityConfig subClassEntityConfig, PropertyConfig idProperty) throws SQLException {
    // TODO: really add also to SingleTable and TablePerClass sub classes or only to JoinedTable ones?
    if(getInheritance() == InheritanceType.JOINED) {
      subClassEntityConfig.addProperty(new InheritanceSubTableIdPropertyConfig(subClassEntityConfig, this, idProperty));
    }
    else {
      subClassEntityConfig.setIdProperty(idProperty);
    }
  }

  @Override
  public void setVersionProperty(PropertyConfig versionProperty) {
    super.setVersionProperty(versionProperty);

    for(EntityConfig subClassEntityConfig : getSubClassEntityConfigs()) {
      if(subClassEntityConfig.getVersionProperty() == null) {
        setVersionPropertyOnSubClassEntityConfig(subClassEntityConfig, versionProperty);
      }
    }
  }

  protected void setVersionPropertyOnSubClassEntityConfig(EntityConfig subClassEntityConfig, PropertyConfig versionProperty) {
    subClassEntityConfig.setVersionProperty(versionProperty);
  }

  public void addInheritanceLevelSubEntities(List<EntityConfig> subEntitiesToAdd) throws SQLException {
    for(EntityConfig subClassEntityConfig : subEntitiesToAdd) {
      this.addSubClassEntityConfig(subClassEntityConfig);
    }
  }

  @Override
  public boolean addSubClassEntityConfig(EntityConfig subClassEntityConfig) throws SQLException {
    if(super.addSubClassEntityConfig(subClassEntityConfig)) {
      subClassEntityConfig.setInheritance(this.inheritance);
      subClassEntityConfig.setInheritanceTopLevelEntityConfig(this);

      if(getIdProperty() != null) {
        setIdOnSubClassEntityConfig(subClassEntityConfig, getIdProperty());
      }

      if(getVersionProperty() != null) {
        setVersionPropertyOnSubClassEntityConfig(subClassEntityConfig, getVersionProperty());
      }

      findAndStoreDiscriminatorValueForEntity(subClassEntityConfig);

      return true;
    }

    return false;
  }

  protected void findAndStoreDiscriminatorValueForEntity(EntityConfig entity) throws SQLException {
    String discriminatorValue = determineEntityDiscriminatorValue(entity);

    mapDiscriminatorValuesToSubEntities.put(discriminatorValue, entity);
    mapSubEntitiesToDiscriminatorValues.put(entity, discriminatorValue);
    mapClassToDiscriminatorValues.put(entity.getEntityClass(), discriminatorValue);
  }

  protected String determineEntityDiscriminatorValue(EntityConfig entity) throws SQLException {
    Class<?> entityClass = entity.getEntityClass();
    if(entityClass.isAnnotationPresent(DiscriminatorValue.class)) {
      DiscriminatorValue discriminatorValueAnnotation = entityClass.getAnnotation(DiscriminatorValue.class);
      if(discriminatorValueAnnotation.value().length() > this.discriminatorPropertyConfig.getLength())
        throw new SQLException("DiscriminatorValue " + discriminatorValueAnnotation.value() + " for Entity " + entity + " is too long for configured (default) column length of " +
                               discriminatorPropertyConfig.getLength() + " (default length = " + DefaultDiscriminatorColumnLength + ")");
      return discriminatorValueAnnotation.value();
    }

    if(discriminatorType == DiscriminatorType.INTEGER)
      return Integer.toString(mapDiscriminatorValuesToSubEntities.size() + 1);
    else if(discriminatorType == DiscriminatorType.CHAR)
      return new String(new char[] { (char)(65 + mapDiscriminatorValuesToSubEntities.size())});

    String entityName = entity.getTableName(); // default value for STRING is Entity's name
    if(entityName.length() > discriminatorPropertyConfig.getLength())
      entityName = entityName.substring(0, discriminatorPropertyConfig.getLength()); // TODO: check if this name is unique
    return entityName;
  }

  public EntityConfig getEntityForDiscriminatorValue(String discriminatorValue) {
    return mapDiscriminatorValuesToSubEntities.get(discriminatorValue);
  }

  public String getDiscriminatorValueForEntity(EntityConfig entity) {
    return mapSubEntitiesToDiscriminatorValues.get(entity);
  }

  public String getDiscriminatorValueForEntityClass(Class entityClass) {
    return mapClassToDiscriminatorValues.get(entityClass);
  }

  protected PropertyConfig createDiscriminatorColumn(Class<?> tableClass) throws SQLException {
    if(tableClass.isAnnotationPresent(DiscriminatorColumn.class) == false) // if DiscriminatorColumn is not specified explicitly, return PropertyConfig with default values
      return new DiscriminatorColumnConfig(this, DefaultDiscriminatorColumnName, DefaultDiscriminatorColumnType, DefaultDiscriminatorColumnLength, DefaultDiscriminatorColumnDefinition);
    else {
      DiscriminatorColumn discriminatorColumnAnnotation = tableClass.getAnnotation(DiscriminatorColumn.class);
      this.discriminatorType = discriminatorColumnAnnotation.discriminatorType();
      return new DiscriminatorColumnConfig(this, discriminatorColumnAnnotation.name(), discriminatorColumnAnnotation.discriminatorType(), discriminatorColumnAnnotation.length(),
          StringHelper.isNotNullOrEmpty(discriminatorColumnAnnotation.columnDefinition()) ? discriminatorColumnAnnotation.columnDefinition() : DefaultDiscriminatorColumnDefinition);
    }
  }


  public DiscriminatorType getDiscriminatorType() {
    return discriminatorType;
  }

  public PropertyConfig getDiscriminatorPropertyConfig() {
    return discriminatorPropertyConfig;
  }

  public List<EntityConfig> getSubEntities() {
    return subEntities;
  }
}
