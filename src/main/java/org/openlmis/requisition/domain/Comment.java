package org.openlmis.requisition.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.fulfillment.utils.LocalDateTimePersistenceConverter;
import org.openlmis.hierarchyandsupervision.domain.User;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.view.View;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", schema = "referencedata")
@NoArgsConstructor
public class Comment extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "requisitionId", nullable = false)
  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private Requisition requisition;

  @OneToOne
  @JoinColumn(name = "authorId", nullable = false)
  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private User author;

  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private String body;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @Convert(converter = LocalDateTimePersistenceConverter.class)
  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private LocalDateTime createdDate;

  @PrePersist
  private void prePersist() {
    this.createdDate = LocalDateTime.now();
  }

  /**
   * Copy values of attributes into new or updated Comment.
   *
   * @param comment Comment with new values.
   */
  public void updateFrom(Comment comment) {
    this.requisition = comment.getRequisition();
    this.author = comment.getAuthor();
    this.body = comment.getBody();
  }
}