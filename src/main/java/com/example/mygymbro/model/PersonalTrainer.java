package com.example.mygymbro.model;

import java.util.ArrayList;
import java.util.List;


public class PersonalTrainer extends User {

    private List<Athlete> athleteList = new ArrayList<>();
    String certCode;

    public PersonalTrainer(int id, String username, String password, String name, String cognome, String email, String certCode) {
        super(id, username, password, name, cognome, email);
        this.certCode = certCode;
    }


    public void addAthlete(Athlete athlete){
        athleteList.add(athlete);

    }
    public void removeAthlete(Athlete athlete){
        athleteList.remove(athlete);
    }

    public List<Athlete> getAthleteList() {
        return athleteList;
    }

    public String getCertCode() {
        return certCode;
    }

    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

}
