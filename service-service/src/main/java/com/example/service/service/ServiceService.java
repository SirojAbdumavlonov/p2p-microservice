package com.example.service.service;


import com.example.service.exception.ServiceNotFoundException;
import com.example.service.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.service.entity.Service;
import org.apache.coyote.BadRequestException;

import java.util.Date;
import java.util.List;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional
    public Integer addService(Service service) throws BadRequestException {
        if(service.getId() != null){
            throw new BadRequestException("New user cannot have an id");
        }
        service.setCreatedAt(new Date());

        serviceRepository.save(service);
        log.debug("Service saved: {}", service);

        return service.getId();
    }

    public Service getServiceById(Integer serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Service updateService(Service service, Integer serviceId) {
        log.debug("Updating service: {}", service);
        if (!service.getId().equals(serviceId)) {
            throw new RuntimeException("Service not found");
        }
        checkIfServiceExists(serviceId);

        return serviceRepository.save(service);
    }

    public void deleteService(Integer serviceId) {
        log.debug("Deleting service: {}", serviceId);
        checkIfServiceExists(serviceId);

        serviceRepository.deleteById(serviceId);
    }

    private void checkIfServiceExists(Integer serviceId) {
        log.debug("Checking if service exists: {}", serviceId);
        if(!serviceRepository.existsById(serviceId)) {
            log.error("Service with id {} not found", serviceId);
            throw new ServiceNotFoundException(serviceId);
        }
    }
}
