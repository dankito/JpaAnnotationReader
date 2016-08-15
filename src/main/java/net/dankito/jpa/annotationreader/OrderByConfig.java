package net.dankito.jpa.annotationreader;

public class OrderByConfig {

  protected final static String OrderByColumnNotYetLoaded = "OrderBy Column not yet loaded";


  protected String columnName;
  protected boolean ascending;

  protected Class targetEntityClass = null;
  protected Property orderByPropertyForNotYetLoadedOrderByColumn = null;


	public OrderByConfig(String columnName, boolean ascending) {
		this.columnName = columnName;
		this.ascending = ascending;
	}

  public OrderByConfig(Class targetEntityClass, Property orderByProperty, boolean ascending) {
    this(OrderByColumnNotYetLoaded, ascending);
    this.targetEntityClass = targetEntityClass;
    this.orderByPropertyForNotYetLoadedOrderByColumn = orderByProperty;
  }

	public String getColumnName() {
    if(OrderByColumnNotYetLoaded.equals(columnName)) {
      if(Registry.getPropertyRegistry().hasPropertyConfiguration(targetEntityClass, orderByPropertyForNotYetLoadedOrderByColumn))
        this.columnName = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, orderByPropertyForNotYetLoadedOrderByColumn).getColumnName();
      // TODO: what to return if property is not found?
    }
		return columnName;
	}

	public boolean isAscending() {
		return ascending;
	}

}
