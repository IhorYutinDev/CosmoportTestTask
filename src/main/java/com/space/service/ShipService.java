package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ShipService {
    Ship create(Ship ship);

    Ship read(Long id);

    ResponseEntity<Ship> update(Ship ship, Long id);

    boolean deleteShip(Long id);

    List<Ship> filterByAllField(String name, String planet, ShipType shipType, Long after,
                                Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                Double maxRating);

    boolean isValid(String idString);

    List<Ship> sort(List<Ship> shipList, ShipOrder shipOrder);

    List<Ship> getShips(String name, String planet, ShipType shipType, Long after,
                        Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                        Integer minCrewSize, Integer maxCrewSize, Double minRating,
                        Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize);

    Ship getShipById(Long id);
}
