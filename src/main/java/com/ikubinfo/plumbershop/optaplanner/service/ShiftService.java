package com.ikubinfo.plumbershop.optaplanner.service;

import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;

import java.util.List;

public interface ShiftService {

    List<ShiftDocument> createShiftList(int daysAfterToday);

    void deleteAll();
}
