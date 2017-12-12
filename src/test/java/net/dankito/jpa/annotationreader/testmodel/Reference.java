package net.dankito.jpa.annotationreader.testmodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;


@Entity(name = TableConfig.ReferenceTableName)
@DiscriminatorValue(TableConfig.ReferenceDiscriminatorValue)
public class Reference extends ReferenceBase implements Comparable<Reference> {

  private static final long serialVersionUID = -7176298227016698447L;


//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.ReferenceCategoryJoinColumnName)
//  protected ReferenceCategory category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.ReferenceSeriesTitleJoinColumnName)
  protected SeriesTitle series;

  @Column(name = TableConfig.ReferenceSeriesTitleOrderColumnName)
  protected int seriesOrder = Integer.MAX_VALUE;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference", cascade = CascadeType.PERSIST)
  protected Set<ReferenceSubDivision> subDivisions = new HashSet<>();

  protected transient SortedSet<ReferenceSubDivision> subDivisionsSorted = null;

//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference")
//  protected Set<Entry> entries = new HashSet<>();


  @Column(name = TableConfig.ReferenceTableOfContentsColumnName)
  @Lob
  protected String tableOfContents;

//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.PersonDeepThoughtJoinColumnName)
//  protected DeepThought deepThought;



  public Reference() {

  }

  public Reference(String title) {
    super(title);
  }



  public SeriesTitle getSeries() {
    return series;
  }

  public void setSeries(SeriesTitle series) {
    Object previousValue = this.series;
    if(this.series != null)
      this.series.removeSerialPart(this);

    this.series = series;

    if(series != null) {
      series.addSerialPart(this); // causes a Stackoverflow
//      series.serialParts.add(this);
    }

//    for(Entry entry : new ArrayList<>(entries))
//      entry.setSeries(series);
  }

  public int getSeriesOrder() {
    return seriesOrder;
  }

  public void setSeriesOrder(int seriesOrder) {
    this.seriesOrder = seriesOrder;
  }

  public Collection<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  public boolean addSubDivision(ReferenceSubDivision subDivision) {
    if(subDivisions.add(subDivision)) {
      subDivision.setSubDivisionOrder(subDivisions.size() - 1);
      subDivision.setReference(this);
      subDivisionsSorted = null;

      return true;
    }

    return false;
  }

  public boolean removeSubDivision(ReferenceSubDivision subDivision) {
    int removeSubDivisionOrder = subDivision.getSubDivisionOrder();
    if(subDivisions.remove(subDivision)) {
      // TODO: remove from database
      subDivision.setReference(null);
      subDivisionsSorted = null;

//      for(Entry entry : new ArrayList<>(subDivision.getEntries()))
//        entry.setReferenceSubDivision(null);

      for(ReferenceSubDivision subDivisionEnum : getSubDivisions()) {
        if(subDivisionEnum.getSubDivisionOrder() > removeSubDivisionOrder)
          subDivisionEnum.setSubDivisionOrder(subDivisionEnum.getSubDivisionOrder() - 1);
      }
      return true;
    }

    return false;
  }

  public SortedSet<ReferenceSubDivision> getSubDivisionsSorted() {
    if(subDivisionsSorted == null)
      subDivisionsSorted = new TreeSet<>(getSubDivisions());

    return subDivisionsSorted;
  }


//  public Collection<Entry> getEntries() {
//    return entries;
//  }
//
//  protected boolean addEntry(Entry entry) {
//    if(entries.add(entry)) {
//      return true;
//    }
//
//    return false;
//  }
//
//  protected boolean removeEntry(Entry entry) {
//    if(entries.remove(entry)) {
//      return true;
//    }
//
//    return false;
//  }

//  public ReferenceCategory getCategory() {
//    return category;
//  }
//
//  public void setCategory(ReferenceCategory category) {
//    if(this.category != null)
//      this.category.removeReference(this);
//
//    Object previousValue = this.category;
//    this.category = category;
//
//    if(this.category != null)
//      this.category.addReference(this);
//
//    callPropertyChangedListeners(TableConfig.ReferenceCategoryJoinColumnName, previousValue, category);
//  }

  public String getTableOfContents() {
    return tableOfContents;
  }

  public void setTableOfContents(String tableOfContents) {
    this.tableOfContents = tableOfContents;
  }

//  public DeepThought getDeepThought() {
//    return deepThought;
//  }



  @Override
  @Transient
  public String getTextRepresentation() {
    return title;
  }

  @Override
  public String toString() {
    return "Reference " + getTextRepresentation();
  }


  @Override
  public int compareTo(Reference other) {
    if(other == null)
      return 1;

    return getTitle().compareTo(other.getTitle()); // TODO
  }
}
