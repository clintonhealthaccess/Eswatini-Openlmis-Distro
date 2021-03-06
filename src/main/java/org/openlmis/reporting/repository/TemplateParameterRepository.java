package org.openlmis.reporting.repository;

import org.openlmis.referencedata.repository.ReferenceDataRepository;
import org.openlmis.reporting.model.TemplateParameter;

import java.util.UUID;

public interface TemplateParameterRepository extends
      ReferenceDataRepository<TemplateParameter, UUID> {
}
