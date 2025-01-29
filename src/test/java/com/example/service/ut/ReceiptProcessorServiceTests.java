package com.example.service.ut;

import com.example.model.Item;
import com.example.model.Receipt;
import com.example.service.ReceiptProcessorServiceImpl;
import com.example.validation.ReceiptValidator;
import com.example.validation.ValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReceiptProcessorServiceTests {
    @Mock private ReceiptValidator receiptValidator;
    @InjectMocks private ReceiptProcessorServiceImpl receiptProcessorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processPointsValidReceiptShouldReturnProcessId() {
        Receipt receipt = createValidReceipt();
        when(receiptValidator.validateReceipt(receipt))
                .thenReturn(ValidationResponse.builder().isValid(true).build());

        String processId = receiptProcessorService.processPoints(receipt);
        verify(receiptValidator, times(1)).validateReceipt(receipt);
    }

    @Test
    void processPointsInvalidReceiptShouldThrowException() {
        Receipt receipt = createValidReceipt();
        ValidationResponse validationResponse = ValidationResponse.builder()
                .isValid(false)
                .errors(Collections.singletonList("Invalid receipt"))
                .build();
        when(receiptValidator.validateReceipt(receipt)).thenReturn(validationResponse);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            receiptProcessorService.processPoints(receipt);
        });
        assertTrue(exception.getMessage().contains("Invalid receipt"));
        verify(receiptValidator, times(1)).validateReceipt(receipt);
    }

    @Test
    void calculatePointsValidReceiptShouldReturnCorrectPoints() {
        Receipt receipt = createValidReceipt();
        when(receiptValidator.validateReceipt(receipt))
                .thenReturn(ValidationResponse.builder().isValid(true).build());
        String processId = receiptProcessorService.processPoints(receipt);
        int points = receiptProcessorService.getPoints(processId);

        assertEquals(28, points);
    }

    @Test
    void calculatePointsValidReceiptShouldReturnCorrectPoints2() {
        Receipt receipt = createValidReceipt2();
        when(receiptValidator.validateReceipt(receipt))
                .thenReturn(ValidationResponse.builder().isValid(true).build());
        String processId = receiptProcessorService.processPoints(receipt);
        int points = receiptProcessorService.getPoints(processId);

        assertEquals(109, points);
    }

    private Receipt createValidReceipt() {
        List<Item> items = Arrays.asList(
                new Item("Mountain Dew 12PK", "6.49"),
                new Item("Emils Cheese Pizza", "12.25"),
                new Item("Knorr Creamy Chicken", "1.26"),
                new Item("Doritos Nacho Cheese", "3.35"),
                new Item("Klarbrunn 12-PK 12 FL OZ", "12.00")
        );

        Receipt receipt = new Receipt();
        receipt.setRetailer("Target");
        receipt.setPurchaseDate("2022-01-01");
        receipt.setPurchaseTime("13:01");
        receipt.setTotal("35.35");
        receipt.setItems(items);
        return receipt;
    }

    private Receipt createValidReceipt2() {
        List<Item> items = Arrays.asList(
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25"),
                new Item("Gatorade", "2.25")
        );

        Receipt receipt = new Receipt();
        receipt.setRetailer("M&M Corner Market");
        receipt.setPurchaseDate("2022-03-20");
        receipt.setPurchaseTime("14:33");
        receipt.setTotal("9.00");
        receipt.setItems(items);
        return receipt;
    }
}
