package org.openlmis.referencedata.web;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.product.domain.Product;
import org.openlmis.product.domain.ProductCategory;
import org.openlmis.product.repository.ProductCategoryRepository;
import org.openlmis.product.repository.ProductRepository;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.repository.ProgramProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("PMD.TooManyMethods")
public class ProgramProductControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/programProducts";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String PROGRAM = "program";
  private static final String FULL_SUPPLY = "fullSupply";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");

  @Autowired
  private ProgramProductRepository programProductRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  private List<ProgramProduct> programProducts;

  private Integer currentInstanceNumber;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
    programProducts = new ArrayList<>();
    for ( int programProductNumber = 0; programProductNumber < 5; programProductNumber++ ) {
      programProducts.add(generateProgramProduct());
    }
  }

  @Test
  public void shouldFindProgramProducts() {
    ProgramProduct[] response = restAssured.given()
            .queryParam(PROGRAM, programProducts.get(0).getProgram().getId())
            .queryParam(FULL_SUPPLY, programProducts.get(0).isFullSupply())
            .queryParam(ACCESS_TOKEN, getToken())
            .when()
            .get(SEARCH_URL)
            .then()
            .statusCode(200)
            .extract().as(ProgramProduct[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for ( ProgramProduct programProduct : response ) {
      assertEquals(
              programProduct.getProgram().getId(),
              programProducts.get(0).getProgram().getId());
      assertEquals(
              programProduct.isFullSupply(),
              programProducts.get(0).isFullSupply());
    }
  }

  @Test
  public void shouldDeleteProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(programProductRepository.exists(programProduct.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);
    programProductRepository.delete(programProduct);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);
    programProductRepository.delete(programProduct);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(programProduct)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);
    programProduct.setDosesPerMonth(4);

    ProgramProduct response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .body(programProduct)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct.class);

    assertTrue(response.getDosesPerMonth().equals(4));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewProgramProductIfDoesNotExist() {

    ProgramProduct programProduct = programProducts.get(4);
    programProductRepository.delete(programProduct);
    programProduct.setDosesPerMonth(4);

    ProgramProduct response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", ID)
          .body(programProduct)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct.class);

    assertTrue(response.getDosesPerMonth().equals(4));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProgramProducts() {

    ProgramProduct[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct[].class);

    Iterable<ProgramProduct> programProducts = Arrays.asList(response);
    assertTrue(programProducts.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);

    ProgramProduct response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProgramProduct.class);

    assertTrue(programProductRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentProgramProduct() {

    ProgramProduct programProduct = programProducts.get(4);
    programProductRepository.delete(programProduct);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", programProduct.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProgramProduct generateProgramProduct() {
    Program program = generateProgram();
    ProductCategory productCategory = generateProductCategory();
    Product product = generateProduct(productCategory);
    ProgramProduct programProduct = new ProgramProduct();
    programProduct.setProduct(product);
    programProduct.setProductCategory(productCategory);
    programProduct.setProgram(program);
    programProduct.setFullSupply(true);
    programProduct.setActive(true);
    programProduct.setDosesPerMonth(3);
    programProductRepository.save(programProduct);
    return programProduct;
  }

  private Program generateProgram() {
    Program program = new Program();
    program.setCode("code" + generateInstanceNumber());
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }

  private ProductCategory generateProductCategory() {
    Integer instanceNumber = generateInstanceNumber();
    ProductCategory productCategory = new ProductCategory();
    productCategory.setCode("code" + instanceNumber);
    productCategory.setName("vaccine" + instanceNumber);
    productCategory.setDisplayOrder(1);
    productCategoryRepository.save(productCategory);
    return productCategory;
  }

  private Product generateProduct(ProductCategory productCategory) {
    Integer instanceNumber = generateInstanceNumber();
    Product product = new Product();
    product.setCode("code" + instanceNumber);
    product.setPrimaryName("product" + instanceNumber);
    product.setDispensingUnit("unit" + instanceNumber);
    product.setDosesPerDispensingUnit(10);
    product.setPackSize(1);
    product.setPackRoundingThreshold(0);
    product.setRoundToZero(false);
    product.setActive(true);
    product.setFullSupply(true);
    product.setTracer(false);
    product.setProductCategory(productCategory);
    productRepository.save(product);
    return product;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
