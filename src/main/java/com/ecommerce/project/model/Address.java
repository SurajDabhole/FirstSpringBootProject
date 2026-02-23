package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street must be at least 5 characters long")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be at least 5 characters long")
    private String buildingName;

    @NotBlank
    @Size(min = 2, message = "City name must be at least 5 characters long")
    private String city;

    @NotBlank
    @Size(min = 5, message = "State name must be at least 5 characters long")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be at least 5 characters long")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pincode must be at least 6 characters long")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

    public Address(String buildingName, String city, String country, String pincode, String state, String street) {
        this.buildingName = buildingName;
        this.city = city;
        this.country = country;
        this.pincode = pincode;
        this.state = state;
        this.street = street;
    }
}
