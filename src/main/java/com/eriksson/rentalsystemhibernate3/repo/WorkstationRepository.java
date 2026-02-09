package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Workstation;
import java.util.List;

public interface WorkstationRepository {

    void save(Workstation workstation);

    Workstation findById(Long id);

    List<Workstation> findAll();
}
