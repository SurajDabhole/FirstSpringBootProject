package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAllAddressesForUser(User user);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddress(Long addressId);

    AddressDTO updateAddressById(Long addressId, @Valid AddressDTO addressDTO);

    AddressDTO deleteAddressById(Long addressId);
}
