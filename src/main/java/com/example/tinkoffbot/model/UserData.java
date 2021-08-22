package com.example.tinkoffbot.model;

public class UserData {

    private String Name;
    private int KK;
    private int DK;
    private int TI;
    private int SIM;
    private int MNP;

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

    @Override
    public String toString() {
        return Name + " " + KK + " " + DK + " " + TI + " " + SIM + " " + MNP;
    }
}
