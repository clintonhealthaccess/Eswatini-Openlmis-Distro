/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.requisition.dto;

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.requisition.testutils.FacilityDtoDataBuilder;
import org.openlmis.requisition.testutils.ProgramDtoDataBuilder;

public class SupplyLineDtoTest {

  @Test
  public void equalsContract() {
    SupervisoryNodeDto supervisoryNodeDto1 = new SupervisoryNodeDto();
    supervisoryNodeDto1.setId(UUID.randomUUID());

    SupervisoryNodeDto supervisoryNodeDto2 = new SupervisoryNodeDto();
    supervisoryNodeDto2.setId(UUID.randomUUID());

    EqualsVerifier
        .forClass(SupplyLineDto.class)
        .withRedefinedSuperclass()
        .withPrefabValues(SupervisoryNodeDto.class, supervisoryNodeDto1, supervisoryNodeDto2)
        .withPrefabValues(ProgramDto.class,
            new ProgramDtoDataBuilder().build(), new ProgramDtoDataBuilder().build())
        .withPrefabValues(FacilityDto.class,
            new FacilityDtoDataBuilder().build(), new FacilityDtoDataBuilder().build())
        .suppress(Warning.NONFINAL_FIELDS) // fields in DTO cannot be final
        .verify();
  }

}
