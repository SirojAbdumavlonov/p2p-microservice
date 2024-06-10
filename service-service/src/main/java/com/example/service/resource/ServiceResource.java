package com.example.service.resource;

import com.example.service.entity.Service;
import com.example.service.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceResource {

    private final ServiceService service;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<?> addService(@Validated @RequestBody Service newService) throws BadRequestException, URISyntaxException {
        log.info("REST request to add service");
        Integer serviceId = service.addService(newService);

        return ResponseEntity.created(
                new URI("/api/services/" + serviceId)
        ).body("Service added successfully");
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<Service> getServiceById(@PathVariable Integer serviceId) {
        log.info("REST request to get service");
        Service newService = service.getServiceById(serviceId);
        log.info("Found service: {}", newService);

        return ResponseEntity.ok().body(newService);
    }
    @GetMapping("")
    public ResponseEntity<List<Service>> getAllServices() {
        log.info("REST request to get all services");
        List<Service> services = service.getAllServices();
        log.info("Found {} services: {}", services.size(), services);

        return ResponseEntity.ok().body(services);
    }


    @PutMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Only administrators can update services
    public ResponseEntity<Service> updateService(@PathVariable Integer serviceId, @Valid @RequestBody Service updatableService) {
        log.info("REST request to update service: {}", updatableService);
        Service updatedService = service.updateService(updatableService, serviceId);
        log.info("Updated service: {}", updatedService);
        return ResponseEntity.ok().body(updatedService);
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Only administrators can delete services
    public ResponseEntity<String> deleteService(@PathVariable Integer serviceId) {
        log.info("REST request to delete service: {}", serviceId);
        service.deleteService(serviceId);
        log.info("Deleted service: {}", serviceId);
        return ResponseEntity.ok().body("Deleted successfully!");
    }


}
