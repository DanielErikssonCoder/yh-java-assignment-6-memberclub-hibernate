package com.eriksson.rentalsystemhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.Member;
import com.eriksson.rentalsystemhibernate3.entity.Rental;
import com.eriksson.rentalsystemhibernate3.repo.MemberRepository;
import com.eriksson.rentalsystemhibernate3.repo.RentalRepository;
import com.eriksson.rentalsystemhibernate3.exception.*;
import java.util.List;

public class MemberService {

    private final MemberRepository memberRepository;
    private final RentalRepository rentalRepository;

    public MemberService(MemberRepository memberRepository, RentalRepository rentalRepository) {
        this.memberRepository = memberRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Validates input, ensures unique email, persists member
     */
    public void createMember(String firstName, String lastName, String email) {

        validateMemberData(firstName, lastName, email);

        boolean emailExists = memberRepository.findAll().stream()
                .anyMatch(m -> m.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new MemberAlreadyExistsException("En medlem med e-post " + email + " finns redan.");
        }

        Member member = new Member(firstName, lastName, email);
        memberRepository.save(member);
    }

    /**
     * Updates member details, validates input, enforces unique email
     */
    public void updateMember(Long id, String firstName, String lastName, String email) {
        validateMemberData(firstName, lastName, email);

        Member member = memberRepository.findById(id);

        if (member == null) {
            throw new EntityNotFoundException("Kan inte uppdatera: Medlem med ID " + id + " hittades inte.");
        }

        // Validates email uniqueness against other members
        boolean emailTakenByOther = memberRepository.findAll().stream()
                .anyMatch(m -> m.getEmail().equalsIgnoreCase(email) && !m.getMemberId().equals(id));

        if (emailTakenByOther) {
            throw new MemberAlreadyExistsException("E-postadressen " + email + " används redan av en annan medlem.");
        }

        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);

        memberRepository.update(member);
    }

    private void validateMemberData(String firstName, String lastName, String email) {

        // Checks that names are non‑blank, throws if invalid
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new InvalidMemberDataException("Förnamn och efternamn får inte vara tomma.");
        }

        // Validates e‑mail forma, throws on invalid
        if (email == null || !email.contains("@")) {
            throw new InvalidMemberDataException("Ogiltig e-postadress: " + email);
        }
    }

    public Member getMember(Long id) {

        Member member = memberRepository.findById(id);

        if (member == null) {
            throw new EntityNotFoundException("Medlem med ID " + id + " hittades inte.");
        }

        return member;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Deletes member only if no active rentals and verifies existence
     */
    public void deleteMember(Long id) {

        Member member = memberRepository.findById(id);

        if (member == null) {
            throw new EntityNotFoundException("Kan inte radera: Medlem med ID " + id + " saknas.");
        }

        List<Rental> allRentals = rentalRepository.findAll();

        // Checks for active rentals before deletion
        boolean hasActiveRentals = allRentals.stream().anyMatch(r -> r.getMember().getMemberId().equals(id) && r.getEndDate() == null);

        if (hasActiveRentals) {
            throw new MemberHasActiveRentalsException("Kan inte radera medlem som har pågående uthyrningar.");
        }

        memberRepository.delete(member);
    }
}