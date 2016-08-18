package net.dankito.jpa.annotationreader.config.inheritance;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;

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
    allTableFieldsList = new ArrayList<>(Arrays.asList(getProperties()));
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
