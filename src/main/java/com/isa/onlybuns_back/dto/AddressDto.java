package com.isa.onlybuns_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddressDto {
    @NotBlank(message = "Country is required")
    private String country;
    @NotBlank(message = "Country is required")
    private String postalCode;
    @NotBlank(message = "Country is required")
    private String city;
    @NotBlank(message = "Country is required")
    private String street;
    @NotBlank(message = "Country is required")
    private String number;
    @NotBlank(message = "Latitude is required")
    private Double latitude;
    @NotBlank(message = "Longitude is required")
    private Double longitude;

    public @NotBlank(message = "Country is required") String getCountry() {
        return country;
    }

    public void setCountry(@NotBlank(message = "Country is required") String country) {
        this.country = country;
    }

    public @NotBlank(message = "Country is required") String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(@NotBlank(message = "Country is required") String postalCode) {
        this.postalCode = postalCode;
    }

    public @NotBlank(message = "Country is required") String getCity() {
        return city;
    }

    public void setCity(@NotBlank(message = "Country is required") String city) {
        this.city = city;
    }

    public @NotBlank(message = "Country is required") String getStreet() {
        return street;
    }

    public void setStreet(@NotBlank(message = "Country is required") String street) {
        this.street = street;
    }

    public @NotBlank(message = "Country is required") String getNumber() {
        return number;
    }

    public void setNumber(@NotBlank(message = "Country is required") String number) {
        this.number = number;
    }
}
