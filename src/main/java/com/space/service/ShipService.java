package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ShipService {
    Ship create(Ship ship);

    TypeResultUpdateStatus update(Ship ship, Long id);

    boolean deleteShip(Long id);

    List<Ship> getShipsFilteredByAllField(String name, String planet, ShipType shipType, Long after,
                                Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                Double maxRating);

    List<Ship> getShips(String name, String planet, ShipType shipType, Long after,
                        Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                        Integer minCrewSize, Integer maxCrewSize, Double minRating,
                        Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize);

    Ship getShipById(Long id);
}
