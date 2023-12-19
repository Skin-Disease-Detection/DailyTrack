package com.example.dailytrack;

import com.google.firebase.firestore.PropertyName;
public class AttendanceRecord {

    private String employeeId;
    private String attendaceTime; // Change the field name to match Firestore
    private String checkInTime;
    private String checkOutTime;
    private String date;
    private String location;
    private String status;
    private  String name,workhour,OutTime;

    public AttendanceRecord() {
        // Required empty constructor for Firestore
    }

    public AttendanceRecord(String employeeId, String attendaceTime, String checkInTime, String checkOutTime, String date, String location,String status,String name,String workhour,String OutTime) {
        this.employeeId = employeeId;
        this.attendaceTime = attendaceTime;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.date = date;
        this.location = location;
        this.status=status;
        this.name =name;
        this.workhour=workhour;
        this.OutTime=OutTime;
    }
    public AttendanceRecord(String employeeId, String location) {
        this.employeeId = employeeId;
        this.location = location;
    }


    public String getEmployeeId() {
        return employeeId;
    }
    public String getWorkhour() {
        return workhour;
    }
    public void setOutTime(String OutTime) {
        this.OutTime = OutTime;
    }
    public void setWorkhour(String workhour) {
        this.workhour = workhour;
    }
    public String getOutTime() {
        return OutTime;
    }




    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    @PropertyName("AttendaceTime")
    public String getAttendaceTime() {
        return attendaceTime;
    }

    @PropertyName("AttendaceTime")
    public void setAttendaceTime(String attendaceTime) {
        this.attendaceTime = attendaceTime;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getName(){
        return name;
    }
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
    public void setName(String name )
    {
        this.name=name;
    }
    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
