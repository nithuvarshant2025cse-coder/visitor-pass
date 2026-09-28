package com.example.visitorpass.service;

import com.example.visitorpass.entity.Flat;
import com.example.visitorpass.exception.BusinessException;
import com.example.visitorpass.exception.ResourceNotFoundException;
import com.example.visitorpass.repository.FlatRepository;
import com.example.visitorpass.repository.ResidentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlatService {
    private final FlatRepository flatRepo;
    private final ResidentRepository residentRepo;

    public FlatService(FlatRepository flatRepo, ResidentRepository residentRepo) {
        this.flatRepo = flatRepo;
        this.residentRepo = residentRepo;
    }

    @Transactional(readOnly = true)
    public List<Flat> findAll() { return flatRepo.findAll(); }

    @Transactional(readOnly = true)
    public Flat findById(Long id) {
        return flatRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flat not found with id " + id));
    }

    @Transactional
    public Flat create(Flat f) {
        if (flatRepo.existsByBlockIgnoreCaseAndFlatNumberIgnoreCase(f.getBlock().trim(), f.getFlatNumber().trim()))
            throw new BusinessException(HttpStatus.CONFLICT, "Flat already exists in this block");
        Flat flat = new Flat();
        flat.setBlock(f.getBlock().trim());
        flat.setFlatNumber(f.getFlatNumber().trim());
        return flatRepo.save(flat);
    }

    @Transactional
    public Flat update(Long id, Flat f) {
        Flat flat = findById(id);
        boolean changed = !flat.getBlock().equalsIgnoreCase(f.getBlock().trim())
                || !flat.getFlatNumber().equalsIgnoreCase(f.getFlatNumber().trim());
        if (changed && flatRepo.existsByBlockIgnoreCaseAndFlatNumberIgnoreCase(f.getBlock().trim(), f.getFlatNumber().trim()))
            throw new BusinessException(HttpStatus.CONFLICT, "Flat already exists in this block");
        flat.setBlock(f.getBlock().trim());
        flat.setFlatNumber(f.getFlatNumber().trim());
        return flatRepo.save(flat);
    }

    @Transactional
    public void delete(Long id) {
        Flat flat = findById(id);
        if (residentRepo.existsByFlatId(id))
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot delete flat: residents are assigned to it");
        flatRepo.delete(flat);
    }
}
