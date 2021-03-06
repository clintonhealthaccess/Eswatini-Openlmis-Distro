package org.openlmis.referencedata.web;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.hierarchyandsupervision.domain.SupervisoryNode;
import org.openlmis.hierarchyandsupervision.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class SupervisoryNodeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/supervisoryNodes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");
  private static final String CODE = "OpenLMIS";

  @Autowired
  private SupervisoryNodeRepository repository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  private SupervisoryNode supervisoryNode = new SupervisoryNode();

  @Before
  public void setUp() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("facilityOperator");
    facilityOperatorRepository.save(facilityOperator);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode("facilityTypeCode");
    facilityTypeRepository.save(facilityType);

    GeographicLevel geoLevel = new GeographicLevel();
    geoLevel.setCode("geoCode");
    geoLevel.setLevelNumber(1);
    geographicLevelRepository.save(geoLevel);

    GeographicZone geoZone = new GeographicZone();
    geoZone.setCode("geoZoneCode");
    geoZone.setLevel(geoLevel);
    geographicZoneRepository.save(geoZone);

    Facility facility = new Facility();
    facility.setCode("facilityCode");
    facility.setActive(true);
    facility.setEnabled(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);
    facilityRepository.save(facility);

    supervisoryNode.setCode("supervisoryNodeCode");
    supervisoryNode.setFacility(facility);
    repository.save(supervisoryNode);
  }

  @Test
  public void shouldDeleteSupervisoryNode() {

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(repository.exists(supervisoryNode.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentSupervisoryNode() {

    repository.delete(supervisoryNode);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSupervisoryNode() {

    repository.delete(supervisoryNode);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(supervisoryNode)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupervisoryNode() {

    supervisoryNode.setCode(CODE);

    SupervisoryNode response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .body(supervisoryNode)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode.class);

    assertEquals(response.getCode(), CODE);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewSupervisoryNodeIfDoesNotExist() {

    repository.delete(supervisoryNode);
    supervisoryNode.setCode(CODE);

    SupervisoryNode response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", ID)
          .body(supervisoryNode)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode.class);

    assertEquals(response.getCode(), CODE);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupervisoryNodes() {

    SupervisoryNode[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode[].class);

    Iterable<SupervisoryNode> supervisoryNodes = Arrays.asList(response);
    assertTrue(supervisoryNodes.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenSupervisoryNode() {

    SupervisoryNode response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode.class);

    assertTrue(repository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentSupervisoryNode() {

    repository.delete(supervisoryNode);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
