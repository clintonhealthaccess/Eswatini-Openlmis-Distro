package org.openlmis.referencedata.web;

import org.openlmis.hierarchyandsupervision.utils.ErrorResponse;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class GeographicLevelController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicLevelController.class);

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  /**
   * Allows creating new geographicLevels.
   * If the id is specified, it will be ignored.
   *
   * @param geographicLevel A geographicLevel bound to the request body
   * @return ResponseEntity containing the created geographicLevel
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.POST)
  public ResponseEntity<?> createGeographicLevel(@RequestBody GeographicLevel geographicLevel) {
    try {
      LOGGER.debug("Creating new geographicLevel");
      geographicLevel.setId(null);
      GeographicLevel newGeographicLevel = geographicLevelRepository.save(geographicLevel);
      LOGGER.debug("Created new geographicLevel with id: " + geographicLevel.getId());
      return new ResponseEntity<GeographicLevel>(newGeographicLevel, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while creating geographicLevel", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all geographicLevels.
   *
   * @return GeographicLevels.
   */
  @RequestMapping(value = "/geographicLevels", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllGeographicLevel() {
    Iterable<GeographicLevel> geographicLevels = geographicLevelRepository.findAll();
    return new ResponseEntity<>(geographicLevels, HttpStatus.OK);
  }

  /**
   * Allows updating geographicLevels.
   *
   * @param geographicLevel A geographicLevel bound to the request body
   * @param geographicLevelId UUID of geographicLevel which we want to update
   * @return ResponseEntity containing the updated geographicLevel
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateGeographicLevel(@RequestBody GeographicLevel geographicLevel,
                                            @PathVariable("id") UUID geographicLevelId) {

    GeographicLevel geographicLevelToUpdate =
          geographicLevelRepository.findOne(geographicLevelId);
    try {
      if (geographicLevelToUpdate == null) {
        geographicLevelToUpdate = new GeographicLevel();
        LOGGER.info("Creating new geographicLevel");
      } else {
        LOGGER.debug("Updating geographicLevel with id: " + geographicLevelId);
      }

      geographicLevelToUpdate.updateFrom(geographicLevel);
      geographicLevelToUpdate = geographicLevelRepository.save(geographicLevelToUpdate);

      LOGGER.debug("Saved geographicLevel with id: " + geographicLevelToUpdate.getId());
      return new ResponseEntity<GeographicLevel>(geographicLevelToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while saved geographicLevel with id: "
                  + geographicLevelToUpdate.getId(), ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get chosen geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to get
   * @return geographicLevel.
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getGeographicLevel(@PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicLevel, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting geographicLevel.
   *
   * @param geographicLevelId UUID of geographicLevel which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/geographicLevels/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteGeographicLevel(@PathVariable("id") UUID geographicLevelId) {
    GeographicLevel geographicLevel = geographicLevelRepository.findOne(geographicLevelId);
    if (geographicLevel == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        geographicLevelRepository.delete(geographicLevel);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
              new ErrorResponse("An error accurred while deleting geographicLevel with id: "
                    + geographicLevelId, ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<GeographicLevel>(HttpStatus.NO_CONTENT);
    }
  }
}
