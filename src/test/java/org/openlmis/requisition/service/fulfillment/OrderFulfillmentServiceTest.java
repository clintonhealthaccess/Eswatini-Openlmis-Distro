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

package org.openlmis.requisition.service.fulfillment;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.requisition.dto.OrderDto;
import org.openlmis.requisition.dto.ProofOfDeliveryDto;
import org.openlmis.requisition.exception.ValidationMessageException;
import org.openlmis.requisition.service.BaseCommunicationService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.UUID;

public class OrderFulfillmentServiceTest extends BaseFulfillmentServiceTest<OrderDto> {

  @Override
  protected BaseCommunicationService<OrderDto> getService() {
    return new OrderFulfillmentService();
  }

  @Override
  protected OrderDto generateInstance() {
    OrderDto order = new OrderDto();
    order.setId(UUID.randomUUID());
    
    return order;
  }

  @Test
  public void shouldSendRequestToCreateOrder() throws Exception {
    // given
    OrderFulfillmentService service = (OrderFulfillmentService) prepareService();
    OrderDto order = generateInstance();

    // then
    service.create(order);

    // then
    verify(restTemplate)
        .postForEntity(uriCaptor.capture(), entityCaptor.capture(), eq(Object.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl();

    assertThat(uri.toString(), is(equalTo(url)));

    HttpEntity entity = entityCaptor.getValue();
    Object body = entity.getBody();

    assertThat(body, instanceOf(OrderDto.class));
    assertThat(((OrderDto) body).getId(), is(equalTo(order.getId())));
    assertAuthHeader(entity);
  }

  @Test
  public void shouldSendRequestToCreateMultipleOrders() {
    // given
    OrderFulfillmentService service = (OrderFulfillmentService) prepareService();
    OrderDto order = generateInstance();
    OrderDto order2 = generateInstance();

    // when
    service.create(asList(order, order2));

    // then
    verify(restTemplate)
        .postForEntity(uriCaptor.capture(), entityCaptor.capture(), eq(Object.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getBatchUrl();

    assertThat(uri.toString(), is(equalTo(url)));

    HttpEntity entity = entityCaptor.getValue();
    Object body = entity.getBody();

    assertThat(body, is(asList(order, order2)));
    assertAuthHeader(entity);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionWhenCreatingMultipleOrdersFails() {
    // given
    OrderFulfillmentService service = (OrderFulfillmentService) prepareService();
    OrderDto order = generateInstance();
    OrderDto order2 = generateInstance();

    when(restTemplate.postForEntity(any(URI.class), any(), eq(Object.class))).thenThrow(
        new RestClientException("test"));

    // when
    service.create(asList(order, order2));
  }

  @Test
  public void shouldGetProofOfDeliveries() throws Exception {
    // given
    ProofOfDeliveryDto pod = new ProofOfDeliveryDto();
    pod.setId(UUID.randomUUID());

    ResponseEntity<ProofOfDeliveryDto> response = mock(ResponseEntity.class);
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ProofOfDeliveryDto.class)))
        .thenReturn(response);

    when(response.getBody()).thenReturn(pod);

    // when
    OrderFulfillmentService service = (OrderFulfillmentService) prepareService();
    OrderDto order = generateInstance();
    ProofOfDeliveryDto actual = service.getProofOfDelivery(order.getId());

    // then
    verify(restTemplate).exchange(
          uriCaptor.capture(), eq(HttpMethod.GET),
          entityCaptor.capture(), eq(ProofOfDeliveryDto.class)
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + order.getId()
        + "/proofOfDeliveries";

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(actual.getId(), is(equalTo(pod.getId())));

    assertThat(entityCaptor.getValue().getBody(), is(nullValue()));
    assertAuthHeader(entityCaptor.getValue());
  }
}
