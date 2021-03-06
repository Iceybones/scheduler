package com.iceybones.scheduler.models;

import java.util.Objects;

/**
 * Used to store data pertaining to an individual.
 */
public class Customer implements Comparable<Customer> {

  private int customerId;
  private String customerName;
  private String address;
  private String postalCode;
  private String phone;
  private Division division;
  private User createdBy;
  private User lastUpdatedBy;

  /**
   * Constructs a new blank <code>Customer</code> object
   */
  public Customer() {
    super();
  }

  public int getCustomerId() {
    return customerId;
  }

  public void setCustomerId(int customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public Division getDivision() {
    return division;
  }

  @SuppressWarnings("unused")
  public Country getCountry() {
    return division.getCountry();
  }

  public void setDivision(Division division) {
    this.division = division;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public User getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  @Override
  public int compareTo(Customer other) {
    return this.getCustomerId() - other.getCustomerId();
  }

  @Override
  public String toString() {
    return customerId + ": " + customerName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Customer)) {
      return false;
    }
    Customer customer = (Customer) o;
    return customerId == customer.customerId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId);
  }
}
