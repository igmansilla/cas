package com.cas.packinglist.repository;

import com.cas.packinglist.model.PackingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingListItemRepository extends JpaRepository<PackingListItem, Long> {
    // You can add custom query methods here if needed in the future
}
