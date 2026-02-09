package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Member;

import java.util.List;

public interface MemberRepository {

    void save(Member member);

    Member findById(Long id);

    List<Member> findAll();

    void delete(Member member);

    void update(Member member);
}