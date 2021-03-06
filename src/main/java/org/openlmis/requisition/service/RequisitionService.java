package org.openlmis.requisition.service;

import org.openlmis.hierarchyandsupervision.domain.SupervisoryNode;
import org.openlmis.hierarchyandsupervision.domain.User;
import org.openlmis.hierarchyandsupervision.repository.UserRepository;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionLine;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.exception.RequisitionException;
import org.openlmis.requisition.repository.RequisitionLineRepository;
import org.openlmis.requisition.repository.RequisitionRepository;
import org.openlmis.settings.service.ConfigurationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RequisitionService {
  private static final String REQUISITION_NULL_MESSAGE = "requisition cannot be null";
  private static final String REQUISITION_DOES_NOT_EXISTS_MESSAGE = "Requisition does not exist: ";
  private static final String REQUISITION_BAD_STATUS_MESSAGE = "requisition has bad status";

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionService.class);

  @Autowired
  private RequisitionRepository requisitionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RequisitionLineService requisitionLineService;

  @Autowired
  private RequisitionLineRepository requisitionLineRepository;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  /**
   * Initiated given requisition if possible.
   *
   * @param requisitionDto Requisition object to initiate.
   * @return Initiated requisition.
   * @throws RequisitionException Exception thrown when
   *      it is not possible to initialize a requisition.
   */
  public Requisition initiateRequisition(Requisition requisitionDto)
                                          throws RequisitionException {

    if (requisitionDto == null) {
      throw new RequisitionException("Requisition cannot be initiated with null object");
    } else if (requisitionRepository.findOne(requisitionDto.getId()) == null) {

      requisitionDto.setStatus(RequisitionStatus.INITIATED);
      requisitionLineService.initiateRequisitionLineFields(requisitionDto);

      requisitionDto.getRequisitionLines().forEach(
          requisitionLine -> requisitionLineRepository.save(requisitionLine));
      requisitionRepository.save(requisitionDto);

    } else {
      throw new RequisitionException("Cannot initiate requisition."
          + " Requisition with such parameters already exists");
    }

    return requisitionDto;
  }

  /**
   * Submits given requisition if it exists and has status INITIATED.
   *
   * @param requisition Requisition to be submitted.
   * @return Submitted requisition.
   * @throws RequisitionException Exception thrown when it is not possible to submit a requisition.
   */
  public Requisition submitRequisition(Requisition requisition) throws RequisitionException {

    Requisition initiatedRequisition = requisitionRepository.findOne(requisition.getId());

    if (initiatedRequisition == null) {
      throw new RequisitionException(REQUISITION_DOES_NOT_EXISTS_MESSAGE + requisition.getId());
    } else if (requisition.getStatus() != RequisitionStatus.INITIATED) {
      throw new RequisitionException("Cannot submit requisition: "
          + requisition.getId() + ". Requisition must have status 'INITIATED' to be submitted.");
    } else {
      LOGGER.debug("Submitting a requisition with id " + requisition.getId());
      requisition.setStatus(RequisitionStatus.SUBMITTED);
      requisitionLineService.calculateRequisitionLineFields(requisition);
      requisitionRepository.save(requisition);
      LOGGER.debug("Requisition with id " + requisition.getId() + " submitted");
      return requisition;
    }
  }

  /**
   * Delete given Requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be deleted.
   * @return True if deletion successful, false otherwise.
   * @throws RequisitionException Exception thrown when it is not possible to delete a requisition.
   */
  public boolean tryDelete(UUID requisitionId) throws RequisitionException {
    Requisition requisition = requisitionRepository.findOne(requisitionId);

    if (requisition == null) {
      throw new RequisitionException(REQUISITION_DOES_NOT_EXISTS_MESSAGE + requisitionId);
    } else if (requisition.getStatus() != RequisitionStatus.INITIATED) {
      LOGGER.debug("Delete failed - " + REQUISITION_BAD_STATUS_MESSAGE);
    } else {
      requisitionRepository.delete(requisition);
      LOGGER.debug("Requisition deleted");
      return true;
    }
    return false;
  }

  /**
   * Skip given requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be skipped.
   * @return Skipped Requisition.
   * @throws RequisitionException Exception thrown when it is not possible to skip a requisition.
   */
  public Requisition skip(UUID requisitionId) throws RequisitionException {
    Requisition requisition = requisitionRepository.findOne(requisitionId);

    if (requisition == null) {
      throw new RequisitionException("Skip failed - "
          + REQUISITION_NULL_MESSAGE);
    } else if (requisition.getStatus() != RequisitionStatus.INITIATED) {
      throw new RequisitionException("Skip failed - "
          + REQUISITION_BAD_STATUS_MESSAGE);
    } else if (!requisition.getProgram().getPeriodsSkippable()) {
      throw new RequisitionException("Skip failed - "
              + "requisition program does not allow skipping");
    } else {
      LOGGER.info("Requisition skipped");
      requisition.setStatus(RequisitionStatus.SKIPPED);
      return requisitionRepository.save(requisition);
    }
  }

  /**
   * Reject given requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be rejected.
   * @throws RequisitionException Exception thrown when it is not possible to reject a requisition.
   */
  public Requisition reject(UUID requisitionId) throws RequisitionException {

    Requisition requisition = requisitionRepository.findOne(requisitionId);
    if (requisition == null) {
      throw new RequisitionException(REQUISITION_DOES_NOT_EXISTS_MESSAGE + requisitionId);
    } else if (requisition.getStatus() != RequisitionStatus.AUTHORIZED) {
      throw new RequisitionException("Cannot reject requisition: " + requisitionId
          + " .Requisition must be waiting for approval to be rejected");
    } else {
      LOGGER.debug("Requisition rejected: " + requisitionId);
      requisition.setStatus(RequisitionStatus.INITIATED);
      return requisitionRepository.save(requisition);
    }
  }

  /**
   * Finds requisitions matching all of provided parameters.
   */
  public List<Requisition> searchRequisitions(Facility facility, Program program,
                                              LocalDateTime createdDateFrom,
                                              LocalDateTime createdDateTo,
                                              ProcessingPeriod processingPeriod,
                                              SupervisoryNode supervisoryNode,
                                              RequisitionStatus requisitionStatus) {
    return requisitionRepository.searchRequisitions(
            facility, program, createdDateFrom,
            createdDateTo, processingPeriod, supervisoryNode, requisitionStatus);
  }

  /**
   * Get requisitions to approve for specified user.
   */
  public List<Requisition> getRequisitionsForApproval(UUID userId) {
    User user = userRepository.findOne(userId);
    List<Requisition> requisitionsForApproval = new ArrayList<>();
    if (user.getSupervisedNode() != null) {
      requisitionsForApproval.addAll(getAuthorizedRequisitions(user.getSupervisedNode()));
    }
    return requisitionsForApproval;
  }

  /**
   * Get authorized requisitions supervised by specified Node.
   */
  public List<Requisition> getAuthorizedRequisitions(SupervisoryNode supervisoryNode) {
    List<Requisition> requisitions = new ArrayList<>();
    Set<SupervisoryNode> supervisoryNodes = supervisoryNode.getChildNodes();
    if (supervisoryNodes == null) {
      supervisoryNodes = new HashSet<>();
    }
    supervisoryNodes.add(supervisoryNode);

    for (SupervisoryNode supNode : supervisoryNodes) {
      List<Requisition> reqList = searchRequisitions(null, null, null, null, null, supNode, null);
      if (reqList != null) {
        for (Requisition req : reqList) {
          if (req.getStatus() == RequisitionStatus.AUTHORIZED) {
            requisitions.add(req);
          }
        }
      }
    }

    return requisitions;
  }

  /**
   * Authorize given Requisition if possible.
   *
   * @param requisitionId UUID of Requisition to be authorized.
   * @param requisitionDto Requisition object to be authorized.
   * @param validationErrors Boolean which contains information if given object is valid.
   * @return Authorized requisition.
   * @throws RequisitionException Exception thrown when
   *      it is not possible to authorize a requisition.
   */
  public Requisition authorize(UUID requisitionId, Requisition requisitionDto,
                               boolean validationErrors) throws RequisitionException {
    if (configurationSettingService.getBoolValue("skipAuthorization")) {
      throw new RequisitionException("Requisition authorization is configured to be skipped");
    }
    Requisition requisition = requisitionRepository.findOne(requisitionId);
    if (requisition == null) {
      throw new RequisitionException(REQUISITION_DOES_NOT_EXISTS_MESSAGE + requisitionId);
    } else if (requisition.getStatus() != RequisitionStatus.SUBMITTED) {
      throw new RequisitionException("Cannot authorize requisition: " + requisitionId
        + " . Requisition must have submitted status to be authorized");
    } else if (requisitionDto == null || validationErrors) {
      throw new RequisitionException("Requisition object is not valid.");
    } else {
      requisitionDto.setStatus(RequisitionStatus.AUTHORIZED);
      requisitionLineService.calculateRequisitionLineFields(requisitionDto);
      return requisitionRepository.save(requisitionDto);
    }
  }


  /**
   * Releases the list of given requisitions as order.
   *
   * @param requisitionList list of requisitions to be released as order
   * @return list of released requisitions
   */
  public List<Requisition> releaseRequisitionsAsOrder(List<Requisition> requisitionList) {
    List<Requisition> releasedRequisitions = new ArrayList<>();
    for (Requisition requisition : requisitionList) {
      Requisition loadedRequisition = requisitionRepository.findOne(requisition.getId());
      loadedRequisition.setStatus(RequisitionStatus.RELEASED);
      releasedRequisitions.add(requisitionRepository.save(loadedRequisition));
    }
    return releasedRequisitions;
  }

  private Requisition save(Requisition requisition) throws RequisitionException {
    if (requisition != null) {
      if (requisition.getRequisitionLines() != null) {
        for (RequisitionLine requisitionLine : requisition.getRequisitionLines()) {
          requisitionLineService.save(requisition,requisitionLine);
        }
      }
      return requisitionRepository.save(requisition);
    } else {
      return null;
    }
  }

}
