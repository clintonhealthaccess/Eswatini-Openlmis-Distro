package org.openlmis.hierarchyandsupervision.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Facility;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "supervisory_nodes", schema = "referencedata")
@NoArgsConstructor
public class SupervisoryNode extends BaseEntity {
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

  @ManyToOne
  @JoinColumn(nullable = false, name = "facilityid")
  @Getter
  @Setter
  private Facility facility;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "parentId")
  @ManyToOne
  @JoinColumn(name = "parentid")
  @Getter
  @Setter
  private SupervisoryNode parentNode;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "childNodesSetId")
  @OneToMany(mappedBy = "parentNode")
  @Getter
  @Setter
  private Set<SupervisoryNode> childNodes;

  /**
   * Copy values of attributes into new or updated SupervisoryNode.
   *
   * @param supervisoryNode SupervisoryNode with new values.
   */
  public void updateFrom(SupervisoryNode supervisoryNode) {
    this.code = supervisoryNode.getCode();
    this.name = supervisoryNode.getName();
    this.description = supervisoryNode.getDescription();
    this.facility = supervisoryNode.getFacility();
    this.parentNode = supervisoryNode.getParentNode();
    this.childNodes = supervisoryNode.getChildNodes();
  }
}
