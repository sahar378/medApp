package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Machine;
import privateApp.repositories.MachineRepository;
import privateApp.services.MachineService;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    @Autowired
    private MachineService machineService;
    
    @Autowired
    private MachineRepository machineRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INFIRMIER', 'INTENDANT')")
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Machine> addMachine(@RequestBody Machine machine) {
        return ResponseEntity.ok(machineService.addMachine(machine));
    }

    @PutMapping("/{id}/disponibilite")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Machine> updateDisponibilite(@PathVariable Long id, @RequestParam int disponibilite) {
        return ResponseEntity.ok(machineService.updateDisponibilite(id, disponibilite));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Void> archiveMachine(@PathVariable Long id) {
        machineService.archiveMachine(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INFIRMIER', 'INTENDANT')")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        Machine machine = machineService.getMachineById(id);
        return ResponseEntity.ok(machine);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Void> updateMachine(@PathVariable Long id, @RequestBody Machine machine) {
        machineService.updateMachine(id, machine);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/non-archived")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'INFIRMIER')")
    public ResponseEntity<List<Machine>> getNonArchivedMachines() {
        List<Machine> machines = machineService.getNonArchivedMachines();
        return ResponseEntity.ok(machines);
    }

    @GetMapping("/archived")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Machine>> getArchivedMachines() {
        List<Machine> machines = machineService.getArchivedMachines();
        return ResponseEntity.ok(machines);
    }
    
    @GetMapping("/disponibles")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Machine>> getAvailableMachines() {
        List<Machine> machines = machineRepository.findByDisponibiliteAndArchivedFalse(0);
        return ResponseEntity.ok(machines);
    }
}