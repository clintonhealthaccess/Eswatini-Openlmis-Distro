package org.openlmis.referencedata.web;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");
  private static final String CODE = "OpenLMIS";

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  private GeographicZone geoZone = new GeographicZone();
  private GeographicLevel geoLevel = new GeographicLevel();

  @Before
  public void setUp() {
    geoLevel.setCode("geoLevelCode");
    geoLevel.setLevelNumber(1);
    geographicLevelRepository.save(geoLevel);
    geoZone.setCode("geoZoneCode");
    geoZone.setLevel(geoLevel);
    geographicZoneRepository.save(geoZone);
  }

  @Test
  public void shouldDeleteGeographicZone() {

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", geoZone.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(geographicZoneRepository.exists(geoZone.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentGeographicZone() {

    geographicZoneRepository.delete(geoZone);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", geoZone.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateGeographicZone() {

    geographicZoneRepository.delete(geoZone);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(geoZone)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateGeographicZone() {

    geoZone.setCode(CODE);

    GeographicZone response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", geoZone.getId())
          .body(geoZone)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(GeographicZone.class);

    assertEquals(response.getCode(), CODE);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewGeographicZoneIfDoesNotExist() {

    geographicZoneRepository.delete(geoZone);
    geoZone.setCode(CODE);

    GeographicZone response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", ID)
          .body(geoZone)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(GeographicZone.class);

    assertEquals(response.getCode(), CODE);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicZones() {

    GeographicZone[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(GeographicZone[].class);

    Iterable<GeographicZone> geographicZones = Arrays.asList(response);
    assertTrue(geographicZones.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenGeographicZone() {

    GeographicZone response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", geoZone.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(GeographicZone.class);

    assertTrue(geographicZoneRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentGeographicZone() {

    geographicZoneRepository.delete(geoZone);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", geoZone.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
