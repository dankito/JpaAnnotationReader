package net.dankito.jpa.annotationreader.property;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by ganymed on 07/03/15.
 */
public class VersionAnnotationTest extends JpaConfigurationReaderTestBase {

  @Entity
  static class EntityWithoutVersionAnnotation {
    @Id protected Long id;

    protected Long version;
  }

  @Test
  public void versionAnnotationNotSet_IsVersionIsSetToFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutVersionAnnotation.class });
    PropertyConfig versionPropertyConfig = getPropertyConfigurationForField(EntityWithoutVersionAnnotation.class, "version");

    Assert.assertFalse(versionPropertyConfig.isVersion());
  }


  @Entity
  static class EntityWithVersionAnnotation {
    @Id protected Long id;

    @Version protected Long version;
  }

  @Test
  public void versionAnnotationSet_IsVersionIsSetToTruee() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { EntityWithVersionAnnotation.class });
    PropertyConfig versionPropertyConfig = getPropertyConfigurationForField(EntityWithVersionAnnotation.class, "version");

    Assert.assertTrue(versionPropertyConfig.isVersion());
  }

}
