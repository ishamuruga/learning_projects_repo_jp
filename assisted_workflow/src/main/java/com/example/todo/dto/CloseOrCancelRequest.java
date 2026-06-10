package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;

public class CloseOrCancelRequest {

    @NotBlank(message = "Remarks are required when closing or cancelling a todo")
    private String remarks;

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
