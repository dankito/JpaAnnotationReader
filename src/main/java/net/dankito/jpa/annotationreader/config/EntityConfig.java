package net.dankito.jpa.annotationreader.config;

import net.dankito.jpa.annotationreader.reflection.ReflectionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Index;
import javax.persistence.InheritanceType;
import javax.persistence.UniqueConstraint;

/**
 * Information about a database table including the associated joinTableName, class, constructor, and the included fields.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class EntityConfig<T, ID> {

  private final static Logger log = LoggerFactory.getLogger(EntityConfig.class);

	private static final PropertyConfig[] NO_FOREIGN_COLLECTIONS = new PropertyConfig[0];
  private static final PropertyConfig[] NO_MANY_TO_MANY_FIELDS = new PropertyConfig[0];
  private static final PropertyConfig[] NO_JOIN_COLUMNS = new PropertyConfig[0];

	protected Class<T> entityClass;
	protected String tableName;
  protected String entityName; // only used for JPQL, so not of any use in our case

  protected Constructor<T> constructor;
  protected AccessType access = null;
	protected List<PropertyConfig> propertyConfigs = new ArrayList<>();
  protected Map<String, PropertyConfig> propertyConfigsColumnNames = new HashMap<>(); // TODO: merge with propertyConfigs
  protected List<PropertyConfig> propertyConfigsIncludingInheritedOnes = null;

	protected List<PropertyConfig> collectionProperties = new ArrayList<>();
  protected List<PropertyConfig> joinColumns = null;
  protected List<PropertyConfig> joinTableProperties = null;

	protected PropertyConfig idProperty;
  protected PropertyConfig versionProperty;

	private Map<String, PropertyConfig> fieldNameMap;

  // @Table Annotation settings
  protected String catalogName = "";
  protected String schemaName = "";
  protected UniqueConstraint[] uniqueConstraints = new UniqueConstraint[0];
  protected Index[] indexes = new Index[0];

  // inheritance
  protected EntityConfig inheritanceTopLevelEntityConfig = null;
  protected EntityConfig parentEntityConfig = null;
  protected List<EntityConfig> topDownInheritanceHierarchy = null;
  protected Set<EntityConfig> subClassEntityConfigs = new HashSet<>();
  protected InheritanceType inheritance = null;

  // Life Cycle Events
  // TODO: but it's possible that it has more than one method per event, isn't it?
  protected Method prePersistLifeCycleMethod = null;
  protected Method postPersistLifeCycleMethod = null;
  protected Method postLoadLifeCycleMethod = null;
  protected Method preUpdateLifeCycleMethod = null;
  protected Method postUpdateLifeCycleMethod = null;
  protected Method preRemoveLifeCycleMethod = null;
  protected Method postRemoveLifeCycleMethod = null;


  protected EntityConfig() { // for Reflection

  }

  // default constructor
  public EntityConfig(Class entityClass) throws SQLException {
    this.entityClass = entityClass;

    setEntityName(entityClass.getSimpleName());
    setTableName(getEntityName());

    this.constructor = ReflectionHelper.findNoArgConstructor(entityClass);
  }

  // for Inheritance tables
  public EntityConfig(Class entityClass, String tableName, PropertyConfig[] propertyConfigs) throws SQLException {
    this(entityClass);
    setTableName(tableName);

    this.setPropertyConfigs(propertyConfigs);
  }

  // for JoinTables
  protected EntityConfig(String tableName, PropertyConfig... propertyConfigs) throws SQLException {
    entityClass = null;
    setEntityName(tableName);
    setTableName(tableName);

    constructor = null;

    setPropertyConfigs(propertyConfigs);
  }

  /**
	 * Return the class associated with this object-info.
	 */
	public Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName() {
		return tableName;
	}

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public UniqueConstraint[] getUniqueConstraints() {
    return uniqueConstraints;
  }

  public void setUniqueConstraints(UniqueConstraint[] uniqueConstraints) {
    this.uniqueConstraints = uniqueConstraints;
  }

  public Index[] getIndexes() {
    return indexes;
  }

  public void setIndexes(Index[] indexes) {
    this.indexes = indexes;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  /**
	 * Return the array of field types associated with the object.
	 */
	public PropertyConfig[] getProperties() {
		return propertyConfigs.toArray(new PropertyConfig[propertyConfigs.size()]); // do not return mutable List<PropertyConfig> instance
	}
  public PropertyConfig[] getPropertiesIncludingInheritedOnes() {
    if(propertyConfigsIncludingInheritedOnes == null) {
      propertyConfigsIncludingInheritedOnes = determinePropertiesIncludingInheritedOnes();
    }

    return propertyConfigsIncludingInheritedOnes.toArray(new PropertyConfig[propertyConfigsIncludingInheritedOnes.size()]); // do not return mutable List<PropertyConfig> instance
  }

  protected List<PropertyConfig> determinePropertiesIncludingInheritedOnes() {
    List<PropertyConfig> propertiesIncludingInheritedOnes = new ArrayList<>();
    propertiesIncludingInheritedOnes.addAll(propertyConfigs);

    EntityConfig parentEntity = getParentEntityConfig();

    while(parentEntity != null) {
      propertiesIncludingInheritedOnes.addAll(parentEntity.propertyConfigs);

      parentEntity = parentEntity.getParentEntityConfig();
    }

    return propertiesIncludingInheritedOnes;
  }


  public PropertyConfig[] getRelationshipPropertiesWithCascadePersist() {
    // TODO: cache this info (and reset cache when adding item to propertyConfigs) so that we don't loose CPU time on each call to getRelationshipPropertiesWithCascadePersist()
    return getRelationshipPropertiesWithCascade(CascadeType.PERSIST, false);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadePersistIncludingInheritedOnes() {
    return getRelationshipPropertiesWithCascade(CascadeType.PERSIST, true);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeMerge() {
    return getRelationshipPropertiesWithCascade(CascadeType.MERGE, false);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeMergeIncludingInheritedOnes() {
    return getRelationshipPropertiesWithCascade(CascadeType.MERGE, true);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeRefresh() {
    return getRelationshipPropertiesWithCascade(CascadeType.REFRESH, false);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeRefreshIncludingInheritedOnes() {
    return getRelationshipPropertiesWithCascade(CascadeType.REFRESH, true);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeDetach() {
    return getRelationshipPropertiesWithCascade(CascadeType.DETACH, false);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeDetachIncludingInheritedOnes() {
    return getRelationshipPropertiesWithCascade(CascadeType.DETACH, true);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeRemove() {
    return getRelationshipPropertiesWithCascade(CascadeType.REMOVE, false);
  }

  public PropertyConfig[] getRelationshipPropertiesWithCascadeRemoveIncludingInheritedOnes() {
    return getRelationshipPropertiesWithCascade(CascadeType.REMOVE, true);
  }

  protected PropertyConfig[] getRelationshipPropertiesWithCascade(CascadeType cascadeType, boolean includeInheritedProperties) {
    List<PropertyConfig> cascadedRelationshipProperties = new ArrayList<>();
    PropertyConfig[] propertiesToTest = includeInheritedProperties ? getPropertiesIncludingInheritedOnes() : getProperties();

    for(PropertyConfig property : propertiesToTest) {
      if(property.isRelationshipProperty() && property.hasCascade(cascadeType)) {
        cascadedRelationshipProperties.add(property);
      }
    }

    return cascadedRelationshipProperties.toArray(new PropertyConfig[cascadedRelationshipProperties.size()]);
  }

  public PropertyConfig[] getPropertiesWithoutCollectionProperties() {
    List<PropertyConfig> propertiesWithoutCollectionProperties = new ArrayList<>();

    for(PropertyConfig propertyConfig : getProperties()) {
      if(propertyConfig.isCollectionProperty() == false)
        propertiesWithoutCollectionProperties.add(propertyConfig);
    }

    PropertyConfig[] temp = new PropertyConfig[propertiesWithoutCollectionProperties.size()];
    return propertiesWithoutCollectionProperties.toArray(temp);
  }

	/**
	 * Return the {@link PropertyConfig} associated with the columnName.
	 */
	public PropertyConfig getPropertyByColumnName(String columnName) {
    columnName = columnName.toUpperCase();
    if(propertyConfigsColumnNames.containsKey(columnName))
      return propertyConfigsColumnNames.get(columnName);
    else if(parentEntityConfig != null) {
      PropertyConfig propertyFromParentEntity = parentEntityConfig.getPropertyByColumnName(columnName);
      if(propertyFromParentEntity != null)
        return propertyFromParentEntity;
    }

		if (fieldNameMap == null) {
			// build our alias map if we need it
			Map<String, PropertyConfig> map = new HashMap<String, PropertyConfig>();
			for (PropertyConfig propertyConfig : propertyConfigs) {
				map.put(propertyConfig.getColumnName().toLowerCase(), propertyConfig);
			}
			fieldNameMap = map;
		}
		PropertyConfig propertyConfig = fieldNameMap.get(columnName.toLowerCase());
		// if column name is found, return it
		if (propertyConfig != null) {
			return propertyConfig;
		}
		// look to see if someone is using the field-name instead of column-name
		for (PropertyConfig propertyConfig2 : propertyConfigs) {
			if (propertyConfig2.getFieldName().equals(columnName)) {
				throw new IllegalArgumentException("You should use columnName '" + propertyConfig2.getColumnName()
						+ "' for table " + tableName + " instead of fieldName '" + propertyConfig2.getFieldName() + "'");
			}
		}
		throw new IllegalArgumentException("Unknown column name '" + columnName + "' in table " + tableName);
	}

  /**
   * <p>
   *   Not such a good design.<br>
   *   Only needed for Joined inheritance tables as by default all super class fields get added to TableInfo,
   *   but Joined tables only contain columns from their own fields.
   * </p>
   * @param propertyConfigs
   * @throws SQLException
   */
  protected void setPropertyConfigs(PropertyConfig[] propertyConfigs) throws SQLException {
    this.propertyConfigs.clear();
    this.propertyConfigsColumnNames.clear();

    for(PropertyConfig propertyConfig : propertyConfigs) {
      addProperty(propertyConfig);
    }
  }

  public boolean addProperty(PropertyConfig propertyConfig) throws SQLException {
    if(propertyConfigsColumnNames.containsKey(propertyConfig.getColumnName().toUpperCase()) == false) {
      propertyConfigsColumnNames.put(propertyConfig.getColumnName().toUpperCase(), propertyConfig);
      this.propertyConfigs.add(propertyConfig);

      if(propertyConfig.isId()) {
        setIdProperty(propertyConfig);
      }
      if(propertyConfig.isVersion()) {
        setVersionProperty(propertyConfig);
      }

      if(propertyConfig.isCollectionProperty()) {
        collectionProperties.add(propertyConfig);
      }

      return true;
    }
    else
      tryToMergeProperties(propertyConfigsColumnNames.get(propertyConfig.getColumnName().toUpperCase()), propertyConfig);

    return false;
  }

  protected void tryToMergeProperties(PropertyConfig currentProperty, PropertyConfig propertyWithSameColumnName) {
    if(currentProperty.getField() == null && propertyWithSameColumnName.getField() != null)
      currentProperty.field = propertyWithSameColumnName.getField();
    if(currentProperty.fieldGetMethod == null && propertyWithSameColumnName.fieldGetMethod != null)
      currentProperty.fieldGetMethod = propertyWithSameColumnName.fieldGetMethod;
    if(currentProperty.fieldSetMethod == null && propertyWithSameColumnName.fieldSetMethod != null)
      currentProperty.fieldSetMethod = propertyWithSameColumnName.fieldSetMethod;
  }

  /**
	 * Return the id-field associated with the object.
	 */
	public PropertyConfig getIdProperty() {
		return idProperty;
	}

  public void setIdProperty(PropertyConfig idProperty) throws SQLException {
    if(this.idProperty != null && idProperty != null && this.idProperty.equals(idProperty) == false)
      throw new SQLException("There is more than one ID property defined for Entity " + this + ". Found (at least) " + this.idProperty + " and " + idProperty);
    this.idProperty = idProperty;
  }


  public boolean isVersionPropertySet() {
    return getVersionProperty() != null;
  }

  public PropertyConfig getVersionProperty() {
    return versionProperty;
  }

  public void setVersionProperty(PropertyConfig versionProperty) {
    this.versionProperty = versionProperty;
  }


  public Constructor<T> getConstructor() {
		return constructor;
	}

  public AccessType getAccess() {
    return access;
  }

  public void setAccess(AccessType access) {
    this.access = access;
  }

	/**
	 * Return true if we can update this object via its ID.
	 */
	public boolean isUpdatable() {
		// to update we must have an id field and there must be more than just the id field
		return (idProperty != null && propertyConfigs.size() > 1);
	}

  public boolean hasCollectionProperties() {
    return collectionProperties.size() > 0;
  }

	public PropertyConfig[] getCollectionProperties() {
		return collectionProperties.toArray(new PropertyConfig[collectionProperties.size()]);
	}

  public boolean isJoinTable() {
    return false;
  }

  // TODO: joinColumns and joinTableProperties still needed?
  public boolean hasJoinColumns() {
    return joinColumns != null && joinColumns.size() > 0;
  }

  public List<PropertyConfig> getJoinColumns() {
    if(joinColumns == null)
      joinColumns = new ArrayList<>();
    return joinColumns;
  }

  public boolean addJoinColumn(PropertyConfig joinColumnProperty) {
    if(getJoinColumns().contains(joinColumnProperty) == false)
      return getJoinColumns().add(joinColumnProperty);
    return false;
  }

  public boolean hasJoinTableProperties() {
    return joinTableProperties != null && joinTableProperties.size() > 0;
  }

  public List<PropertyConfig> getJoinTableProperties() {
    if(joinTableProperties == null)
      joinTableProperties = new ArrayList<>();
    return joinTableProperties;
  }

  public boolean addJoinTableProperty(PropertyConfig joinTableProperty) {
    return getJoinTableProperties().add(joinTableProperty);
  }

  public EntityConfig getInheritanceTopLevelEntityConfig() {
    return inheritanceTopLevelEntityConfig;
  }

  public void setInheritanceTopLevelEntityConfig(EntityConfig inheritanceTopLevelEntityConfig) {
    this.inheritanceTopLevelEntityConfig = inheritanceTopLevelEntityConfig;
  }

  public boolean hasParentEntityConfig() {
    return parentEntityConfig != null;
  }

  public EntityConfig getParentEntityConfig() {
    return parentEntityConfig;
  }

  public void setParentEntityConfig(EntityConfig parentEntityConfig) {
    this.parentEntityConfig = parentEntityConfig;
    this.topDownInheritanceHierarchy = null;
  }

  public Class[] getParentEntityClasses() {
    List<EntityConfig> parentEntities = getTopDownInheritanceHierarchyList();
    Class[] parentEntityClasses = new Class[parentEntities.size() - 1]; // -1 because last entity is this entity

    for(int i = 0; i < parentEntityClasses.length; i++) {
      parentEntityClasses[i] = parentEntities.get(i).getEntityClass();
    }

    return parentEntityClasses;
  }

  public List<EntityConfig> getTopDownInheritanceHierarchyList() {
    if(topDownInheritanceHierarchy == null) {
      topDownInheritanceHierarchy = new ArrayList<>();
      for(EntityConfig parentEntityConfig = this; parentEntityConfig != null; parentEntityConfig = parentEntityConfig.getParentEntityConfig()) {
        topDownInheritanceHierarchy.add(0, parentEntityConfig);
      }
    }

    return topDownInheritanceHierarchy;
  }

  public EntityConfig[] getTopDownInheritanceHierarchy() {
    List<EntityConfig> hierarchyList = getTopDownInheritanceHierarchyList();

    return hierarchyList.toArray(new EntityConfig[hierarchyList.size()]);
  }

  public List<EntityConfig> getSubClassEntityConfigs() {
    return new ArrayList<>(subClassEntityConfigs);
  }

  public boolean addSubClassEntityConfig(EntityConfig subClassEntityConfig) throws SQLException {
    return subClassEntityConfigs.add(subClassEntityConfig);
  }

  public InheritanceType getInheritance() {
    return inheritance;
  }

  public void setInheritance(InheritanceType inheritance) {
    this.inheritance = inheritance;
  }

  /**
	 * Return true if this table information has a field with this columnName as set by
	 * DatabaseField.columnName() or the field name if not set.
	 */
	public boolean hasColumnName(String columnName) {
    return propertyConfigsColumnNames.containsKey(columnName);

//		for (PropertyConfig propertyConfig : propertyConfigs) {
//			if (propertyConfig.getColumnName().equals(columnName)) {
//				return true;
//			}
//		}
//		return false;
	}


  public Method prePersistLifeCycleMethod() {
    return prePersistLifeCycleMethod;
  }

  public void addPrePersistLifeCycleMethod(Method method) {
    if(prePersistLifeCycleMethod == null)
    prePersistLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postPersistLifeCycleMethod() {
    return postPersistLifeCycleMethod;
  }

  public void addPostPersistLifeCycleMethod(Method method) {
    if(postPersistLifeCycleMethod == null)
      postPersistLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postLoadLifeCycleMethod() {
    return postLoadLifeCycleMethod;
  }

  public void addPostLoadLifeCycleMethod(Method method) {
    if(postLoadLifeCycleMethod == null)
      postLoadLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method preUpdateLifeCycleMethod() {
    return preUpdateLifeCycleMethod;
  }

  public void addPreUpdateLifeCycleMethod(Method method) {
    if(preUpdateLifeCycleMethod == null)
      preUpdateLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postUpdateLifeCycleMethod() {
    return postUpdateLifeCycleMethod;
  }

  public void addPostUpdateLifeCycleMethod(Method method) {
    if(postUpdateLifeCycleMethod == null)
      postUpdateLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method preRemoveLifeCycleMethod() {
    return preRemoveLifeCycleMethod;
  }

  public void addPreRemoveLifeCycleMethod(Method method) {
    if(preRemoveLifeCycleMethod == null)
      preRemoveLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postRemoveLifeCycleMethod() {
    return postRemoveLifeCycleMethod;
  }

  public void addPostRemoveLifeCycleMethod(Method method) {
    if(postRemoveLifeCycleMethod == null)
      postRemoveLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public void invokePrePersistLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(prePersistLifeCycleMethod, data);
  }

  public void invokePostPersistLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postPersistLifeCycleMethod, data);
  }

  public void invokePostLoadLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postLoadLifeCycleMethod, data);
  }

  public void invokePreUpdateLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(preUpdateLifeCycleMethod, data);
  }

  public void invokePostUpdateLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postUpdateLifeCycleMethod, data);
  }

  public void invokePreRemoveLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(preRemoveLifeCycleMethod, data);
  }

  public void invokePostRemoveLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postRemoveLifeCycleMethod, data);
  }

  protected void invokeMethodIfNotNull(Method method, Object methodOwner) {
    if(method == null)
      return;

    try {
      boolean isAccessible = method.isAccessible();
      if(isAccessible == false)
        method.setAccessible(true);

      method.invoke(methodOwner);

      if(isAccessible == false)
        method.setAccessible(false);
    } catch(Exception ex) { log.error("Could not invoke method " + method.getName(), ex); }
  }


  @Override
  public String toString() {
    return entityName;
  }
}
