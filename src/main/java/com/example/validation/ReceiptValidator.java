package com.example.validation;

import com.example.model.Item;
import com.example.model.Receipt;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ReceiptValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ValidationResponse validateReceipt(Receipt receipt) {
        log.info("Validating receipt {}", receipt);
        ValidationResponse validationResponse = ValidationResponse
                .builder()
                .isValid(true)
                .build();

        if (StringUtils.isBlank(receipt.getRetailer())) {
            validationResponse.addErrors("Receipt retailer is blank");
        }

        if (StringUtils.isBlank(receipt.getPurchaseDate())) {
            validationResponse.addErrors("Receipt purchase date is blank");
        }

        if (!isValidPurchaseDate(receipt.getPurchaseDate())) {
            validationResponse.addErrors("Receipt purchase date is invalid: " + receipt.getPurchaseDate());
        }

        if (StringUtils.isBlank(receipt.getPurchaseTime())) {
            validationResponse.addErrors("Receipt purchase time is blank");
        }

        if (!isValidPurchaseTime(receipt.getPurchaseTime())) {
            validationResponse.addErrors("Receipt purchase time is invalid: " + receipt.getPurchaseTime());
        }

        if (StringUtils.isBlank(receipt.getTotal())) {
            validationResponse.addErrors("Receipt total is blank");
        }

        if (!isValidTotal(receipt.getTotal())) {
            validationResponse.addErrors("Receipt total is invalid: " + receipt.getTotal());
        }

        List<String> validationErrors = validateItems(receipt.getItems());
        if (!validationErrors.isEmpty()) {
            validationResponse.addErrors(validationErrors);
        }

        if (validationResponse.getErrors() != null && !validationResponse.getErrors().isEmpty()) {
            validationResponse.setValid(false);
        }

        return validationResponse;
    }

    private boolean isValidPurchaseDate(String purchaseDate) {
        try {
            LocalDate.parse(purchaseDate, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidPurchaseTime(String purchaseTime) {
        try {
            LocalTime.parse(purchaseTime, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidTotal(String total) {
        try {
            new BigDecimal(total);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<String> validateItems(List<Item> items) {
        List<String> errors = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            errors.add("Items list is empty or null");
            return errors;
        }

        for (Item item : items) {
            isValidItem(item, errors);
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        return Collections.emptyList();
    }

    private void isValidItem(Item item, List<String> errors) {
        if (item.getShortDescription() == null || item.getShortDescription().trim().isEmpty()) {
            errors.add("Item short description is blank or null");
        }

        if (item.getPrice() == null || item.getPrice().trim().isEmpty()) {
            errors.add("Item price is blank or null");
        } else {
            try {
                new BigDecimal(item.getPrice());
            } catch (NumberFormatException e) {
                errors.add("Item price is invalid: " + item.getPrice());
            }
        }
    }
}
