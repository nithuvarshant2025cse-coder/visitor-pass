package com.example.visitorpass.service;

import com.example.visitorpass.dto.ResidentRequest;
import com.example.visitorpass.entity.Resident;
import com.example.visitorpass.exception.BusinessException;
import com.example.visitorpass.exception.ResourceNotFoundException;
import com.example.visitorpass.repository.ResidentRepository;
import com.example.visitorpass.repository.VisitorPreApprovalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResidentService {
    private final ResidentRepository residentRepo;
    private final VisitorPreApprovalRepository approvalRepo;
    private final FlatService flatService;

    public ResidentService(ResidentRepository residentRepo, VisitorPreApprovalRepository approvalRepo, FlatService flatService) {
        this.residentRepo = residentRepo;
        this.approvalRepo = approvalRepo;
        this.flatService = flatService;
    }

    @Transactional(readOnly = true)
    public List<Resident> findAll() { return residentRepo.findAll(); }

    @Transactional(readOnly = true)
    public Resident findById(Long id) {
        return residentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resident not found with id " + id));
    }

    @Transactional
    public Resident create(ResidentRequest r) { return save(new Resident(), r); }

    @Transactional
    public Resident update(Long id, ResidentRequest r) { return save(findById(id), r); }

    private Resident save(Resident res, ResidentRequest r) {
        res.setName(r.name().trim());
        res.setPhone(r.phone());
        res.setEmail(r.email().trim());
        res.setFlat(flatService.findById(r.flatId()));
        return residentRepo.save(res);
    }

    @Transactional
    public void delete(Long id) {
        Resident res = findById(id);
        if (approvalRepo.existsByResidentId(id))
            throw new BusinessException(HttpStatus.CONFLICT, "Cannot delete resident: pre-approvals exist for this resident");
        residentRepo.delete(res);
    }
}
