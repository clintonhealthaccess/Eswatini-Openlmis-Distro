package org.openlmis.requisition.service;

import org.openlmis.product.domain.Product;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionLine;
import org.openlmis.requisition.domain.RequisitionTemplate;
import org.openlmis.requisition.domain.RequisitionTemplateColumn;
import org.openlmis.requisition.exception.RequisitionException;
import org.openlmis.requisition.repository.RequisitionLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequisitionLineService {

  @Autowired
  private RequisitionLineRepository requisitionLineRepository;

  @Autowired
  private ProcessingPeriodService periodService;

  @Autowired
  private RequisitionService requisitionService;

  @Autowired
  private RequisitionTemplateService requisitionTemplateService;

  /**
   * Saves given RequisitionLine if possible.
   *
   * @param requisition Requisition which contains given RequisitionLine.
   * @param requisitionLine Requisition Line to be saved.
   * @return Saved RequisitionLine.
   * @throws RequisitionException Exception thrown when
   *      it is not possible to save given RequisitionLine.
   */
  public RequisitionLine save(Requisition requisition,
                              RequisitionLine requisitionLine) throws RequisitionException {
    if (requisitionLine == null) {
      throw new RequisitionException("Requisition line does not exist");
    } else {
      List<RequisitionTemplate> requisitionTemplateList = requisitionTemplateService
          .searchRequisitionTemplates(requisitionLine.getRequisition().getProgram());

      RequisitionTemplateColumn requisitionTemplateColumn =
          requisitionTemplateList.get(0).getColumnsMap().get("beginningBalance");

      if (!requisitionTemplateColumn.getCanBeChangedByUser()) {
        resetBeginningBalance(requisition, requisitionLine);
      }

      requisitionLineRepository.save(requisitionLine);
      return requisitionLine;
    }
  }

  /**
   * Method returns all requisition lines with matched parameters.
   * @param requisition requisition of searched requisition lines.
   * @param product product of searched requisition lines.
   * @return list of requisition lines with matched parameters.
   */
  public List<RequisitionLine> searchRequisitionLines(Requisition requisition, Product product) {
    return requisitionLineRepository.searchRequisitionLines(requisition, product);
  }

  /**
   * Initiate all RequisitionLine fields from given Requisition to default value.
   *
   * @param requisition Requisition with RequisitionLines to be initiated.
   * @return Returns Requisition with initiated RequisitionLines.
   */
  public Requisition initiateRequisitionLineFields(Requisition requisition) {
    List<RequisitionTemplate> requisitionTemplateList
        = requisitionTemplateService.searchRequisitionTemplates(requisition.getProgram());

    if (!requisitionTemplateList.isEmpty()) {
      initiateBeginningBalance(requisition, requisitionTemplateList.get(0));
      initiateTotalQuantityReceived(requisition);
    }

    return requisition;
  }

  public void calculateRequisitionLineFields(Requisition requisition) {
    calculateStockOnHand(requisition);
  }

  private void initiateBeginningBalance(Requisition requisition,
                                        RequisitionTemplate requisitionTemplate) {
    Iterable<ProcessingPeriod> previousPeriods = periodService.searchPeriods(
        requisition.getProcessingPeriod().getProcessingSchedule(),
        requisition.getProcessingPeriod().getStartDate());

    if (requisitionTemplate.getColumnsMap().get("beginningBalance").getIsDisplayed()
        && previousPeriods != null && previousPeriods.iterator().hasNext()) {

      List<Requisition> previousRequisition;
      List<RequisitionLine> previousRequisitionLine;
      previousRequisition = requisitionService.searchRequisitions(
              requisition.getFacility(),
              requisition.getProgram(),
              null,null,
              previousPeriods.iterator().next(),
              null,
              null);
      if (previousRequisition.size() == 0) {
        return;
      }
      for (RequisitionLine requisitionLine : requisition.getRequisitionLines()) {
        previousRequisitionLine = searchRequisitionLines(
            previousRequisition.get(0), requisitionLine.getProduct());

        if (requisitionLine.getBeginningBalance() == null) {
          if (previousRequisitionLine != null
              && previousRequisitionLine.get(0).getStockInHand() != null) {
            requisitionLine.setBeginningBalance(previousRequisitionLine.get(0).getStockInHand());
          } else {
            requisitionLine.setBeginningBalance(0);
          }
        }
      }
    } else {
      for (RequisitionLine requisitionLine : requisition.getRequisitionLines()) {
        requisitionLine.setBeginningBalance(0);
      }
    }
  }

  private void resetBeginningBalance(Requisition requisition, RequisitionLine requisitionLine) {
    Iterable<ProcessingPeriod> previousPeriods = periodService.searchPeriods(
        requisitionLine.getRequisition().getProcessingPeriod().getProcessingSchedule(),
        requisitionLine.getRequisition().getProcessingPeriod().getStartDate());

    if (!previousPeriods.iterator().hasNext()) {
      requisitionLine.setBeginningBalance(0);
      return;
    }
    List<Requisition> previousRequisition =
            requisitionService.searchRequisitions(
                requisition.getFacility(),
                requisition.getProgram(),
                null,
                null,
                previousPeriods.iterator().next(),
                null,
                null);

    if (previousRequisition.size() == 0) {
      requisitionLine.setBeginningBalance(0);
      return;
    }

    List<RequisitionLine> previousRequisitionLine;
    previousRequisitionLine = searchRequisitionLines(
        previousRequisition.get(0), requisitionLine.getProduct());

    if (previousRequisitionLine == null) {
      requisitionLine.setBeginningBalance(0);
      return;
    }

    if (requisitionLine.getBeginningBalance() != previousRequisitionLine.get(0).getStockInHand()) {
      requisitionLine.setBeginningBalance(previousRequisitionLine.get(0).getStockInHand());
    }

  }

  private void initiateTotalQuantityReceived(Requisition requisition) {
    for (RequisitionLine requisitionLine : requisition.getRequisitionLines()) {
      requisitionLine.setTotalReceivedQuantity(0);
    }
  }


  private void calculateStockOnHand(Requisition requisition) {
    for (RequisitionLine requisitionLine : requisition.getRequisitionLines()) {
      requisitionLine.setStockOnHand(
          requisitionLine.getBeginningBalance()
              + requisitionLine.getTotalReceivedQuantity()
              + requisitionLine.getTotalLossesAndAdjustments()
              - requisitionLine.getTotalConsumedQuantity());
    }
  }
}
