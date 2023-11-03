package com.ikubinfo.plumbershop.optaplanner.service.impl;

import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.mapper.ScheduleMapper;
import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ScheduleRepository;
import com.ikubinfo.plumbershop.optaplanner.service.SellerAvailabilityService;
import com.ikubinfo.plumbershop.optaplanner.service.ScheduleService;
import com.ikubinfo.plumbershop.optaplanner.service.ShiftService;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final SolverManager<ScheduleDocument, String> solverManager;
    private final UserService userService;
    private final SellerAvailabilityService sellerAvailabilityService;
    private final ShiftService shiftService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    public ScheduleServiceImpl(SolverManager<ScheduleDocument, String> solverManager,
                               UserService userService, SellerAvailabilityService sellerAvailabilityService,
                               ShiftService shiftService, ScheduleRepository scheduleRepository) {
        this.solverManager = solverManager;
        this.userService = userService;
        this.sellerAvailabilityService = sellerAvailabilityService;
        this.shiftService = shiftService;
        this.scheduleRepository = scheduleRepository;
        this.scheduleMapper = Mappers.getMapper(ScheduleMapper.class);
    }

    @Override
    public ScheduleDto solve(int numberOfDays) {
        shiftService.deleteAll();
        List<ShiftDocument> shifts = shiftService.createShiftList(numberOfDays);
        List<UserDocument> sellers = userService.getAllSellers();
        List<SellerAvailabilityDocument> availabilityList =
                sellerAvailabilityService.findAllByDates(shifts.get(0).getStartDateTime(),
                        shifts.get(shifts.size()-1).getEndDateTime());
        String problemId = UUID.randomUUID().toString();
        ScheduleDocument problem = new ScheduleDocument();
        problem.setShiftList(shifts);
        problem.setAvailabilityList(availabilityList);
        problem.setEmployeeList(sellers);

        SolverJob<ScheduleDocument, String> solverJob = solverManager.solve(problemId, problem);
        ScheduleDocument solution;
        try {
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IllegalStateException("Solving failed.", e);
        }

        return scheduleMapper.toDto(scheduleRepository.save(solution));
    }

}
