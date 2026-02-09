package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Rental;
import com.eriksson.rentalsystemhibernate3.entity.RentalType;
import java.util.List;

public interface RentalRepository {

    void save(Rental rental);

    Rental findById(Long id);

    List<Rental> findAll();

    boolean isObjectRented(Long objectId, RentalType type);
}