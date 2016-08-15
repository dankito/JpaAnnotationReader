package net.dankito.jpa.annotationreader.reflection;


import net.dankito.jpa.annotationreader.Property;
import net.dankito.jpa.annotationreader.reflection.ReflectionHelper;
import net.dankito.jpa.annotationreader.testmodel.Category;
import net.dankito.jpa.annotationreader.testmodel.ReferenceBase;
import net.dankito.jpa.annotationreader.testmodel.SeriesTitle;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ganymed on 10/03/15.
 */
public class ReflectionHelperTest {

  @Test
  public void getEntityPersistablePropertiesForCategory_CorrectPropertiesWillBeReturned() {
    List<Property> properties = ReflectionHelper.getEntityPersistableProperties(Category.class);

    Assert.assertEquals(14, properties.size());
  }

  @Test
  public void getEntityPersistablePropertiesForSeriesTitle_CorrectPropertiesWillBeReturned() {
    List<Property> properties = ReflectionHelper.getEntityPersistableProperties(SeriesTitle.class);

    Assert.assertEquals(2, properties.size()); // only its direct properties, not including that ones from parent Entity
  }

  @Test
  public void getEntityPersistablePropertiesForReferenceBase_CorrectPropertiesWillBeReturned() {
    List<Property> properties = ReflectionHelper.getEntityPersistableProperties(ReferenceBase.class);

    Assert.assertEquals(11 + 1, properties.size()); // +1 for DiscriminatorColumn
  }
}
