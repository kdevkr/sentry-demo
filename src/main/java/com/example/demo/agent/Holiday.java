package com.example.demo.agent;

import lombok.Data;

@Data
public class Holiday {
    private int seq;
    private String dateKind;
    private String dateName;
    private String locdate;
    private String isHoliday;
}
