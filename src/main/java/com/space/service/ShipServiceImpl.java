package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;
    private final static int MAX_LENGTH_BOUND_NAME_PLANET = 50;
    private final static int DEFAULT_PAGE_SIZE = 3;
    private final static int DEFAULT_PAGE_NUMBER = 0;
    private final static double MIN_SPEED = 0.01;
    private final static double MAX_SPEED = 0.99;
    private final static int AFTER_PROD_YEAR = 2800;
    private final static int BEFORE_PROD_YEAR = 3019;
    private final static int MIN_CREW_SIZE = 1;
    private final static int MAX_CREW_SIZE = 9999;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship create(Ship ship) {
        if (isShipValuesNotValid(ship)) {
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

    private boolean isShipValuesNotValid(Ship ship) {
        String name = ship.getName();
        String planet = ship.getPlanet();
        Double speed = ship.getSpeed();
        Integer crewSize = ship.getCrewSize();

        return ship.getShipType() == null || name == null || name.length() > MAX_LENGTH_BOUND_NAME_PLANET
                || name.isEmpty() || planet == null || planet.length() > MAX_LENGTH_BOUND_NAME_PLANET
                || planet.isEmpty() || speed == null || speed > MAX_SPEED || speed < MIN_SPEED
                || ship.getProdDate() == null || getYear(ship) < AFTER_PROD_YEAR || getYear(ship) > BEFORE_PROD_YEAR
                || crewSize == null || crewSize < MIN_CREW_SIZE || crewSize > MAX_CREW_SIZE;
    }

    private double getRating(Ship ship) {
        double k = ship.getUsed() ? 0.5 : 1.0;
        int year = getYear(ship);
        return Math.round((100 * 80 * ship.getSpeed() * k) / (BEFORE_PROD_YEAR - year + 1)) / 100.0;
    }

    private int getYear(Ship ship) {
        Date date = ship.getProdDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    private List<Ship> sort(List<Ship> shipList, ShipOrder shipOrder) {
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
    public TypeResultUpdateStatus update(Ship ship, Long id) {
        if (!shipRepository.existsById(id)) {
            return TypeResultUpdateStatus.NOT_FOUND;
        }

        Ship shipUpdated = shipRepository.findById(id).orElse(null);

        if (shipUpdated != null) {
            if (ship.getName() != null) {
                String name = ship.getName();
                if (name.length() > MAX_LENGTH_BOUND_NAME_PLANET || name.isEmpty()) {
                    return TypeResultUpdateStatus.BAD_REQUEST;
                } else {
                    shipUpdated.setName(name);
                }
            }

            if (ship.getPlanet() != null) {
                String planet = ship.getPlanet();
                if (planet.length() > MAX_LENGTH_BOUND_NAME_PLANET || planet.isEmpty()) {
                    return TypeResultUpdateStatus.BAD_REQUEST;
                } else {
                    shipUpdated.setPlanet(planet);
                }
            }

            if (ship.getCrewSize() != null) {
                Integer crewSize = ship.getCrewSize();
                if (crewSize > 9999 || crewSize < 1) {
                    return TypeResultUpdateStatus.BAD_REQUEST;
                } else {
                    shipUpdated.setCrewSize(crewSize);
                }
            }

            if (ship.getSpeed() != null) {
                Double speed = ship.getSpeed();
                if (speed > MAX_SPEED || speed < MIN_SPEED) {
                    return TypeResultUpdateStatus.BAD_REQUEST;
                } else {
                    shipUpdated.setSpeed(Math.round(100.0 * speed) / 100.0);
                }
            }

            if (ship.getProdDate() != null) {
                int year = getYear(ship);
                if (year > BEFORE_PROD_YEAR || year < AFTER_PROD_YEAR) {
                    return TypeResultUpdateStatus.BAD_REQUEST;
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

            return TypeResultUpdateStatus.OK;

        } else return TypeResultUpdateStatus.NOT_FOUND;
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
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long after,
                               Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                               Integer minCrewSize, Integer maxCrewSize, Double minRating,
                               Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize) {

        List<Ship> shipsFiltered = getShipsFilteredByAllField(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        if (pageNumber == null) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        }
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        List<Ship> sortedShipsByOrder = order != null ? sort(shipsFiltered, order) : sort(shipsFiltered, ShipOrder.ID);

        List<Ship> result = new ArrayList<>();
        for (int i = pageNumber * pageSize; i < (pageNumber + 1) * pageSize; i++) {
            if (i < sortedShipsByOrder.size()) {
                result.add(sortedShipsByOrder.get(i));
            }
        }

        return result;
    }

    @Override
    public List<Ship> getShipsFilteredByAllField(String name, String planet, ShipType shipType, Long after,
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
        return shipRepository.findById(id).orElse(null);
    }
}
