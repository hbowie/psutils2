package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.strings.StringUtils;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;

  import java.util.*;

  import javafx.scene.control.*;

/**
 * A list of Works created by authors.
 */
public class WorkList
    extends ValueList {

  private Map<String, Work> works;

  /** Creates a new instance of TagsList */
  public WorkList(ComboBox<String> comboBox) {

    super(comboBox);
    works = new HashMap<String, Work>();
  }

  public WorkList() {

    super();
    works = new HashMap<String, Work>();
  }

  public void add(Note note) {
    if (note.hasWorkTitle()) {
      Work work = note.getWork();
      registerValue (work.getTitle());
      if (works.containsKey(work.getKey())) {
        Work existingWork = works.get(work.getKey());
        existingWork.mergeLatest(work);
        note.getWork().mergeLatest(existingWork);
      } else {
        works.put(work.getKey(), work);
      }
    }
  }

  public void modify(Note note) {
    if (note.hasWorkTitle()) {
      Work work = note.getWork();
      registerValue (work.getTitle());
      if (works.containsKey(work.getKey())) {
        Work existingWork = works.get(work.getKey());
        existingWork.mergeLatest(work);
        note.getWork().mergeLatest(existingWork);
      } else {
        works.put(work.getKey(), work);
      }
    }
  }

  public void remove(Note note) {
    // No need to do anything
  }

  public Work getWork(String workTitle) {
    String workKey = StringUtils.commonName(workTitle);
    return works.get(workKey);
  }
}
