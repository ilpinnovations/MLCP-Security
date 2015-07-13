package com.ilp.innovations.myapplication.Beans;


public class Slot {

    private int bookId;
    private String slotId;
    private String empId;
    private String regId;
    private String slot;
    private int flag;
    private boolean isChecked;
    private boolean isReserved;
    private boolean isAllocated;

    public Slot() {
    }

    public Slot(int bookId, String regId, String slot) {
        this.bookId = bookId;
        this.regId = regId;
        this.slot = slot;
        this.isChecked = false;
    }

    public int getBookId() {
        return bookId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setIsReserved(boolean isReserved) {
        this.isReserved = isReserved;
    }

    public boolean isAllocated() {
        return isAllocated;
    }

    public void setIsAllocated(boolean isAllocated) {
        this.isAllocated = isAllocated;
    }
}
