package net.dankito.jpa.annotationreader;

import net.dankito.jpa.annotationreader.reflection.AnnotationElementsReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by ganymed on 05/03/15.
 */
public class JpaEntityConfigurationReader {

  public final static String NotSupportedExceptionTrailMessage = "currently not supported and there are no plans to implement support for it";

  private final static Logger log = LoggerFactory.getLogger(JpaEntityConfigurationReader.class);


  protected JpaPropertyConfigurationReader propertyConfigurationReader = null;

  protected AnnotationElementsReader annotationElementsReader = null;

  protected ConfigRegistry configRegistry = null;


  public JpaEntityConfigurationReader() {
    this(new JpaPropertyConfigurationReader());
  }

  public JpaEntityConfigurationReader(JpaPropertyConfigurationReader propertyConfigurationReader) {
    this(propertyConfigurationReader, new AnnotationElementsReader());
  }

  public JpaEntityConfigurationReader(JpaPropertyConfigurationReader propertyConfigurationReader, AnnotationElementsReader annotationElementsReader) {
    this.propertyConfigurationReader = propertyConfigurationReader;

    this.annotationElementsReader = annotationElementsReader;
  }


  public EntityConfig[] readConfiguration(Class... entityClasses) throws SQLException {
    configRegistry = new ConfigRegistry(Arrays.asList(entityClasses));

    propertyConfigurationReader.setConfigRegistry(configRegistry);
    propertyConfigurationReader.setAnnotationElementsReader(annotationElementsReader);

    List<EntityConfig> entityConfigs = new ArrayList<>();

    for(Class entityClass : entityClasses) {
      entityConfigs.add(readEntityConfiguration(entityClass));
    }

    return entityConfigs.toArray(new EntityConfig[entityConfigs.size()]);
  }

  protected EntityConfig readEntityConfiguration(Class<?> entityClass) throws SQLException {
    log.info("Reading configuration for Entity " + entityClass + " ...");
    if(configRegistry.isAnEntityWhichConfigurationShouldBeRead(entityClass) == false)
      throw new SQLException("Class " + entityClass + " is an unknown Entity. Add this Class to to Classes parameter of Method readConfiguration()");

    if(classIsEntity(entityClass) == false)
      throw new SQLException("Class " + entityClass + " is not an Entity as no @Entity annotation could be found");

    if(configRegistry.hasEntityConfiguration(entityClass))
      return configRegistry.getEntityConfiguration(entityClass);

    EntityConfig entityConfig = createEntityConfig(entityClass, new ArrayList<EntityConfig>());

    readEntityClassHierarchyConfiguration(entityConfig);

    if(entityConfig.getIdProperty() == null)
      throw new SQLException("Id not set on Entity " + entityConfig);

    return entityConfig;
  }

  protected void readEntityClassHierarchyConfiguration(EntityConfig entityConfig) throws SQLException {
    List<EntityConfig> currentInheritanceTypeSubEntities = new ArrayList<>();
    currentInheritanceTypeSubEntities.add(entityConfig);

    EntityConfig currentEntityConfig = null, previousEntityConfig = entityConfig;

    for (Class<?> classWalk = entityConfig.getEntityClass().getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if (classIsEntityOrMappedSuperclass(classWalk) == false)
        break; // top of inheritance hierarchy reached

      if(classIsEntity(classWalk)) {
        currentEntityConfig = getCachedOrCreateNewEntityConfig(classWalk, currentInheritanceTypeSubEntities);
        currentInheritanceTypeSubEntities.add(currentEntityConfig);

        if(previousEntityConfig != null)
          currentEntityConfig.addChildTableInfo(previousEntityConfig);

        previousEntityConfig = currentEntityConfig;
      }
    }
  }

  protected <T, ID> EntityConfig<T, ID> getCachedOrCreateNewEntityConfig(Class entityClass, List<EntityConfig> currentInheritanceTypeSubEntities) throws SQLException {
    if(configRegistry.hasEntityConfiguration(entityClass)) {
      EntityConfig entityConfig = configRegistry.getEntityConfiguration(entityClass);
      // TODO: re-implement
//      if(entityConfig instanceof InheritanceEntityConfig) {
//        ((InheritanceEntityConfig) entityConfig).addInheritanceLevelSubEntities(currentInheritanceTypeSubEntities);
//        currentInheritanceTypeSubEntities.clear();
//      }

      return entityConfig;
    }

    return createEntityConfig(entityClass, currentInheritanceTypeSubEntities);
  }

  protected <T, ID> EntityConfig<T, ID> createEntityConfig(Class entityClass, List<EntityConfig> currentInheritanceTypeSubEntities) throws SQLException {
    EntityConfig<T, ID> entityConfig = null;
    InheritanceType inheritanceStrategy = getInheritanceStrategyIfEntityIsInheritanceStartEntity(entityClass);

    if(inheritanceStrategy == null)
      entityConfig = new EntityConfig<T, ID>(entityClass);
//    else
//      entityConfig = createInheritanceEntityConfig(entityClass, inheritanceStrategy, currentInheritanceTypeSubEntities);

    configRegistry.registerEntityConfiguration(entityClass, entityConfig);

    readEntityAnnotations(entityClass, entityConfig);
    findLifeCycleEvents(entityClass, entityConfig);
    propertyConfigurationReader.readEntityPropertiesConfiguration(entityConfig);

    return entityConfig;
  }

//  protected InheritanceEntityConfig createInheritanceEntityConfig(Class<?> dataClass, InheritanceType inheritanceStrategy, List<EntityConfig> currentInheritanceTypeSubclasses) throws SQLException {
//
//
//    switch (inheritanceStrategy) {
//      case SINGLE_TABLE:
//        return createSingleTableTableInfoForClass(dataClass, currentInheritanceTypeSubclasses);
//      case JOINED:
//        return createJoinedTableInfoForClass(dataClass, currentInheritanceTypeSubclasses);
//      // TODO: implement TABLE_PER_CLASS (or throw at least an Exception that it's currently not supported
//    }
//
////    return getAndMayCreateTableInfoForClass(dataClass, connectionSource); // produces a Stack Overflow
//    return null;
//  }
//
//  public SingleTableEntityConfig createSingleTableTableInfoForClass(Class tableClass, List<EntityConfig> subclasses) throws SQLException {
//    SingleTableEntityConfig singleTableEntityConfig = new SingleTableEntityConfig(tableClass, subclasses);
//
//    return singleTableEntityConfig;
//  }
//
//  public JoinedEntityConfig createJoinedTableInfoForClass(Class tableClass, List<EntityConfig> subclasses) throws SQLException {
//    JoinedEntityConfig joinedEntityConfig = new JoinedEntityConfig(tableClass, subclasses);
//
//    return joinedEntityConfig;
//  }

  protected void readEntityAnnotations(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    entityConfig.setTableName(getEntityTableName(entityClass, annotationElementsReader));

    readEntityAnnotation(entityClass, entityConfig);
    readTableAnnotation(entityClass, entityConfig);
    readAccessAnnotation(entityClass, entityConfig);
  }

  protected void readEntityAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Entity.class) == false)
      throw new SQLException("@Entity annotation is not set on Entity " + entityConfig);

    Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
    Map<String, Object> elements = annotationElementsReader.getElements(entityAnnotation);

    String name = (String)elements.get("name");
    if(StringHelper.isNotNullOrEmpty(name))
      entityConfig.setEntityName(name);
  }

  protected void readTableAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Table.class)) {
      Table tableAnnotation = entityClass.getAnnotation(Table.class);
      Map<String, Object> elements = annotationElementsReader.getElements(tableAnnotation);

      String catalog = (String)elements.get("catalog");
      entityConfig.setCatalogName(catalog);
      if(StringHelper.isNotNullOrEmpty(catalog)) // TODO: remove as soon as Catalog is respected at table creation
        throw new SQLException("Catalog (as on class " + entityClass + ") is " + NotSupportedExceptionTrailMessage);

      String schema = (String)elements.get("schema");
      entityConfig.setSchemaName(schema);
      if(StringHelper.isNotNullOrEmpty(schema)) // TODO: remove as soon as Schema is respected at table creation
        throw new SQLException("Schema (as on class " + entityClass + ") is " + NotSupportedExceptionTrailMessage);

      UniqueConstraint[] uniqueConstraints = (UniqueConstraint[])elements.get("uniqueConstraints");
      entityConfig.setUniqueConstraints(uniqueConstraints);
      if(uniqueConstraints.length > 0) // TODO: remove as soon as Unique constraints are respected at table creation
        throw new SQLException("Unique Contraints (as on class " + entityClass + ") are " + NotSupportedExceptionTrailMessage);

      Index[] indexes = (Index[])elements.get("indexes");
      entityConfig.setIndexes(indexes);
      if(indexes.length > 0) // TODO: remove as soon as Indexes are respected at table creation
        throw new SQLException("Indexes (as on class " + entityClass + ") are " + NotSupportedExceptionTrailMessage);
    }
  }

  protected void readAccessAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Access.class)) {
      Access accessAnnotation = entityClass.getAnnotation(Access.class);
      Map<String, Object> elements = annotationElementsReader.getElements(accessAnnotation);

      AccessType value = (AccessType)elements.get("value");
      entityConfig.setAccess(accessAnnotation.value());
    }
  }


  protected void findLifeCycleEvents(Class dataClass, EntityConfig entityConfig) {
    for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(classIsEntityOrMappedSuperclass(dataClass)) {
        for (Method method : classWalk.getDeclaredMethods()) {
          checkMethodForLifeCycleEvents(method, entityConfig);
        }
      }
    }
  }

  protected void checkMethodForLifeCycleEvents(Method method, EntityConfig entityConfig) {
//    List<Annotation> methodAnnotations = Arrays.asList(method.getAnnotations());

    // TODO: i don't know what the specifications says but i implemented it this way that superclass life cycle events don't overwrite that ones from child classes
    // (or should both be called?)
    if(method.isAnnotationPresent(PrePersist.class))
      entityConfig.addPrePersistLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostPersist.class))
      entityConfig.addPostPersistLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostLoad.class))
      entityConfig.addPostLoadLifeCycleMethod(method);
    if(method.isAnnotationPresent(PreUpdate.class))
      entityConfig.addPreUpdateLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostUpdate.class))
      entityConfig.addPostUpdateLifeCycleMethod(method);
    if(method.isAnnotationPresent(PreRemove.class))
      entityConfig.addPreRemoveLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostRemove.class))
      entityConfig.addPostRemoveLifeCycleMethod(method);
  }

  // TODO: try to remove static modifiers

  public static String getEntityTableName(Class<?> entityClass, AnnotationElementsReader annotationElementsReader) throws SQLException {
    if(entityClass.isAnnotationPresent(Table.class)) {
      Table tableAnnotation = entityClass.getAnnotation(Table.class);
      Map<String, Object> elements = annotationElementsReader.getElements(tableAnnotation);;
      String name = (String)elements.get("name");
      if(StringHelper.isNotNullOrEmpty(name)) {
        return name;
      }
    }

    if(entityClass.isAnnotationPresent(Entity.class)) {
      Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
      Map<String, Object> elements = annotationElementsReader.getElements(entityAnnotation);
      String name = (String)elements.get("name");
      if(StringHelper.isNotNullOrEmpty(name)) {
        return name;
      }
    }

    return entityClass.getSimpleName();
  }


//  public static InheritanceHierarchy getInheritanceHierarchyForClass(Class entity) throws SQLException {
//    InheritanceHierarchy hierarchy = new InheritanceHierarchy();
//    List<Class> currentHierarchyTypeSubclasses = new ArrayList<>();
//
//    for (Class<?> classWalk = entity; classWalk != null; classWalk = classWalk.getSuperclass()) {
//      if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false)
//        break;
//
////      currentHierarchyTypeSubclasses.add(classWalk);
//
//      if(classWalk.isAnnotationPresent(Inheritance.class)) {
//        Inheritance inheritanceAnnotation = classWalk.getAnnotation(Inheritance.class);
//        Map<String, Object> elements = annotationElementsReader.getElements(inheritanceAnnotation);
//        InheritanceType inheritanceStrategy = (InheritanceType)elements.get("strategy");
//
//        EntityInheritance entityInheritance = new EntityInheritance(classWalk, inheritanceStrategy, currentHierarchyTypeSubclasses);
//        currentHierarchyTypeSubclasses = new ArrayList<>();
//
//        if(classWalk.isAnnotationPresent(DiscriminatorColumn.class)) {
//          DiscriminatorColumn discriminatorColumnAnnotation = classWalk.getAnnotation(DiscriminatorColumn.class);
//          entityInheritance.setDiscriminatorColumn(discriminatorColumnAnnotation);
//        }
//
//        hierarchy.addEntityHierarchyAtTop(entityInheritance);
//      }
//      else
//        currentHierarchyTypeSubclasses.add(classWalk);
//    }
//
//    return hierarchy;
//  }


  public static boolean classIsEntity(Class dataClass) {
    return dataClass.isAnnotationPresent(Entity.class);
  }

  public static boolean classIsMappedSuperClass(Class dataClass) {
    return dataClass.isAnnotationPresent(MappedSuperclass.class);
  }

  public static boolean classIsEntityOrMappedSuperclass(Class dataClass) {
    return classIsEntity(dataClass) || classIsMappedSuperClass(dataClass);
  }

  protected InheritanceType getInheritanceStrategyIfEntityIsInheritanceStartEntity(Class entityClass) throws SQLException {
    if(isInheritanceClass(entityClass))
      return getInheritanceStrategy(entityClass);

    for (Class<?> classWalk = entityClass.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(classIsMappedSuperClass(classWalk) == false) // only check super classes belonging directly to this Entity (MappedSuperClasses), not other Entities
        break;

      if(isInheritanceClass(classWalk))
        return getInheritanceStrategy(classWalk);
    }

    return null;
  }

  public boolean isInheritanceClass(Class dataClass) {
    return dataClass.isAnnotationPresent(Inheritance.class);
  }

  public InheritanceType getInheritanceStrategy(Class dataClass) throws SQLException {
    Inheritance inheritanceAnnotation = (Inheritance)dataClass.getAnnotation(Inheritance.class);
    Map<String, Object> elements = annotationElementsReader.getElements(inheritanceAnnotation);
    return (InheritanceType)elements.get("strategy");
  }

  public ConfigRegistry getConfigRegistry() {
    return configRegistry;
  }
}
