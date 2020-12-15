package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import com.space.service.TypeResultUpdateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class MyShipController {
    private final ShipService shipService;

    @Autowired
    public MyShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public ResponseEntity<List<Ship>> getShips(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String planet,
                                               @RequestParam(required = false) ShipType shipType,
                                               @RequestParam(required = false) Long after,
                                               @RequestParam(required = false) Long before,
                                               @RequestParam(required = false) Boolean isUsed,
                                               @RequestParam(required = false) Double minSpeed,
                                               @RequestParam(required = false) Double maxSpeed,
                                               @RequestParam(required = false) Integer minCrewSize,
                                               @RequestParam(required = false) Integer maxCrewSize,
                                               @RequestParam(required = false) Double minRating,
                                               @RequestParam(required = false) Double maxRating,
                                               @RequestParam(required = false) ShipOrder order,
                                               @RequestParam(required = false) Integer pageNumber,
                                               @RequestParam(required = false) Integer pageSize) {

        List<Ship> shipList = shipService.getShips(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);

        return new ResponseEntity<>(shipList, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getShipsCount(@RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String planet,
                                                 @RequestParam(required = false) ShipType shipType,
                                                 @RequestParam(required = false) Long after,
                                                 @RequestParam(required = false) Long before,
                                                 @RequestParam(required = false) Boolean isUsed,
                                                 @RequestParam(required = false) Double minSpeed,
                                                 @RequestParam(required = false) Double maxSpeed,
                                                 @RequestParam(required = false) Integer minCrewSize,
                                                 @RequestParam(required = false) Integer maxCrewSize,
                                                 @RequestParam(required = false) Double minRating,
                                                 @RequestParam(required = false) Double maxRating) {

        List<Ship> shipList = shipService.getShipsFilteredByAllField(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        return new ResponseEntity<>(shipList.size(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        Ship result = shipService.create(ship);
        return result != null
                ? new ResponseEntity<>(result, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ship> getShipById(@PathVariable String id) {
        if (!isIdValidNumber(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShipById(Long.parseLong(id));

        return ship != null ?
                new ResponseEntity<>(ship, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Ship> upDateShip(@PathVariable String id,
                                           @RequestBody Ship ship) {
        if (!isIdValidNumber(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship != null) {
            TypeResultUpdateStatus typeResultUpdateStatus = shipService.update(ship, Long.parseLong(id));
            switch (typeResultUpdateStatus) {
                case OK:
                    Ship shipUpdated = shipService.getShipById(Long.parseLong(id));
                    return new ResponseEntity<>(shipUpdated, HttpStatus.OK);
                case NOT_FOUND:
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                case BAD_REQUEST:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable String id) {
        if (!isIdValidNumber(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return shipService.deleteShip(Long.parseLong(id)) ?
                new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private boolean isIdValidNumber(String idString) {
        try {
            long id = Long.parseLong(idString);
            if (id < 1) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
