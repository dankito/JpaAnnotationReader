package net.dankito.jpa.annotationreader.inheritance;

import net.dankito.jpa.annotationreader.EntityConfig;
import net.dankito.jpa.annotationreader.PropertyConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public class SingleTableEntityConfig<T, ID> extends InheritanceEntityConfig<T, ID> {

  protected List<PropertyConfig> allTableFieldsList = null;
  protected PropertyConfig[] allTableFields = null;


  public SingleTableEntityConfig(Class entityClass, String tableName, List<EntityConfig> subEntities) throws SQLException {
    super(entityClass, tableName, subEntities, InheritanceType.SINGLE_TABLE);
    allTableFieldsList = new ArrayList<>(Arrays.asList(getPropertyConfigs()));
  }


  public List<PropertyConfig> getAllTableFieldsList() {
    return allTableFieldsList;
  }

  public PropertyConfig[] getAllTableFields() {
    if(allTableFields == null)
      allTableFields = allTableFieldsList.toArray(new PropertyConfig[allTableFieldsList.size()]);
    return allTableFields;
  }
}
