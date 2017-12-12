package net.dankito.jpa.annotationreader.testmodel;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

@Entity(name = TableConfig.TagTableName)
public class Tag extends UserDataEntity implements Comparable<Tag>, Serializable {

  private static final long serialVersionUID = 1204202485407318615L;


  @Column(name = TableConfig.TagNameColumnName)
  protected String name = "";

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
  @OrderBy("entryIndex DESC")
  protected Collection<Entry> entries = new ArrayList<>();

//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.TagDeepThoughtJoinColumnName)
//  protected DeepThought deepThought;



  public Tag() {

  }

  public Tag(String name) {
    this();
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
//    boolean result = entries.add(entry);
//    callEntryAddedListeners(entry);
//    return result;

    if(entries instanceof List)
      ((List)entries).add(0, entry);
    else
      entries.add(entry);

    return true;
  }

  protected boolean removeEntry(Entry entry) {
    boolean result = entries.remove(entry);
    return result;
  }


//  public DeepThought getDeepThought() {
//    return deepThought;
//  }

  @Override
  public int compareTo(Tag other) {
    return name.toLowerCase().compareTo(other.getName().toLowerCase());
  }



  @Override
  @Transient
  public String getTextRepresentation() {
    return "Tag " + name + " (" + entries.size() + ")";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


}
