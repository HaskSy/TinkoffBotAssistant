package com.tinkoffbot.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data parsed from report-type messages")
public class ReportData {
    @Schema(description = "First Name + Second Name + Last Name of agent") // clarify how to call представитель in english
    private String fsl;
    @Schema(description = "Unique ID of activity")
    private String activityId;
    @Schema(description = "Agent's question")
    private String question;
}
