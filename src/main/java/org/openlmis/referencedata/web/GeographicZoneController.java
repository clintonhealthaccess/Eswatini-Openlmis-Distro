package org.openlmis.referencedata.web;

import org.openlmis.hierarchyandsupervision.utils.ErrorResponse;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
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
public class GeographicZoneController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicZoneController.class);

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  /**
   * Allows creating new geographicZones.
   * If the id is specified, it will be ignored.
   *
   * @param geographicZone A geographicZone bound to the request body
   * @return ResponseEntity containing the created geographicZone
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.POST)
  public ResponseEntity<?> createGeographicZone(@RequestBody GeographicZone geographicZone) {
    try {
      LOGGER.debug("Creating new geographicZone");
      geographicZone.setId(null);
      GeographicZone newGeographicZone = geographicZoneRepository.save(geographicZone);
      LOGGER.debug("Created new geographicZone with id: " + geographicZone.getId());
      return new ResponseEntity<GeographicZone>(newGeographicZone, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while creating geographicZone", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all geographicZones.
   *
   * @return GeographicZones.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllGeographicZones() {
    Iterable<GeographicZone> geographicZones = geographicZoneRepository.findAll();
    return new ResponseEntity<>(geographicZones, HttpStatus.OK);
  }

  /**
   * Allows updating geographicZones.
   *
   * @param geographicZone A geographicZone bound to the request body
   * @param geographicZoneId UUID of geographicZone which we want to update
   * @return ResponseEntity containing the updated geographicZone
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateGeographicZone(@RequestBody GeographicZone geographicZone,
                                                 @PathVariable("id") UUID geographicZoneId) {

    GeographicZone geographicZoneToUpdate = geographicZoneRepository.findOne(geographicZoneId);
    try {
      if (geographicZoneToUpdate == null) {
        geographicZoneToUpdate = new GeographicZone();
        LOGGER.info("Creating new geographicZone");
      } else {
        LOGGER.debug("Updating geographicZone with id: " + geographicZoneId);
      }

      geographicZoneToUpdate.updateFrom(geographicZone);
      geographicZoneToUpdate = geographicZoneRepository.save(geographicZoneToUpdate);

      LOGGER.debug("Saved geographicZone with id: " + geographicZoneToUpdate.getId());
      return new ResponseEntity<GeographicZone>(geographicZoneToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while saving geographicZone with id: "
                  + geographicZoneToUpdate.getId(), ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get chosen geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to get
   * @return geographicZone.
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getGeographicZone(@PathVariable("id") UUID geographicZoneId) {
    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicZone, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteGeographicZone(@PathVariable("id") UUID geographicZoneId) {
    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        geographicZoneRepository.delete(geographicZone);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
              new ErrorResponse("An error accurred while deleting geographicZone with id: "
                    + geographicZoneId, ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<GeographicZone>(HttpStatus.NO_CONTENT);
    }
  }
}
