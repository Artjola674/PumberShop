package com.ikubinfo.plumbershop.optaplanner.service.impl;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.SCHEDULE;

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

        ScheduleDocument problem = createProblem(numberOfDays);

        String problemId = UUID.randomUUID().toString();
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

    @Override
    public Page<ScheduleDto> getAll(Filter filter) {
        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(),
                Sort.by(Sort.Direction.valueOf(filter.getSortType()),
                        UtilClass.getSortField(UserDocument.class, filter.getSortBy())));
        return scheduleRepository.findAll(pageable).map(scheduleMapper::toDto);
    }

    @Override
    public ScheduleDto getById(String id) {
        ScheduleDocument document = findById(id);
        return scheduleMapper.toDto(document);
    }

    @Override
    public String deleteById(String id) {
        scheduleRepository.deleteById(id);
        return DELETED_SUCCESSFULLY.replace(DOCUMENT,SCHEDULE);
    }

    private ScheduleDocument findById(String id) {
        return scheduleRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(SCHEDULE,ID, id));
    }

    private ScheduleDocument createProblem(int numberOfDays) {
        List<ShiftDocument> shifts = shiftService.createShiftList(numberOfDays);
        List<UserDocument> sellers = userService.getAllSellers();
        List<SellerAvailabilityDocument> availabilityList =
                sellerAvailabilityService.findAllByDates(shifts.get(0).getStartDateTime(),
                        shifts.get(shifts.size()-1).getEndDateTime());

        ScheduleDocument problem = new ScheduleDocument();
        problem.setShiftList(shifts);
        problem.setAvailabilityList(availabilityList);
        problem.setEmployeeList(sellers);
        shiftService.deleteAll();
        return problem;
    }


}
