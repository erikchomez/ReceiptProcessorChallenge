package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class Receipt {
    private String retailer;
    private String purchaseDate;
    private String purchaseTime;
    private String total;
    private List<Item> items;
}
