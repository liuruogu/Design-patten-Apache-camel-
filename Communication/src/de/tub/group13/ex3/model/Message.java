package de.tub.group13.ex3.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String customerID;
    private String firstName;
    private String lastName;
    private Integer overallItems;
    private Integer numberOfDivingSuits;
    private Integer numberOfSurfboards;
    private Integer orderID;
    private Boolean valid;
    private String validationResult;

    public int getNumberOfSurfboards() {
        return numberOfSurfboards;
    }

    public void setNumberOfSurfboards(int numberOfSurfboards) {
        this.numberOfSurfboards = numberOfSurfboards;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getOverallItems() {
        return overallItems;
    }

    public void setOverallItems(int overallItems) {
        this.overallItems = overallItems;
    }

    public int getNumberOfDivingSuits() {
        return numberOfDivingSuits;
    }

    public void setNumberOfDivingSuits(int numberOfDivingSuits) {
        this.numberOfDivingSuits = numberOfDivingSuits;
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("customerID='").append(customerID).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", overallItems=").append(overallItems);
        sb.append(", numberOfDivingSuits=").append(numberOfDivingSuits);
        sb.append(", numberOfSurfboards=").append(numberOfSurfboards);
        sb.append(", orderID=").append(orderID);
        sb.append(", valid=").append(valid);
        sb.append(", validationResult='").append(validationResult).append('\'');
        sb.append('}');
        return sb.toString();
    }
}