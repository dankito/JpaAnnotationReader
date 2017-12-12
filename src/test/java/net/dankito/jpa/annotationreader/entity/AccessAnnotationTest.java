package net.dankito.jpa.annotationreader.entity;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;


public class AccessAnnotationTest extends JpaConfigurationReaderTestBase {


  @Entity
  static class EntityWithoutAccessAnnotationButIdSetOnField {
    @Id protected Long id;
  }

  @Test
  public void accessAnnotationNotSetButIdAnnotationIsOnField_AccessIsSetToField() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutAccessAnnotationButIdSetOnField.class });

    Assert.assertEquals(AccessType.FIELD, configs[0].getAccess());
  }


  @Entity
  static class EntityWithoutAccessAnnotationButIdSetOnProperty {
    protected Long id;
    @Id public Long getId() { return id; }
  }

  @Test
  public void accessAnnotationNotSetButIdAnnotationIsOnField_AccessIsSetToProperty() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutAccessAnnotationButIdSetOnProperty.class });

    Assert.assertEquals(AccessType.PROPERTY, configs[0].getAccess());
  }


  @Entity
  @Access(AccessType.FIELD)
  static class EntityWithAccessSetToField {
    protected Long id;
    @Id public Long getId() { return id; }
  }

  @Test
  public void entityWithAccessSetToField_AccessIsSetToField() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithAccessSetToField.class });

    Assert.assertEquals(AccessType.FIELD, configs[0].getAccess());
  }


  @Entity
  @Access(AccessType.PROPERTY)
  static class EntityWithAccessSetToProperty {
    @Id protected Long id;
  }

  @Test
  public void entityWithAccessSetToProperty_AccessIsSetToProperty() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithAccessSetToProperty.class });

    Assert.assertEquals(AccessType.PROPERTY, configs[0].getAccess());
  }

}
