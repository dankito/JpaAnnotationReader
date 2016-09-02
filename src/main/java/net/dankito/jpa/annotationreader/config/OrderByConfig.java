package net.dankito.jpa.annotationreader.config;

import net.dankito.jpa.annotationreader.util.ConfigRegistry;

public class OrderByConfig {

  protected final static String OrderByColumnNotYetLoaded = "OrderBy Column not yet loaded";


  protected String columnName;
  protected boolean ascending;

  protected PropertyConfig orderByTargetProperty;

  protected Class targetEntityClass = null;
  protected Property orderByPropertyForNotYetLoadedOrderByColumn = null;

  protected ConfigRegistry configRegistry;


  protected OrderByConfig(String columnName, boolean ascending) {
    this.columnName = columnName;
    this.ascending = ascending;
  }

  public OrderByConfig(PropertyConfig orderByTargetProperty, boolean ascending) {
    this(orderByTargetProperty.getColumnName(), ascending);
    this.orderByTargetProperty = orderByTargetProperty;
  }

  public OrderByConfig(Class targetEntityClass, Property orderByProperty, boolean ascending, ConfigRegistry configRegistry) {
    this(OrderByColumnNotYetLoaded, ascending);
    this.targetEntityClass = targetEntityClass;
    this.orderByPropertyForNotYetLoadedOrderByColumn = orderByProperty;
    this.configRegistry = configRegistry;
  }


  public String getColumnName() {
    if(OrderByColumnNotYetLoaded.equals(columnName)) {
      if(getOrderByTargetProperty() != null) {
        this.columnName = getOrderByTargetProperty().getColumnName();
      }
      // TODO: what to return if property is not found?
    }

    return columnName;
  }

  public PropertyConfig getOrderByTargetProperty() {
    if(orderByTargetProperty == null) {
      PropertyConfig cachedPropertyConfig = configRegistry.getPropertyConfiguration(orderByPropertyForNotYetLoadedOrderByColumn);

      if(cachedPropertyConfig != null) {
        this.orderByTargetProperty = cachedPropertyConfig;

        this.configRegistry = null; // ConfigRegistry is then not needed anymore -> we can release it
        this.targetEntityClass = null;
        this.orderByPropertyForNotYetLoadedOrderByColumn = null;
      }
      // TODO: what to return if property is not found?
    }

    return orderByTargetProperty;
  }

  public boolean isAscending() {
    return ascending;
  }

}
