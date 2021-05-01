package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.Country;
import com.iceybones.scheduler.application.model.Customer;
import com.iceybones.scheduler.application.model.Division;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CustTabController implements Initializable {
    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /////////////////////////// Common Methods /////////////////////////////////
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        custTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteCustomerBtn.setDisable(false);
                editCustomerBtn.setDisable(false);
                addCustAppBtn.setDisable(false);
                if (oldSelection != null && custToolDrawer.isExpanded()) {
                    openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
                }
                if (addCustomerBtn.isSelected()) {
                    addCustomerBtn.setSelected(false);
                    setCollapseToolDrawer(true);
                }
            }
        });
    }

    public void populate() {
        populateTable();
        populateCountryBox();
    }

    void tryActivateConfirmBtn() {
        custConfirmBtn.setDisable(custNameField.getText().equals("") || custPhoneField.getText().equals("") ||
                custAddressField.getText().equals("") || custPostalCodeField.getText().equals("") ||
                countryComboBox.getSelectionModel().isEmpty() || stateComboBox.getSelectionModel().isEmpty() &&
                !deleteCustomerBtn.isSelected());
    }

    private void setupTable() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    public void populateTable() {
        System.out.println("Populate table called");
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                List<Customer> customers = Database.getCustomers();
                custTableView.getItems().clear();
                custTableView.getItems().addAll(customers);
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to populate customer table. Check connection.",
                        MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    void setCollapseToolDrawer(boolean b) {
        custToolDrawer.setCollapsible(true);
        custToolDrawer.setExpanded(!b);
        custToolDrawer.setCollapsible(false);
    }

    private void clearToolDrawer() {
        custNameField.setText("");
        custIdField.setText("Auto-Generated");
        custPhoneField.setText("");
        custAddressField.setText("");
        custPostalCodeField.setText("");
        countryComboBox.getSelectionModel().clearSelection();
        stateComboBox.getSelectionModel().clearSelection();
        stateComboBox.setDisable(true);
        countryComboBox.setPromptText("Country");
        countryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Country item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Country");
                } else {
                    setText(item.getCountry());
                }
            }
        });
        stateComboBox.setPromptText("State");
        stateComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Division item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("State");
                } else {
                    setText(item.getDivision());
                }
            }
        });
    }

    @FXML
    void onActionRefresh(ActionEvent event) {
        mainController.getTableProgress().setVisible(true);
        custTableView.getSelectionModel().clearSelection();
        setCollapseToolDrawer(true);
        resetToolButtons();
        MainController.getDbService().submit(() -> {
            try {
//                populateTable();
//                custTableView.getSelectionModel().clearSelection();
                Database.commit();
                Platform.runLater(() -> mainController.notify("Changes have been committed.",
                        MainController.NotificationType.SUCCESS, false)
                );
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to refresh database. Check connection.",
                        MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
        mainController.refresh();
    }

    void resetToolButtons() {
        editCustomerBtn.setDisable(true);
        deleteCustomerBtn.setDisable(true);
        addCustAppBtn.setDisable(true);
        addCustomerBtn.setSelected(false);
        editCustomerBtn.setSelected(false);
        deleteCustomerBtn.setSelected(false);
    }

    void setToolDrawerEditable(boolean isEdit) {
        custNameField.setEditable(isEdit);
        custPhoneField.setEditable(isEdit);
        custAddressField.setEditable(isEdit);
        custPostalCodeField.setEditable(isEdit);
        countryComboBox.setDisable(!isEdit);
    }

    ////////////////////////////////// Customer Tab Methods //////////////////////////////////////////

    void populateCountryBox() {
        MainController.getDbService().submit(() -> {
            try {
                countryComboBox.getItems().addAll(Database.getCountries());
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to populate country box. Check connection.",
                        MainController.NotificationType.ERROR, false));
            }
        });
    }

    void populateStateBox(Country country) {
        stateComboBox.getItems().clear();
        MainController.getDbService().submit(() -> {
            try {
                stateComboBox.getItems().addAll(Database.getDivisionsByCountry(country));
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to populate state box. Check connection.",
                        MainController.NotificationType.ERROR, false));
            }
        });
    }

    void openCustToolDrawer(Customer cust) {
        setCollapseToolDrawer(false);
        if (cust == null) {
            clearToolDrawer();
        } else {
            custIdField.setText(Integer.toString(cust.getCustomerId()));
            custNameField.setText(cust.getCustomerName());
            custPhoneField.setText(cust.getPhone());
            custAddressField.setText(cust.getAddress());
            custPostalCodeField.setText(cust.getPostalCode());
            var handler = countryComboBox.getOnAction();
            countryComboBox.setOnAction(null);
            countryComboBox.getSelectionModel().select(cust.getDivision().getCountry());
            countryComboBox.setOnAction(handler);
            populateStateBox(cust.getDivision().getCountry());
            var handler2 = stateComboBox.getOnAction();
            stateComboBox.setOnAction(null);
            stateComboBox.getSelectionModel().select(cust.getDivision());
            stateComboBox.setOnAction(handler2);

        }
    }

    private void confirmAddCustomer(Customer customer) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                Database.insertCustomer(customer);
                Platform.runLater(() -> {
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    populateTable();
                    mainController.notify(customer.getCustomerName() +
                            " has been added to the database.", MainController.NotificationType.ADD, true);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to add customer. Check connection and input.",
                        MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    private void confirmDeleteCustomer(Customer customer) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                Database.deleteCustomer(customer);
                Platform.runLater(() -> {
                    clearToolDrawer();
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    custTableView.getItems().remove(customer);
                    mainController.notify(customer.getCustomerName() + " has been removed from the database.",
                            MainController.NotificationType.DELETE, true);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to delete customer. Check connection.",
                        MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    private void confirmUpdateCustomer(Customer newCust, Customer original) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                Database.updateCustomer(newCust);
                Platform.runLater(() -> {
                    clearToolDrawer();
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
                    mainController.notify("Customer " + original.getCustomerId() +  " has been updated.",
                            MainController.NotificationType.EDIT, true);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> mainController.notify("Failed to update customer. Check connection.",
                        MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    @FXML
    void onActionConfirm(ActionEvent event) {
            Customer customer = new Customer();
            customer.setCustomerName(custNameField.getText());
            customer.setCustomerId((custIdField.getText().equals("Auto-Generated") ? 0 : Integer.parseInt(custIdField.getText())));
            customer.setPhone(custPhoneField.getText());
            customer.setAddress(custAddressField.getText());
            customer.setPostalCode(custPostalCodeField.getText());
            customer.setCreatedBy(Database.getConnectedUser());
            customer.setLastUpdatedBy(Database.getConnectedUser());
            customer.setDivision(stateComboBox.getValue());
            if (addCustomerBtn.isSelected()) {
                confirmAddCustomer(customer);
            } else if (deleteCustomerBtn.isSelected()) {
                confirmDeleteCustomer(custTableView.getSelectionModel().getSelectedItem());
            } else if (editCustomerBtn.isSelected()) {
                confirmUpdateCustomer(customer, custTableView.getSelectionModel().getSelectedItem());
            }
    }

    @FXML
    void onActionAddCust(ActionEvent event) {
        custTableView.getSelectionModel().clearSelection();
        editCustomerBtn.setDisable(true);
        deleteCustomerBtn.setDisable(true);
        addCustAppBtn.setDisable(true);
        if (addCustomerBtn.isSelected()) {
            setToolDrawerEditable(true);
            custConfirmBtnImg.setImage(MainController.getAddImg());
            openCustToolDrawer(null);
        } else {
            setCollapseToolDrawer(true);
        }
    }

    @FXML
    void onActionDeleteCust(ActionEvent event) {
        if (deleteCustomerBtn.isSelected()) {
            setToolDrawerEditable(false);
            custConfirmBtnImg.setImage(MainController.getDeleteImg());
            custConfirmBtn.setDisable(false);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true);
        }
    }

    @FXML
    void onActionEditCust(ActionEvent event) {
        if (editCustomerBtn.isSelected()) {
            setToolDrawerEditable(true);
            custConfirmBtnImg.setImage(MainController.getEditImg());
            custConfirmBtn.setDisable(true);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true);
        }
    }

    @FXML
    void onActionAddCustApp(ActionEvent event) {
        custTab.getTabPane().getSelectionModel().select(0);
        setCollapseToolDrawer(false);
        //TODO THIS NEEDS FIXED
//        custTab.getTabPane().getTabs().get(0).
//        appCustComboBox.getSelectionModel().select(custTableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    void onActionCountryComboBox(ActionEvent event) {
        stateComboBox.getItems().clear();
        stateComboBox.setDisable(false);
        if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
            populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
        }
        tryActivateConfirmBtn();
    }

    @FXML
    void onActionStateComboBox(ActionEvent event) {
        tryActivateConfirmBtn();
    }

    @FXML
    void onKeyTypedCustField(KeyEvent event) {
        tryActivateConfirmBtn();
    }

    @FXML
    private ImageView custConfirmBtnImg;

    @FXML
    private Tab custTab;

    @FXML
    private ToggleButton addCustomerBtn;

    @FXML
    private ToggleGroup custToggleGroup;

    @FXML
    private ToggleButton deleteCustomerBtn;

    @FXML
    private ToggleButton editCustomerBtn;

    @FXML
    private Button addCustAppBtn;

    @FXML
    private Button custRefreshBtn;

    @FXML
    private TitledPane custToolDrawer;

    @FXML
    private TextField custNameField;

    @FXML
    private TextField custAddressField;

    @FXML
    private TextField custIdField;

    @FXML
    private TextField custPostalCodeField;

    @FXML
    private TextField custPhoneField;

    @FXML
    private ComboBox<Country> countryComboBox;

    @FXML
    private ComboBox<Division> stateComboBox;

    @FXML
    private Button custConfirmBtn;


    @FXML
    private TableView<Customer> custTableView;

    @FXML
    private TableColumn<Customer, Integer> custIdCol;

    @FXML
    private TableColumn<Customer, String> custNameCol;

    @FXML
    private TableColumn<Customer, String> custAddressCol;

    @FXML
    private TableColumn<Customer, String> custPostalCodeCol;

    @FXML
    private TableColumn<Customer, String> custDivisionCol;

    @FXML
    private TableColumn<Customer, String> custCountryCol;

    @FXML
    private TableColumn<Customer, String> custPhoneCol;

}