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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.requisition.RequisitionLineItemDataBuilder;
import org.openlmis.requisition.domain.RequisitionLineItem;

public class OrderLineItemDtoTest {

  @Test
  public void shouldCreateOrderLineItemBasedOnRequisitionLineItem() throws Exception {
    RequisitionLineItem requisitionLineItem = new RequisitionLineItemDataBuilder().build();
    OrderableDto product = new OrderableDto();

    OrderLineItemDto orderLineItem = OrderLineItemDto.newOrderLineItem(
        requisitionLineItem, product
    );

    assertThat(orderLineItem.getId(), is(nullValue()));
    assertThat(orderLineItem.getOrderable(), is(product));
    assertThat(orderLineItem.getFilledQuantity(), is(0L));
    assertThat(
        orderLineItem.getOrderedQuantity(),
        is(requisitionLineItem.getApprovedQuantity().longValue())
    );
    assertThat(orderLineItem.getPacksToShip(), is(requisitionLineItem.getPacksToShip()));
  }

}