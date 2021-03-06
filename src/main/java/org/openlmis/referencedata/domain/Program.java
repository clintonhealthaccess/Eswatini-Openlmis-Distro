package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "programs", schema = "referencedata")
@NoArgsConstructor
public class Program extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private Boolean active;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean periodsSkippable;

  @Getter
  @Setter
  private Boolean showNonFullSupplyTab;

  @PrePersist
  private void prePersist() {
    if (this.periodsSkippable == null) {
      this.periodsSkippable = false;
    }
  }

  /**
   * Copy values of attributes into new or updated Program.
   *
   * @param program ProgramProduct with new values.
   */
  public void updateFrom(Program program) {
    this.code = program.getCode();
    this.name = program.getName();
    this.description = program.getDescription();
    this.active = program.getActive();
    this.periodsSkippable = program.getPeriodsSkippable();
    this.showNonFullSupplyTab = program.getShowNonFullSupplyTab();
  }
}
