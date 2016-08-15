package net.dankito.jpa.annotationreader;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.jointable.JoinTableConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 15/08/16.
 */
public class JpaAnnotationReaderResult {

  protected List<EntityConfig> readEntities;

  protected List<JoinTableConfig> joinTables;


  public JpaAnnotationReaderResult(EntityConfig[] readEntities, JoinTableConfig[] joinTables) {
    this(Arrays.asList(readEntities), Arrays.asList(joinTables));
  }

  public JpaAnnotationReaderResult(List<EntityConfig> readEntities, List<JoinTableConfig> joinTables) {
    this.readEntities = readEntities;
    this.joinTables = joinTables;
  }


  public List<EntityConfig> getReadEntities() {
    return readEntities;
  }

  public List<JoinTableConfig> getJoinTables() {
    return joinTables;
  }

}
