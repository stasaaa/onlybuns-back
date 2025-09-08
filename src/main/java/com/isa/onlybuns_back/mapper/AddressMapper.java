package com.isa.onlybuns_back.mapper;

import com.isa.onlybuns_back.dto.AddressDto;
import com.isa.onlybuns_back.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public static AddressDto toDto(Address address) {
        return AddressDto.builder()
                .city(address.getCity())
                .country(address.getCountry())
                .street(address.getStreet())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .number(address.getNumber())
                .postalCode(address.getPostalCode())
                .build();
    }

    public static Address toEntity(AddressDto dto) {
        return Address.builder()
                .city(dto.getCity())
                .country(dto.getCountry())
                .street(dto.getStreet())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .number(dto.getNumber())
                .postalCode(dto.getPostalCode())
                .build();
    }
}
