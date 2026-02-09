package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Laptop;

import java.util.List;

public interface LaptopRepository {

    void save(Laptop laptop);

    Laptop findById(Long id);

    List<Laptop> findAll();
}
