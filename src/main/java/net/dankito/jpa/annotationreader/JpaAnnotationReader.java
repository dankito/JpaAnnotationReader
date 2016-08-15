package net.dankito.jpa.annotationreader;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.jointable.JoinTableConfig;
import net.dankito.jpa.annotationreader.reflection.AnnotationElementsReader;
import net.dankito.jpa.annotationreader.util.ConfigRegistry;

import java.sql.SQLException;

/**
 * Created by ganymed on 15/08/16.
 */
public class JpaAnnotationReader {

  public JpaAnnotationReaderResult readConfiguration(Class... entitiesToRead) throws SQLException {
    JpaEntityConfigurationReader entityConfigurationReader = new JpaEntityConfigurationReader(new JpaPropertyConfigurationReader(), new AnnotationElementsReader());
    ConfigRegistry configRegistry = new ConfigRegistry();

    EntityConfig[] readEntities = entityConfigurationReader.readConfiguration(configRegistry, entitiesToRead);
    JoinTableConfig[] joinTables = configRegistry.getJoinTableConfigurations();

    return new JpaAnnotationReaderResult(readEntities, joinTables);
  }

}
