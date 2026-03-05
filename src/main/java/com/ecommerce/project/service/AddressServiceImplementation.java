package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.DuplicateItemException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImplementation implements AddressService {
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    public AddressServiceImplementation(ModelMapper modelMapper, AddressRepository addressRepository, UserRepository userRepository, AuthUtil authUtil) {
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Override
    @Transactional
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        // Re-fetch user with addresses eagerly loaded within the transaction
        User userWithAddresses = userRepository.findWithAddressesByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", user.getUserId()));

        boolean alreadyExists = userWithAddresses.getAddresses().stream().anyMatch(existing ->
                existing.getStreet().equalsIgnoreCase(addressDTO.getStreet()) &&
                existing.getBuildingName().equalsIgnoreCase(addressDTO.getBuildingName()) &&
                existing.getCity().equalsIgnoreCase(addressDTO.getCity()) &&
                existing.getState().equalsIgnoreCase(addressDTO.getState()) &&
                existing.getCountry().equalsIgnoreCase(addressDTO.getCountry()) &&
                existing.getPincode().equalsIgnoreCase(addressDTO.getPincode())
        );

        if (alreadyExists) {
            throw new DuplicateItemException("Address", "details", addressDTO.getStreet() + ", " + addressDTO.getCity());
        }

        List<Address> addressList = userWithAddresses.getAddresses();
        Address address = modelMapper.map(addressDTO, Address.class);
        addressList.add(address);
        userWithAddresses.setAddresses(addressList);
        address.setUser(userWithAddresses);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesForUser(User user) {
        List<Address> addresses = addressRepository.findByUserId(user.getUserId());
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        existingAddress.setCity(addressDTO.getCity());
        existingAddress.setCountry(addressDTO.getCountry());
        existingAddress.setPincode(addressDTO.getPincode());
        existingAddress.setState(addressDTO.getState());
        existingAddress.setStreet(addressDTO.getStreet());
        existingAddress.setBuildingName(addressDTO.getBuildingName());
        Address updatedAddress = addressRepository.save(existingAddress);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
        return modelMapper.map(address, AddressDTO.class);
    }
}
