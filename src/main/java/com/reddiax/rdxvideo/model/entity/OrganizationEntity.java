package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.OrganizationTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ORGANIZATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrganizationEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    @Builder.Default
    private OrganizationTypeEnum organizationType = OrganizationTypeEnum.PUBLISHER;

    private Long logoImageId;
    
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private String subscriptionTier;
}
