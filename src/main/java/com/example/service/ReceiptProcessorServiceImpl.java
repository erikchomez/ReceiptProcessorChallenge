package com.example.service;

import com.example.model.Item;
import com.example.model.Receipt;
import com.example.validation.ReceiptValidator;
import com.example.validation.ValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReceiptProcessorServiceImpl implements ReceiptProcessorService {

    private final ReceiptValidator receiptValidator;
    private final HashMap<String, Integer> processIdToPoints = new HashMap<>();

    public ReceiptProcessorServiceImpl(ReceiptValidator receiptValidator) {
        this.receiptValidator = receiptValidator;
    }

    @Override
    public int getPoints(String id) {
        if (processIdToPoints.containsKey(id)) {
            int numPoints = processIdToPoints.get(id);
            log.info("Points for process id {} is {}", id, numPoints);
            return numPoints;
        } else {
            log.error("No points found for process id {}", id);
            throw new IllegalArgumentException("Invalid process id: " + id);
        }
    }

    @Override
    public String processPoints(Receipt receipt) {
        ValidationResponse validation = receiptValidator.validateReceipt(receipt);
        if (!validation.isValid()) {
            log.error("Encountered error(s) while validating receipt: {}", validation.getErrors());
            throw new IllegalArgumentException("Invalid receipt: " + validation.getErrors());
        }
        int calculatedPoints = calculatePoints(receipt);
        String processId = UUID.randomUUID().toString();
        processIdToPoints.put(processId, calculatedPoints);
        return processId;
    }

    /**
     * Calculates the number of points of a given receipt. The rules are as follows:
     * One point for every alphanumeric character in the retailer name.
     * 50 points if the total is a round dollar amount with no cents.
     * 25 points if the total is a multiple of 0.25.
     * 5 points for every two items on the receipt.
     * If the trimmed length of the item description is a multiple of 3, multiply the price by 0.2 and round up to the nearest integer. The result is the number of points earned.
     * 6 points if the day in the purchase date is odd.
     * 10 points if the time of purchase is after 2:00pm and before 4:00pm.
     * @param receipt the receipt used to calculate total number of points
     * @return total number of points
     */
    private int calculatePoints(Receipt receipt) {
        log.debug("Calculating points for receipt based on given criteria");
        int totalNumberOfPoints = 0;

        totalNumberOfPoints += calculatePointsForRetailerName(receipt.getRetailer());
        totalNumberOfPoints += calculatePointsForTotal(receipt.getTotal());
        totalNumberOfPoints += calculatePointsForItems(receipt.getItems());
        totalNumberOfPoints += calculatePointsForPurchaseData(receipt.getPurchaseDate());
        totalNumberOfPoints += calculatePointsForPurchaseTime(receipt.getPurchaseTime());

        log.info("Calculated points for given receipt as: {}", totalNumberOfPoints);
        return totalNumberOfPoints;
    }

    private int calculatePointsForRetailerName(String retailerName) {
        int points = 0;

        for (char c : retailerName.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                points++;
            }
        }

        log.info("Awarded {} total points based on retailer name {}", points, retailerName);
        return points;
    }

    private int calculatePointsForTotal(String total) {
        int points = 0;
        BigDecimal totalAmount = new BigDecimal(total);
        // check if the total is a whole number (round dollar amount)
        if (totalAmount.scale() == 0 || totalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Total is a round dollar amount with no cents, awarding 50 points");
            points += 50;
        }
        // check if the total is a multiple of 0.25
        if (totalAmount.remainder(new BigDecimal("0.25")).compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Total is a multiple of 0.25, awarding 25 points");
            points += 25;
        }

        log.info("Awarded {} total points based on total", points);
        return points;
    }

    private int calculatePointsForItems(List<Item> items) {
        int totalPoints = 0;
        // calculate points: 5 points for every 2 items
        totalPoints += (items.size() / 2) * 5;
        log.debug("Awarded {} points based on rule: 5 points for every 2 items", totalPoints);

        for (Item item : items) {
            int descriptionLength = item.getShortDescription().trim().length();
            // check if the trimmed length is a multiple of 3
            if (descriptionLength % 3 == 0) {
                // calculate points: price * 0.2, rounded up to nearest int
                BigDecimal price = new BigDecimal(item.getPrice());
                BigDecimal points = price.multiply(new BigDecimal("0.2")).setScale(0, RoundingMode.UP);
                log.debug("Trimmed item description is a multiple of 3, calculated {} points based on price * 0.2, rounded to nearest int", points);
                totalPoints += points.intValue();
            }
        }

        log.info("Awarded {} total points based on items", totalPoints);
        return totalPoints;
    }

    private int calculatePointsForPurchaseData(String purchaseDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(purchaseDate, formatter);
        int day = date.getDayOfMonth();

        int points = 0;
        // check if day is odd
        if (day % 2 != 0) {
            log.debug("Day is odd, awarding 6 points");
            points = 6;
        } else {
            log.debug("Day is even, no points awarded");
        }

        log.info("Awarded {} total points based on purchase date", points);
        return points;
    }

    private int calculatePointsForPurchaseTime(String purchaseTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = LocalTime.parse(purchaseTime, formatter);

        // Define the time range we will use to determine if points should be awarded
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        int points = 0;
        if (time.isAfter(startTime) && time.isBefore(endTime)) {
            log.debug("Purchase time falls between 2pm and 4pm, awarding 10 points");
            points = 10;
        } else {
            log.debug("Purchase time does not fall between 2 and 4, no points awarded");
        }

        log.info("Awarded {} total points based on purchase time", points);
        return points;
    }
}
