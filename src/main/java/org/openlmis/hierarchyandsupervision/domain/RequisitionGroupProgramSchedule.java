package org.openlmis.hierarchyandsupervision.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProcessingSchedule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * RequisitionGroupProgramSchedule represents the schedule to be mapped for
 * a given program and requisition group.
 */
@Entity
@Table(name = "requisition_group_program_schedules")
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionGroupProgramSchedule extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @OneToOne
  @JoinColumn(name = "processingScheduleId", nullable = false)
  @Getter
  @Setter
  private ProcessingSchedule processingSchedule;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean directDelivery;

  @OneToOne
  @JoinColumn(name = "dropOffFacilityId")
  @Getter
  @Setter
  private Facility dropOffFacility;

  /**
   * Copy values of attributes into new or updated RequisitionGroupProgramSchedule.
   *
   * @param requisitionGroupProgramSchedule RequisitionGroupProgramSchedule with new values.
   */
  public void updateFrom(RequisitionGroupProgramSchedule requisitionGroupProgramSchedule) {
    this.program = requisitionGroupProgramSchedule.getProgram();
    this.processingSchedule = requisitionGroupProgramSchedule.getProcessingSchedule();
    this.directDelivery = requisitionGroupProgramSchedule.isDirectDelivery();
    this.dropOffFacility = requisitionGroupProgramSchedule.getDropOffFacility();
  }
}
