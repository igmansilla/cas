package com.cas.packinglist.repository;

import com.cas.packinglist.model.PackingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackingListRepository extends JpaRepository<PackingList, Long> {

    Optional<PackingList> findByUserId(Long userId);
}
