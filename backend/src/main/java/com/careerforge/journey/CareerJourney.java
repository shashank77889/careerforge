package com.careerforge.journey;

import jakarta.persistence.*;

@Entity
@Table(name = "career_journeys")
public class CareerJourney {
    @Id public Long userId;
    @Column(nullable = false) public String targetRole = "Data Scientist";
    @Column(nullable = false) public int weeklyHours = 5;
    @Column(nullable = false, columnDefinition = "varchar(120) default 'MONDAY,WEDNESDAY,SATURDAY'") public String studyDays = "MONDAY,WEDNESDAY,SATURDAY";
    @Column(nullable = false, columnDefinition = "varchar(5) default '19:00'") public String studyTime = "19:00";
    @Column(nullable = false, columnDefinition = "int default 60") public int sessionMinutes = 60;
    @Column(nullable = false, columnDefinition = "varchar(1000) default ''") public String focusSkills = "";
    @Column(nullable = false, columnDefinition = "varchar(255) default '/dashboard'") public String lastPage = "/dashboard";
    @Version public Long version;
}


