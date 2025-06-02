package com.cas.packinglist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packing_list_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PackingListCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_list_id", nullable = false)
    private PackingList packingList;

    @Column(nullable = false)
    private String title;

    @Column(name = "\"order\"", nullable = false) // Quoting "order" as it's a reserved keyword
    private Integer displayOrder;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC") // Assuming items should be ordered
    private List<PackingListItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Helper methods for bidirectional relationship
    public void addItem(PackingListItem item) {
        items.add(item);
        item.setCategory(this);
    }

    public void removeItem(PackingListItem item) {
        items.remove(item);
        item.setCategory(null);
    }
}
