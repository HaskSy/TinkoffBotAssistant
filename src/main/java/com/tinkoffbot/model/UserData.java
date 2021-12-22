package com.tinkoffbot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data parsed from stats-type messages")
public class UserData {

    @Schema(description = "First Name + Surname + Last Name")
    private String Name;
    @Schema(description = "Count of granted credit cards")
    private int KK;
    @Schema(description = "Count of granted debit cards")
    private int DK;
    @Schema(description = "Count of opened investment accounts")
    private int TI;
    @Schema(description = "Count of granted SIM cards")
    private int SIM;
    @Schema(description = "Count of phone number transfers")
    private int MNP;
    @Schema(description = "Count of client meetings")
    private int VS;

    @Override
    public String toString() {
        return Name.concat(" ")
                .concat(Integer.toString(KK)).concat(" ")
                .concat(Integer.toString(DK)).concat(" ")
                .concat(Integer.toString(TI)).concat(" ")
                .concat(Integer.toString(SIM)).concat(" ")
                .concat(Integer.toString(MNP)).concat(" ")
                .concat(Integer.toString(VS));
    }

}
