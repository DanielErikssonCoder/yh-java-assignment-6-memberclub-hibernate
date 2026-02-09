package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.GamingComputer;

import java.util.List;

public interface GamingComputerRepository {

    void save(GamingComputer gameComputer);

    GamingComputer findById(Long id);

    List<GamingComputer> findAll();

}