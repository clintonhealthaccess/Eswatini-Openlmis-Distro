package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "facility_type_approved_products", schema = "referencedata")
@NoArgsConstructor
public class FacilityTypeApprovedProduct extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "facilityTypeId", nullable = false)
  @Getter
  @Setter
  private FacilityType facilityType;

  @ManyToOne
  @JoinColumn(name = "programProductId", nullable = false)
  @Getter
  @Setter
  private ProgramProduct programProduct;

  @Column(nullable = false)
  @Getter
  @Setter
  private Double maxMonthsOfStock;

  @Column
  @Getter
  @Setter
  private Double minMonthsOfStock;

  @Column
  @Getter
  @Setter
  private Double emergencyOrderPoint;

  /**
   * Copy values of attributes into new or updated FacilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct FacilityTypeApprovedProduct with new values.
   */
  public void updateFrom(FacilityTypeApprovedProduct facilityTypeApprovedProduct) {
    this.facilityType = facilityTypeApprovedProduct.getFacilityType();
    this.programProduct = facilityTypeApprovedProduct.getProgramProduct();
    this.maxMonthsOfStock = facilityTypeApprovedProduct.getMaxMonthsOfStock();
    this.minMonthsOfStock = facilityTypeApprovedProduct.getMinMonthsOfStock();
    this.emergencyOrderPoint = facilityTypeApprovedProduct.getEmergencyOrderPoint();
  }
}
