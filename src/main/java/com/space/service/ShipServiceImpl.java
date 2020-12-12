package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {

    private final ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship create(Ship ship) {
        String name = ship.getName();
        String planet = ship.getPlanet();
        Double speed = ship.getSpeed();
        Integer crewSize = ship.getCrewSize();

        if (ship.getShipType() == null || name == null || name.length() > 50 || name.isEmpty()
                || planet == null || planet.length() > 50 || planet.isEmpty() || speed == null || speed > 0.99
                || speed < 0.01 || ship.getProdDate() == null || getYear(ship) < 2800 || getYear(ship) > 3019
                || crewSize == null || crewSize < 1 || crewSize > 9999) {
            return null;
        }


        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setSpeed(Math.round(ship.getSpeed() * 100.0) / 100.0);
        ship.setRating(getRating(ship));
        shipRepository.save(ship);
        return ship;
    }

    private double getRating(Ship ship) {
        double k = ship.getUsed() ? 0.5 : 1.0;
        int year = getYear(ship);
        return Math.round((100 * 80 * ship.getSpeed() * k) / (3019 - year + 1)) / 100.0;
    }

    private int getYear(Ship ship) {
        Date shipProdDate = ship.getProdDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(shipProdDate);
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    @Override
    public List<Ship> readAll() {
        return shipRepository.findAll();
    }

    @Override
    public List<Ship> sort(List<Ship> shipList, ShipOrder shipOrder) {
        switch (shipOrder) {
            case ID:
                shipList.sort(Comparator.comparing(Ship::getId));
                break;
            case SPEED:
                shipList.sort(Comparator.comparing(Ship::getSpeed));
                break;
            case RATING:
                shipList.sort(Comparator.comparing(Ship::getRating));
                break;
            case DATE:
                shipList.sort(Comparator.comparing(Ship::getProdDate));
                break;
        }
        return shipList;
    }

    @Override
    public Ship read(Long id) {
        return shipRepository.getOne(id);
    }

    @Override
    public ResponseEntity<Ship> update(Ship ship, Long id) {
        if (!shipRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Ship shipUpdated = shipRepository.findById(id).get();

        if (ship.getName() != null) {
            String name = ship.getName();
            if (name.length() > 50 || name.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                shipUpdated.setName(name);
            }
        }

        if (ship.getPlanet() != null) {
            String planet = ship.getPlanet();
            if (planet.length() > 50 || planet.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                shipUpdated.setPlanet(planet);
            }
        }

        if (ship.getCrewSize() != null) {
            Integer crewSize = ship.getCrewSize();
            if (crewSize > 9999 || crewSize < 1) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                shipUpdated.setCrewSize(crewSize);
            }
        }

        if (ship.getSpeed() != null) {
            Double speed = ship.getSpeed();
            if (speed > 0.99 || speed < 0.01) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                shipUpdated.setSpeed(Math.round(100.0 * speed) / 100.0);
            }
        }

        if (ship.getProdDate() != null) {
            int year = getYear(ship);
            if (year > 3019 || year < 2800) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                shipUpdated.setProdDate(ship.getProdDate());
            }
        }

        if (ship.getUsed() != null) {
            shipUpdated.setUsed(ship.getUsed());
        }

        if (ship.getShipType() != null) {
            shipUpdated.setShipType(ship.getShipType());
        }

        if (ship.getProdDate() != null || ship.getSpeed() != null || ship.getUsed() != null) {
            Double rating = getRating(shipUpdated);
            shipUpdated.setRating(rating);
        }

        shipRepository.save(shipUpdated);

        return new ResponseEntity<>(shipUpdated, HttpStatus.OK);
    }


    @Override
    public boolean deleteShip(Long id) {
        if (shipRepository.existsById(id)) {
            shipRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid(String idString) {
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

    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long after,
                               Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                               Integer minCrewSize, Integer maxCrewSize, Double minRating,
                               Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize) {

        List<Ship> shipsFiltered = filterByAllField(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 3;
        }

        List<Ship> result = new ArrayList<>();
        for (int i = pageNumber * pageSize; i < (pageNumber + 1) * pageSize; i++) {
            if (i < shipsFiltered.size()) {
                result.add(shipsFiltered.get(i));
            }
        }

        if (order != null) {
            result = sort(result, order);
        } else {
            result = sort(result, ShipOrder.ID);
        }

        return result;
    }

    @Override
    public List<Ship> filterByAllField(String name, String planet, ShipType shipType, Long after,
                                       Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                       Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                       Double maxRating) {

        List<Ship> shipList = shipRepository.findAll();
        if (name != null) {
            shipList.removeIf(ship -> !ship.getName().contains(name));
        }

        if (planet != null) {
            shipList.removeIf(ship -> !ship.getPlanet().contains(planet));
        }

        if (shipType != null) {
            shipList.removeIf(ship -> !(ship.getShipType() == shipType));
        }

        if (after != null) {
            shipList.removeIf(ship -> ship.getProdDate().getTime() < after);
        }
        if (before != null) {
            shipList.removeIf(ship -> ship.getProdDate().getTime() > before);
        }

        if (minSpeed != null) {
            shipList.removeIf(ship -> ship.getSpeed() < minSpeed);
        }
        if (maxSpeed != null) {
            shipList.removeIf(ship -> ship.getSpeed() > maxSpeed);
        }

        if (isUsed != null) {
            shipList.removeIf(ship -> ship.getUsed() != isUsed);
        }

        if (minCrewSize != null) {
            shipList.removeIf(ship -> ship.getCrewSize() < minCrewSize);
        }
        if (maxCrewSize != null) {
            shipList.removeIf(ship -> ship.getCrewSize() > maxCrewSize);
        }

        if (minRating != null) {
            shipList.removeIf(ship -> ship.getRating() < minRating);
        }
        if (maxRating != null) {
            shipList.removeIf(ship -> ship.getRating() > maxRating);
        }
        return shipList;
    }

    @Override
    public Ship getShipById(Long id) {
        return shipRepository.existsById(id)
                ? shipRepository.findById(id).get() : null;
    }

}
