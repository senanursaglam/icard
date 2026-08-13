package com.example.icardbackend.workschedule.service;

import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.common.exception.WorkScheduleException.WorkScheduleNotFoundException;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.repository.DepartmentRepository;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.workschedule.dto.WorkScheduleRequest;
import com.example.icardbackend.workschedule.entity.WorkSchedule;
import com.example.icardbackend.workschedule.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final DepartmentRepository departmentRepository;

    public WorkSchedule create(WorkScheduleRequest request) {
        Department department = resolveDepartment(request.getDepartmentId());

        WorkSchedule ws = WorkSchedule.builder()
                .department(department)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .toleranceMinutes(request.getToleranceMinutes())
                .build();

        return workScheduleRepository.save(ws);
    }

    public WorkSchedule update(Long id, WorkScheduleRequest request) {
        WorkSchedule ws = workScheduleRepository.findById(id)
                .orElseThrow(() -> new WorkScheduleNotFoundException("Mesai kuralı bulunamadı: " + id));

        ws.setDepartment(resolveDepartment(request.getDepartmentId()));
        ws.setStartTime(request.getStartTime());
        ws.setEndTime(request.getEndTime());
        ws.setToleranceMinutes(request.getToleranceMinutes());

        return workScheduleRepository.save(ws);
    }

    public List<WorkSchedule> getAll() {
        return workScheduleRepository.findAll();
    }

    // Kullanıcının bağlı olduğu departmana özel kural varsa onu, yoksa şirket genelini döner.
    // Hiçbiri tanımlı değilse null döner (bu durumda geç kalma/fazla mesai hesaplanmaz).
    public WorkSchedule resolveForUser(User user) {

        if (user.getDepartment() != null) {
            var departmentSchedule = workScheduleRepository.findByDepartment(user.getDepartment());
            if (departmentSchedule.isPresent()) {
                return departmentSchedule.get();
            }
        }

        return workScheduleRepository.findByDepartmentIsNull().orElse(null);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Departman bulunamadı: " + departmentId));
    }
}