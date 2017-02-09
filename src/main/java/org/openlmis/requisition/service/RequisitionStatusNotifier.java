package org.openlmis.requisition.service;

import static org.openlmis.utils.ConfigurationSettingKeys.REQUISITION_EMAIL_CONVERT_TO_ORDER_CONTENT;
import static org.openlmis.utils.ConfigurationSettingKeys.REQUISITION_EMAIL_CONVERT_TO_ORDER_SUBJECT;

import java.text.MessageFormat;
import java.util.Map;
import org.openlmis.requisition.domain.AuditLogEntry;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.dto.ProcessingPeriodDto;
import org.openlmis.requisition.dto.ProgramDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.openlmis.requisition.service.referencedata.UserReferenceDataService;
import org.openlmis.settings.service.ConfigurationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequisitionStatusNotifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionStatusNotifier.class);

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  /**
   * Notify requisition's creator that it was converted to order.
   *
   * @param requisition requisition that was converted
   * @return true if success, false if failed.
   */
  public Boolean notifyConvertToOrder(Requisition requisition) {
    ProgramDto program = programReferenceDataService.findOne(requisition.getProgramId());
    ProcessingPeriodDto period = periodReferenceDataService.findOne(
        requisition.getProcessingPeriodId());

    Map<String, AuditLogEntry> statusChanges = requisition.getStatusChanges();
    if (statusChanges == null) {
      LOGGER.warn("Could not find requisition audit data to notify for convert to order.");
      return false;
    }

    AuditLogEntry initiateAuditEntry = statusChanges.get(RequisitionStatus.INITIATED.toString());
    if (initiateAuditEntry == null) {
      LOGGER.warn("Could not find requisition initiator to notify for convert to order.");
      return false;
    }
    
    UserDto initiator = userReferenceDataService.findOne(initiateAuditEntry.getAuthorId());

    String subject = configurationSettingService
        .getStringValue(REQUISITION_EMAIL_CONVERT_TO_ORDER_SUBJECT);
    String content = configurationSettingService
        .getStringValue(REQUISITION_EMAIL_CONVERT_TO_ORDER_CONTENT);

    Object[] msgArgs = {initiator.getFirstName(), initiator.getLastName(),
        program.getName(), period.getName()};
    content = MessageFormat.format(content, msgArgs);

    return notificationService.notify(initiator, subject, content);
  }
}
