package org.example.impl.services;

import org.example.declarations.CatDao;
import org.example.declarations.OwnerDao;
import org.example.exceptions.SaveExistOwner;
import org.example.exceptions.UnknownOwner;
import org.example.impl.dto.OwnerDto;
import org.example.implementations.entities.Cat;
import org.example.implementations.entities.Owner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OwnerService {
    private final OwnerDao ownerDao;
    private final CatDao catDao;
    @Autowired
    public OwnerService(OwnerDao ownerDao, CatDao catDao) {
        this.ownerDao = ownerDao;
        this.catDao = catDao;
    }

    public ResponseEntity<?> getById(Integer id) throws UnknownOwner {
        Optional<Owner> ownerOptional = ownerDao.findById(id);
        if (ownerOptional.isEmpty())
            throw new UnknownOwner("unknown owner");

        Owner owner = ownerOptional.get();

        return new ResponseEntity<>(convertOwnerToDto(owner), HttpStatus.OK);
    }
    public ResponseEntity<?> save(OwnerDto ownerDto) throws SaveExistOwner {
        if (ownerDao.findById(ownerDto.getId()).isPresent())
            throw new SaveExistOwner();

        return new ResponseEntity<>(convertOwnerToDto(ownerDao.save(convertDtoToOwner(ownerDto))), HttpStatus.CREATED);
    }
    public void delete(Integer id){
        ownerDao.deleteById(id);
    }

    public ResponseEntity<?> update(Integer id, OwnerDto newOwner) throws UnknownOwner {
        if (ownerDao.findById(id).isEmpty())
            throw new UnknownOwner("try update unknown owner");

        return new ResponseEntity<>(ownerDao.save(convertDtoToOwner(newOwner)), HttpStatus.ACCEPTED);
    }
    private OwnerDto convertOwnerToDto(Owner owner) {
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(owner.getId());
        ownerDto.setRole(owner.getRole());
        ownerDto.setName(owner.getName());
        ownerDto.setDateOfBirth(owner.getDateOfBirth());
        ownerDto.setCatIds(owner.getCats()==null ? null : owner.getCats().stream().map(Cat::getId).collect(Collectors.toList()));
        ownerDto.setPassword(owner.getPassword());
        return ownerDto;
    }
    private Owner convertDtoToOwner(OwnerDto ownerDto) {
        Owner owner = new Owner();
        owner.setPassword(new BCryptPasswordEncoder(10).encode(ownerDto.getPassword()));
        owner.setRole(ownerDto.getRole());
        owner.setName(ownerDto.getName());
        owner.setId(ownerDto.getId());
        owner.setDateOfBirth(ownerDto.getDateOfBirth());
        owner.setCats(ownerDto.getCatIds() == null ? null : ownerDto.getCatIds().stream().map((Integer id) -> catDao.findById(id).get()).collect(Collectors.toList()));
        return owner;
    }
}
