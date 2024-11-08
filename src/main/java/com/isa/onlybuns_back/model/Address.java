package com.isa.onlybuns_back.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Address {
    private String country;
    private String postalCode;
    private String city;
    private String street;
    private String number;

    public Address() {
    }

    public Address(String country, String postalCode, String city, String street, String number) {
        this.country = country;
        this.postalCode = postalCode;
        this.city = city;
        this.street = street;
        this.number = number;
    }
}
