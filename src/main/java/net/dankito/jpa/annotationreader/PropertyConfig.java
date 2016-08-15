package net.dankito.jpa.annotationreader;

import net.dankito.jpa.annotationreader.jointable.JoinTableConfig;
import net.dankito.jpa.annotationreader.relationconfig.ManyToManyConfig;
import net.dankito.jpa.annotationreader.relationconfig.OneToManyConfig;
import net.dankito.jpa.annotationreader.relationconfig.OneToOneConfig;
import net.dankito.jpa.annotationreader.util.ConfigRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GenerationType;

/**
 * Per field information configured from the {@link com.j256.ormlite.field.DatabaseField} annotation and the associated {@link Field} in the
 * class. Use the {@link #createFieldType} static method to instantiate the class.
 * 
 * @author graywatson
 */
public class PropertyConfig {

	/** default suffix added to fields that are id fields of foreign objects */
	public static final String FOREIGN_ID_FIELD_SUFFIX = "_id";

  private final static Logger log = LoggerFactory.getLogger(PropertyConfig.class);


  protected String tableName;

  protected Class<?> parentClass;
  protected boolean isPropertyOfParentClass;

  protected PropertyConfig foreignIdField;
  protected EntityConfig<?, ?> foreignEntityConfig;
  protected PropertyConfig foreignPropertyConfig;


  protected EntityConfig entityConfig;

  protected Field field;
  protected Method fieldGetMethod;
  protected Method fieldSetMethod;
  protected Class type;
  protected Class sqlType;

  protected DataType dataType;
  protected String fieldName;
  protected String columnName;

  protected String columnDefinition = null;
  protected int length = 255;

  protected boolean canBeNull = true;
  protected boolean unique = false;
  protected boolean insertable = true;
  protected boolean updatable = true;
  protected FetchType fetch = FetchType.EAGER;
  protected CascadeType[] cascade = new CascadeType[0];

  protected Boolean cascadePersist = null;
  protected Boolean cascadeRefresh = null;
  protected Boolean cascadeMerge = null; // TODO
  protected Boolean cascadeRemove = null;

  protected boolean isId;
  protected boolean isGeneratedId;
  protected GenerationType generatedIdType = GenerationType.AUTO;
  protected String generatedIdSequence;

  protected boolean isVersion = false;

  protected boolean isRelationshipProperty = false;
  protected boolean isOneCardinalityRelationshipProperty = false;
  protected boolean isManyCardinalityRelationshipProperty = false;
  protected boolean isOwningSide = false;
  protected boolean isInverseSide = false;
  protected boolean isJoinColumn = false;
  protected JoinTableConfig joinTable = null;
  protected boolean isBidirectional = false;
  protected boolean isForeignAutoCreate = false; // TODO: try to remove this value or implement other logic - but right now it's needed!

  protected Class targetEntityClass;
  protected EntityConfig targetEntityConfig;
  protected Property targetProperty;
  protected boolean typeIsACollection = false;
  protected PropertyConfig targetPropertyConfig = null;

  protected boolean isOneToOneField = false;
  protected OneToOneConfig oneToOneConfig;

  protected boolean isOneToManyField = false;
  protected boolean isManyToOneField = false;
  protected OneToManyConfig oneToManyConfig;

  protected boolean isManyToManyField = false;
  protected ManyToManyConfig manyToManyConfig;

  protected List<OrderByConfig> orderColumns = new ArrayList<>();

  protected ConfigRegistry configRegistry = null;


  protected PropertyConfig() { // for Reflection

  }

  public PropertyConfig(EntityConfig entityConfig, Property property, ConfigRegistry configRegistry) {
    this.entityConfig = entityConfig;
    this.field = property.getField();
    this.fieldGetMethod = property.getGetMethod();
    this.fieldSetMethod = property.getSetMethod();

    assignType(property.getType());

    this.fieldName = property.getFieldName();
    setColumnName(this.fieldName);

    this.configRegistry = configRegistry;
  }

  // for sub classes like DiscriminatorColumnConfig
  public PropertyConfig(EntityConfig entityConfig, String columnName) {
    this.entityConfig = entityConfig;

    this.fieldName = columnName;
    setColumnName(columnName);
  }


  public EntityConfig getEntityConfig() {
    return entityConfig;
  }

  public Field getField() {
		return field;
	}

  public Class getType() {
    return type;
  }

  protected void assignType(Class type) {
    this.type = type;
    this.typeIsACollection = Collection.class.isAssignableFrom(type);
  }

  public boolean isTypeIsACollection() {
    return typeIsACollection;
  }

  public Class getSqlType() {
    return sqlType;
  }

  public void setSqlType(Class sqlType) {
    this.sqlType = sqlType;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getColumnDefinition() {
    return columnDefinition;
  }

  public void setColumnDefinition(String columnDefinition) {
    this.columnDefinition = columnDefinition;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }


  public boolean canBeNull() {
    return canBeNull;
  }

  public void setCanBeNull(boolean canBeNull) {
    this.canBeNull = canBeNull;
  }


  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public void setInsertable(boolean insertable) {
    this.insertable = insertable;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }


  /*      Relation properties configuration   */

  public boolean isRelationshipProperty() {
    return isRelationshipProperty;
  }

  public void setIsRelationshipProperty(boolean isRelationProperty) {
    this.isRelationshipProperty = isRelationProperty;
//    this.isForeignAutoCreate = true; // TODO: try to remove / implement better logic
  }

  public boolean isOneCardinalityRelationshipProperty() {
    return isOneCardinalityRelationshipProperty;
  }

  public void setIsOneCardinalityRelationshipProperty(boolean isOneCardinalityRelationshipProperty) {
    this.isOneCardinalityRelationshipProperty = isOneCardinalityRelationshipProperty;
  }

  public boolean isManyCardinalityRelationshipProperty() {
    return isManyCardinalityRelationshipProperty;
  }

  public void setIsManyCardinalityRelationshipProperty(boolean isManyCardinalityRelationshipProperty) {
    this.isManyCardinalityRelationshipProperty = isManyCardinalityRelationshipProperty;
  }

  /**
   * Owning Side is that side of a relationship that has the Join Column.
   * @return
   */
  public boolean isOwningSide() {
    return isOwningSide;
  }

  public void setIsOwningSide(boolean isOwningSide) {
    this.isOwningSide = isOwningSide;
  }

  public boolean isInverseSide() {
    return isInverseSide;
  }

  public void setIsInverseSide(boolean isInverseSide) {
    this.isInverseSide = isInverseSide;
  }

  public boolean isJoinColumn() {
    return isJoinColumn;
  }

  public void setIsJoinColumn(boolean isJoinColumn) {
    this.isJoinColumn = isJoinColumn;
    this.entityConfig.addJoinColumn(this);
  }

  public JoinTableConfig getJoinTable() {
    if(joinTable == null && isInverseSide && getTargetPropertyConfig() != null)
      this.joinTable = getTargetPropertyConfig().getJoinTable();
    return joinTable;
  }

  public void setJoinTable(JoinTableConfig joinTable) {
    this.joinTable = joinTable;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public void setIsBidirectional(boolean isBidirectional) {
    this.isBidirectional = isBidirectional;
  }

  public Class getTargetEntityClass() {
    return targetEntityClass;
  }

  public void setTargetEntityClass(Class targetEntityClass) {
    this.targetEntityClass = targetEntityClass;
  }

  public EntityConfig getTargetEntityConfig() {
    if(targetEntityConfig == null && targetEntityClass != null)
      targetEntityConfig = configRegistry.getEntityConfiguration(targetEntityClass);
    return targetEntityConfig;
  }

  public void setTargetEntityConfig(EntityConfig targetEntityConfig) {
    this.targetEntityConfig = targetEntityConfig;
  }

  public Property getTargetProperty() {
    return targetProperty;
  }

  public void setTargetProperty(Property targetProperty) {
    this.targetProperty = targetProperty;
  }

  public PropertyConfig getTargetPropertyConfig() {
    // TODO: this is really bad code design, try to get rid of it (and of configRegistry as well)
    if(targetPropertyConfig == null && targetProperty != null) {
      targetPropertyConfig = configRegistry.getPropertyConfiguration(targetEntityClass, targetProperty);
    }

    return targetPropertyConfig;
  }

  public void setTargetPropertyConfig(PropertyConfig targetPropertyConfig) {
    this.targetPropertyConfig = targetPropertyConfig;
  }

  public boolean isOneToOneField() {
    return isOneToOneField;
  }

  public void setIsOneToOneField(boolean isOneToOneField) {
    this.isOneToOneField = isOneToOneField;
  }

  public OneToOneConfig getOneToOneConfig() {
    return oneToOneConfig;
  }

  public void setOneToOneConfig(OneToOneConfig oneToOneConfig) {
    this.oneToOneConfig = oneToOneConfig;
  }

  public boolean isOneToManyField() {
    return isOneToManyField;
  }

  public void setIsOneToManyField(boolean isOneToManyField) {
    this.isOneToManyField = isOneToManyField;
  }

  public boolean isManyToOneField() {
    return isManyToOneField;
  }

  public void setIsManyToOneField(boolean isManyToOneField) {
    this.isManyToOneField = isManyToOneField;
  }

  public OneToManyConfig getOneToManyConfig() {
    return oneToManyConfig;
  }

  public void setOneToManyConfig(OneToManyConfig oneToManyConfig) {
    this.oneToManyConfig = oneToManyConfig;
  }

  public boolean isManyToManyField() {
    return isManyToManyField;
  }

  public void setIsManyToManyField(boolean isManyToManyField) {
    this.isManyToManyField = isManyToManyField;
  }

  public ManyToManyConfig getManyToManyConfig() {
    return manyToManyConfig;
  }

  public void setManyToManyConfig(ManyToManyConfig manyToManyConfig) {
    this.manyToManyConfig = manyToManyConfig;
  }

  public boolean hasOrderColumns() {
    return getOrderColumns().size() > 0;
  }

  public List<OrderByConfig> getOrderColumns() {
    return orderColumns;
  }

  public FetchType getFetch() {
    return fetch;
  }

  public void setFetch(FetchType fetch) {
    this.fetch = fetch;
  }

  public CascadeType[] getCascade() {
    return cascade;
  }

  public void setCascade(CascadeType[] cascade) {
    this.cascade = cascade;
  }



  public String getTableName() {
		return tableName;
	}


	public boolean isId() {
		return isId;
	}

  public void setIsId(boolean isId) {
    this.isId = isId;
  }

	public boolean isGeneratedId() {
		return isGeneratedId;
	}

  public void setIsGeneratedId(boolean isGeneratedId) {
    this.isGeneratedId = isGeneratedId;
  }

  public GenerationType getGeneratedIdType() {
    return generatedIdType;
  }

  public void setGeneratedIdType(GenerationType generatedIdType) {
    this.generatedIdType = generatedIdType;
  }

  // TODO: still needed?
	public boolean isGeneratedIdSequence() {
		return generatedIdSequence != null;
	}

	/**
	 * Return the generated-id-sequence associated with the field or null if {@link #isGeneratedIdSequence} is false.
	 */
	public String getGeneratedIdSequence() {
		return generatedIdSequence;
	}


  public boolean isVersion() {
    return isVersion;
  }

  public void setIsVersion(boolean isVersion) {
    this.isVersion = isVersion;
  }

  public void setOrderColumns(List<OrderByConfig> orderColumns) {
    this.orderColumns = orderColumns;
  }

  public boolean isForeign() {
		return isOneToOneField || isManyToOneField;
	}

  public boolean isPropertyOfParentClass() {
    return isPropertyOfParentClass;
  }

  public void setIsPropertyOfParentClass(boolean isPropertyOfParentClass) {
    this.isPropertyOfParentClass = isPropertyOfParentClass;
  }


  public boolean isForeignCollection() {
    // TODO: is this correctly implemented with isManyCardinalityRelationshipProperty? Also see isForeign()
    return (isOneToManyField || isManyToManyField);
  }

  public boolean isForeignAutoCreate() {
    return isForeignAutoCreate;
  }


  public boolean cascadePersist() {
    if(cascadePersist == null) {
      cascadePersist = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.PERSIST.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadePersist = true;
          break;
        }
      }
    }

    return cascadePersist;
  }

  public boolean cascadeMerge() {
    if(cascadeMerge == null) {
      cascadeMerge = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.MERGE.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeMerge = true;
          break;
        }
      }
    }

    return cascadeMerge;
  }

  public boolean cascadeRefresh() {
    if(cascadeRefresh == null) {
      cascadeRefresh = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REFRESH.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRefresh = true;
          break;
        }
      }
    }

    return cascadeRefresh;
  }

  public boolean cascadeRemove() {
    if(cascadeRemove == null) {
      cascadeRemove = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REMOVE.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRemove = true;
          break;
        }
      }
    }

    return cascadeRemove;
  }

	@Override
	public String toString() {
		return "Property " + getFieldName() + " on Entity " + entityConfig;
	}
}
