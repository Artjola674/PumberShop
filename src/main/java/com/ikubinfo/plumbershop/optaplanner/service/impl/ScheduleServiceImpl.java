package com.ikubinfo.plumbershop.optaplanner.service.impl;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.email.EmailHelper;
import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.kafka.KafkaProducer;
import com.ikubinfo.plumbershop.optaplanner.dto.ScheduleDto;
import com.ikubinfo.plumbershop.optaplanner.mapper.ScheduleMapper;
import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import com.ikubinfo.plumbershop.optaplanner.model.SellerAvailabilityDocument;
import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.repo.ScheduleRepository;
import com.ikubinfo.plumbershop.optaplanner.service.SellerAvailabilityService;
import com.ikubinfo.plumbershop.optaplanner.service.ScheduleService;
import com.ikubinfo.plumbershop.optaplanner.service.ShiftService;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mapstruct.factory.Mappers;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.optaplanner.constants.Constants.*;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final SolverManager<ScheduleDocument, String> solverManager;
    private final UserService userService;
    private final SellerAvailabilityService sellerAvailabilityService;
    private final ShiftService shiftService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final KafkaProducer kafkaProducer;
    @Value("${documents.folder}")
    private String documentPath;

    public ScheduleServiceImpl(SolverManager<ScheduleDocument, String> solverManager,
                               UserService userService, SellerAvailabilityService sellerAvailabilityService,
                               ShiftService shiftService, ScheduleRepository scheduleRepository, KafkaProducer kafkaProducer) {
        this.solverManager = solverManager;
        this.userService = userService;
        this.sellerAvailabilityService = sellerAvailabilityService;
        this.shiftService = shiftService;
        this.scheduleRepository = scheduleRepository;
        this.kafkaProducer = kafkaProducer;
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

        String filename = generateExcel(solution);

        List<String> emails = userService.getAllUsersBasedOnRole(Role.ADMIN)
                .stream()
                .map(UserDocument::getEmail)
                .toList();

        MessageRequest messageRequest = EmailHelper.createScheduleRequest(documentPath, filename, emails);
        kafkaProducer.sendMessage(messageRequest);
        scheduleRepository.save(solution);

        return scheduleMapper.toDto(solution);
    }

    @Override
    public Page<ScheduleDto> findAll(PageParams pageParams) {
        Pageable pageable = PageRequest.of(pageParams.getPageNumber(), pageParams.getPageSize(),
                Sort.by(Sort.Direction.valueOf(pageParams.getSortType()),
                        UtilClass.getSortField(UserDocument.class, pageParams.getSortBy())));
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
        List<UserDocument> sellers = userService.getAllUsersBasedOnRole(Role.SELLER);
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

    private String generateExcel(ScheduleDocument schedule) {

        String filename = UtilClass.createRandomString() + EXTENSION_XLSX;

        try (Workbook workbook = new XSSFWorkbook()){

            String filePath = documentPath + filename;

            Sheet sheet = workbook.createSheet(SCHEDULE);

            createHeaders(sheet);

            createRows(schedule, sheet);

            FileOutputStream fileOut = new FileOutputStream(filePath);

            workbook.write(fileOut);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Something went wrong while creating excel file");
        }

        return filename;
    }

    private void createRows(ScheduleDocument schedule, Sheet sheet) {
        int rowNum = 1;
        for (ShiftDocument shift : schedule.getShiftList()) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, shift.getSeller().getFirstName().concat(SPACE).concat(shift.getSeller().getLastName()));
            createCell(row,1, shift.getDepartment().toString());
            createCell(row,2, shift.getStartDateTime().toString());
            createCell(row,3, shift.getEndDateTime().toString());
        }
    }

    private void createHeaders(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, USER_FULL_NAME);
        createCell(headerRow, 1,DEPARTMENT);
        createCell(headerRow, 2, SHIFT_START_DATE);
        createCell(headerRow, 3, SHIFT_END_DATE);
    }

    private void createCell(Row row, int cellNumber, String value){
        row.createCell(cellNumber).setCellValue(value);
    }


}
