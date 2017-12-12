package net.dankito.jpa.annotationreader.config.inheritance;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;


public class JoinedEntityConfig<T, ID> extends InheritanceEntityConfig<T, ID> {

  private final static Logger log = LoggerFactory.getLogger(JoinedEntityConfig.class);


  protected Map<String, List<PropertyConfig>> subClassesFieldTypes = new HashMap<>();


  protected JoinedEntityConfig() { // for Reflection

  }

  public JoinedEntityConfig(Class entityClass, String tableName, List<EntityConfig> subEntities) throws SQLException {
    super(entityClass, tableName, subEntities, InheritanceType.JOINED);
  }


  public List<PropertyConfig> getSubClassFieldTypes(String discriminatorValue) {
    if(subClassesFieldTypes.containsKey(discriminatorValue) == false)
      subClassesFieldTypes.put(discriminatorValue, findSubClassFieldTypes(discriminatorValue));
    return subClassesFieldTypes.get(discriminatorValue);
  }

  protected List<PropertyConfig> findSubClassFieldTypes(String discriminatorValue) {
    List<PropertyConfig> subClassPropertyConfigs = new ArrayList<>();

    EntityConfig entityConfigForDiscriminator = getEntityForDiscriminatorValue(discriminatorValue);
    if(entityConfigForDiscriminator == null)
      log.error("Could not get EntityConfig for Discriminator value " + discriminatorValue + " of JoinedEntityConfig " + this); // TODO: shouldn't this throw an Exception instead?
    else {
      for (EntityConfig subClassEntityConfig : entityConfigForDiscriminator.getTopDownInheritanceHierarchy()) {
        subClassPropertyConfigs.addAll(Arrays.asList(subClassEntityConfig.getProperties()));
      }
    }

    return subClassPropertyConfigs;
  }
}
