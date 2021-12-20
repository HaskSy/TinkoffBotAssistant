package com.tinkoffbot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {

    private String Name;
    private int KK;
    private int DK;
    private int TI;
    private int SIM;
    private int MNP;
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
