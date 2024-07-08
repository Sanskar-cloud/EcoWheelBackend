package com.example.demo.payloads;

import lombok.Data;

import java.util.List;
@Data

public class CycleResponse {
    private List<CycleDto> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;
}
