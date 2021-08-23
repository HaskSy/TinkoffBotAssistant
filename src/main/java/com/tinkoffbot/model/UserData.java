package com.tinkoffbot.model;

public class UserData {

    private String Name;
    private int KK;
    private int DK;
    private int TI;
    private int SIM;
    private int MNP;
    private int VS;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getKK() {
        return KK;
    }

    public void setKK(int KK) {
        this.KK = KK;
    }

    public int getDK() {
        return DK;
    }

    public void setDK(int DK) {
        this.DK = DK;
    }

    public int getTI() {
        return TI;
    }

    public void setTI(int TI) {
        this.TI = TI;
    }

    public int getSIM() {
        return SIM;
    }

    public void setSIM(int SIM) {
        this.SIM = SIM;
    }

    public int getMNP() {
        return MNP;
    }

    public void setMNP(int MNP) {
        this.MNP = MNP;
    }

    public int getVS() {
        return VS;
    }

    public void setVS(int VS) {
        this.VS = VS;
    }

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
