package com.cas.packinglist.repository;

import com.cas.packinglist.model.PackingListCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingListCategoryRepository extends JpaRepository<PackingListCategory, Long> {
    // You can add custom query methods here if needed in the future
}
