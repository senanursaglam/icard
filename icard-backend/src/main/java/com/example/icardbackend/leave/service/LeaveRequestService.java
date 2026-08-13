package com.example.icardbackend.leave.service;

import com.example.icardbackend.common.exception.LeaveRequestException.LeaveAlreadyDecidedException;
import com.example.icardbackend.common.exception.LeaveRequestException.LeaveOverlapException;
import com.example.icardbackend.common.exception.LeaveRequestException.LeaveRequestNotFoundException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;
import com.example.icardbackend.leave.dto.LeaveRequestCreateRequest;
import com.example.icardbackend.leave.entity.LeaveRequest;
import com.example.icardbackend.leave.entity.LeaveStatus;
import com.example.icardbackend.leave.repository.LeaveRequestRepository;
import com.example.icardbackend.qr.service.MailService;
import com.example.icardbackend.user.entity.RoleType;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final MailService mailService;
    private final UserRepository userRepository;

    @Transactional
    public LeaveRequest create(User user, LeaveRequestCreateRequest request) {

        // Gelen 'user' nesnesi, farklı (ve artık kapanmış) bir oturumdan geliyor olabilir.
        // Bu metodun kendi oturumunda, kullanıcıyı tekrar çekiyoruz
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + user.getId()));

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                managedUser, request.getStartDate(), request.getEndDate());

        if (!overlapping.isEmpty()) {
            throw new LeaveOverlapException(
                    "Bu tarih aralığında zaten bekleyen veya onaylanmış bir izin talebiniz var.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(managedUser)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        if (managedUser.getManager() != null) {
            mailService.sendLeaveRequestNotificationToManager(
                    managedUser.getManager().getEmail(),
                    managedUser.getFirstName() + " " + managedUser.getLastName(),
                    saved.getStartDate(),
                    saved.getEndDate()
            );
        }

        return saved;
    }

    public List<LeaveRequest> getMyLeaves(User user) {
        return leaveRequestRepository.findByUser(user);
    }

    public Page<LeaveRequest> getTeamLeaves(List<User> team, int page, int size, LeaveStatus status) {
        if (team.isEmpty()) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            return leaveRequestRepository.findByUserInAndStatus(team, status, pageable);
        }
        return leaveRequestRepository.findByUserIn(team, pageable);
    }

    @Transactional
    public LeaveRequest approve(Long leaveRequestId, User approver) {
        return decide(leaveRequestId, approver, true);
    }

    @Transactional
    public LeaveRequest reject(Long leaveRequestId, User approver) {
        return decide(leaveRequestId, approver, false);
    }

    private LeaveRequest decide(Long leaveRequestId, User approver, boolean approve) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("İzin talebi bulunamadı: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveAlreadyDecidedException("Bu izin talebi zaten karara bağlanmış.");
        }

        boolean isOwnTeamMember = leaveRequest.getUser().getManager() != null
                && leaveRequest.getUser().getManager().getId().equals(approver.getId());

        boolean isAdmin = approver.getRole() == RoleType.ADMIN;

        if (!isOwnTeamMember && !isAdmin) {
            throw new AccessDeniedException("Sadece kendi ekibinizin izin taleplerini karara bağlayabilirsiniz.");
        }

        leaveRequest.setStatus(approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setDecisionDate(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        mailService.sendLeaveDecisionNotification(
                saved.getUser().getEmail(),
                approve,
                saved.getStartDate(),
                saved.getEndDate()
        );

        return saved;
    }
}