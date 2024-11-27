package com.tms.sportlight.service;

import com.tms.sportlight.domain.*;
import com.tms.sportlight.dto.*;
import com.tms.sportlight.dto.AdminCourseLocationDTO;
import com.tms.sportlight.exception.BizException;
import com.tms.sportlight.exception.ErrorCode;
import com.tms.sportlight.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminCourseRepository adminCourseRepository;
    private final AdminAttendCourseRepository adminAttendCourseRepository;
    private final AdminRefundRepository adminRefundRepository;
    private final CouponRepository couponRepository;
    private final EventRepository eventRepository;
    private final AdminAdjustmentRepository adminAdjustmentRepository;
    private AdjustmentRepository adjustmentRepository;
    private final AdminHostRequestRepository adminHostRequestRepository;

    public long getUserCount() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> AdminUserDTO.builder()
                        .id(user.getId())
                        .loginId(user.getLoginId())
                        .userName(user.getUserName())
                        .userGender(user.getUserGender())
                        .role(user.getRoles().isEmpty() ? null : user.getRoles().get(0))
                        .regDate(user.getRegDate())
                        .build())
                .collect(Collectors.toList());
    }


    public long getCourseCount() {
        return adminCourseRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getOpenCourseCountsGroupedByStartTime() {
        return adminCourseRepository.getOpenCourseCountsGroupedByStartTime();
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getTop3UsersByRevenue() {
        List<Object[]> resultList = adminUserRepository.findTop3UsersByRevenue();

        return resultList.stream().map(result -> UserDTO.builder()
                .loginId((String) result[1])
                .userNickname((String) result[2])
                .userIntroduce((String) result[3])
                .userName((String) result[4])
                .userPhone((String) result[5])
                .marketingAgreement(((Number) result[6]).intValue() == 1)
                .personalAgreement(((Number) result[7]).intValue() == 1)
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminCourseDTO> getAllCourses() {
        return adminCourseRepository.findAll().stream()
                .map(course -> AdminCourseDTO.builder()
                        .id(course.getId())
                        .categoryName(course.getCategory().getName())
                        .courseTitle(course.getTitle())
                        .courseTuition(course.getTuition())
                        .maxCapacity(course.getMaxCapacity())
                        .courseLevel(course.getLevel())
                        .regDate(course.getRegDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentDTO> getAllPaymentsWithoutRefund() {
        return adminAttendCourseRepository.findAllByRefundLogIsNull().stream()
                .map(attendCourse -> AdminPaymentDTO.builder()
                        .attendCourseId(attendCourse.getId())
                        .totalAmount(attendCourse.getTotalAmount())
                        .paymentFee(attendCourse.getPaymentFee())
                        .regDate(attendCourse.getRequestDate())
                        .userId(attendCourse.getUser().getId())
                        .courseOwnerId(attendCourse.getCourseSchedule().getCourse().getUser().getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminRefundDTO> getAllRefunds() {
        return adminRefundRepository.findAll().stream()
                .map(refunds -> AdminRefundDTO.builder()
                        .refundLogId(refunds.getId())
                        .refundRate(refunds.getRefundRate())
                        .refundAmount(refunds.getRefundAmount())
                        .requestDate(refunds.getRequestDate())
                        .userId(refunds.getAttendCourse().getUser().getId())
                        .courseOwnerId(refunds.getAttendCourse().getCourseSchedule().getCourse().getUser().getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminCouponDTO> getAllCouponsWithEvent() {
        return couponRepository.findAll().stream()
                .map(coupons -> AdminCouponDTO.builder()
                        .couponName(coupons.getName())
                        .eventName(coupons.getEvent().getName())
                        .discountRate(coupons.getDiscountRate())
                        .couponNum(coupons.getEvent().getNum())
                        .startDate(coupons.getEvent().getStartDate())
                        .endDate(coupons.getEvent().getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminEventDTO> getAllEventsWithCoupon() {
        return eventRepository.findAll().stream()
                .map(events -> {
                    String couponName = null;
                    double discountRate = 0.0;

                    if (events.getCoupons() != null && !events.getCoupons().isEmpty()) {
                        Coupon coupon = events.getCoupons().get(0);
                        couponName = coupon.getName();
                        discountRate = coupon.getDiscountRate();
                    }

                    return AdminEventDTO.builder()
                            .id(events.getId())
                            .name(events.getName())
                            .status(events.getStatus())
                            .content(events.getContent())
                            .num(events.getNum())
                            .classLink(events.getClassLink())
                            .startDate(events.getStartDate())
                            .endDate(events.getEndDate())
                            .couponName(couponName)
                            .discountRate(discountRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public AdminUserRoleCountDTO getUserRoleCounts() {
        Long userAndHostRoleCount = adminUserRepository.countUsersWithUserAndHostRoles();
        Long userRoleOnlyCount = adminUserRepository.countUsersWithUserRoleOnly();

        return new AdminUserRoleCountDTO(userAndHostRoleCount, userRoleOnlyCount);
    }

    public AdminUserAgeGroupCountDTO getUserAgeGroupCounts() {
        Object[] result = adminUserRepository.getUserAgeGroupCounts();

        return AdminUserAgeGroupCountDTO.builder()
                .teensCount(((Number) result[0]).longValue())
                .twentiesCount(((Number) result[1]).longValue())
                .thirtiesCount(((Number) result[2]).longValue())
                .fortiesCount(((Number) result[3]).longValue())
                .fiftiesCount(((Number) result[4]).longValue())
                .sixtiesCount(((Number) result[5]).longValue())
                .seventiesCount(((Number) result[6]).longValue())
                .eightiesCount(((Number) result[7]).longValue())
                .ninetiesCount(((Number) result[8]).longValue())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminGenderAgeGroupCourseCountDTO> getCourseCountsGroupedByGenderAndAge() {
        List<Object[]> results = adminAttendCourseRepository.getCourseCountsGroupedByGenderAndAge();

        return results.stream().map(result -> AdminGenderAgeGroupCourseCountDTO.builder()
                .gender((String) result[0])
                .ageGroup((String) result[1])
                .courseCount(((Number) result[2]).longValue())
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminCourseLocationDTO> getAllCourseTitlesAndLocations() {
        List<Object[]> results = adminCourseRepository.getAllCourseTitlesAndLocations();

        return results.stream().map(result -> AdminCourseLocationDTO.builder()
                .title((String) result[0])
                .latitude((double) result[1])
                .longitude((double) result[2])
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryCourseCountDTO> getCategoryCourseCounts() {
        List<Object[]> results = adminCourseRepository.getCategoryCourseCounts();

        return results.stream().map(result -> AdminCategoryCourseCountDTO.builder()
                .categoryName((String) result[0])
                .courseCount(((Number) result[1]).longValue())
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminAdjustmentDetailDTO> getAllAdjustments() {
        List<Object[]> results = adminAdjustmentRepository.getAllAdjustments();

        return results.stream().map(result -> AdminAdjustmentDetailDTO.builder()
                .adjustmentId((Integer) result[0])
                .userId((Long) result[1])
                .requestAmount((double) result[2])
                .adjustedCharge((double) result[3])
                .totalAmount((double) result[4])
                .reqDate(result[5].toString())
                .build()).collect(Collectors.toList());
    }
    public void updateAdjustmentStatus(int id, AdjustmentStatus status) {
        Adjustment adjustment = getAdjustmentId(id);
        adjustment.updateStatus(status);
    }

    protected Adjustment getAdjustmentId(int id) {
        return adjustmentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND_ADJUSTMENT));
    }

    public List<AdminHostRequestDTO> getAllHostRequests() {
        List<Object[]> results = adminHostRequestRepository.getAllHostRequests();

        return results.stream().map(result -> AdminHostRequestDTO.builder()
                .hostRequestId((Integer) result[0])
                .loginId((String) result[1])
                .userNickname((String) result[2])
                .userName((String) result[3])
                .reqStatus((String) result[4])
                .hostBio((String) result[5])
                .certification((String) result[6])
                .portfolio((String) result[7])
                .regDate(result[8].toString())
                .build()).collect(Collectors.toList());
    }

    public void updateHostRequestStatus(int id, HostRequestStatus status) {
        HostRequest hostRequest = getHostRequestId(id);
        hostRequest.updateStatus(status);
    }

    protected HostRequest getHostRequestId(int id) {
        return adminHostRequestRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND_ADJUSTMENT));
    }

    public List<AdminCourseRequestDTO> getAllCourseRequests() {
        List<Object[]> results = adminCourseRepository.getAllCourseRequests();

        return results.stream().map(result -> AdminCourseRequestDTO.builder()
                .courseId((Integer) result[0])
                .categoryName((String) result[1])
                .courseTitle((String) result[2])
                .courseTuition((double) result[3])
                .maxCapacity((int) result[4])
                .status((String) result[5])
                .regDate(result[6].toString())
                .build()).collect(Collectors.toList());
    }

    public void updateCourseRequestStatus(int id, CourseStatus status) {
        Course course = getCourseRequestId(id);
        course.updateStatus(status);
    }

    protected Course getCourseRequestId(int id) {
        return adminCourseRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND_ADJUSTMENT));
    }

    @Transactional
    public void createEvent(AdminEventRequestDTO eventRequestDTO) {
        Event event = Event.builder()
                .name(eventRequestDTO.getName())
                .content(eventRequestDTO.getContent())
                .startDate(eventRequestDTO.getStartDate())
                .endDate(eventRequestDTO.getEndDate())
                .classLink(eventRequestDTO.getClassLink())
                .regDate(LocalDateTime.now())
                .num(eventRequestDTO.getCouponNum())
                .status(1)
                .build();

        Event savedEvent = eventRepository.save(event);

        List<Coupon> coupons = eventRequestDTO.getCoupons().stream()
                .map(couponRequestDTO -> Coupon.builder()
                        .event(savedEvent)
                        .name(couponRequestDTO.getName())
                        .discountRate(couponRequestDTO.getDiscountRate())
                        .build())
                .collect(Collectors.toList());

        couponRepository.saveAll(coupons);
    }
}