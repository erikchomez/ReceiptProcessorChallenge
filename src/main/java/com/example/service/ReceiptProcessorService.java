package com.example.service;

import com.example.model.Receipt;

public interface ReceiptProcessorService {
    String processPoints(Receipt receipt);

    int getPoints(String id);
}
