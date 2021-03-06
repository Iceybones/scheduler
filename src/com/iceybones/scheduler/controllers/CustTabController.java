package com.iceybones.scheduler.controllers;

import com.iceybones.scheduler.models.Country;
import com.iceybones.scheduler.models.Customer;
import com.iceybones.scheduler.models.Division;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

/**
 * Logic controller pertaining to the customer tab of the main scene.
 */
public class CustTabController implements Initializable {

    private MainController mainController;
    private ResourceBundle resourceBundle;

    /**
     * Sets up the scene GUI components utilizing the resource bundle for text values. A lambda
     * expression is used to implement the <code>ChangeListener</code> interface and provide extra
     * functionality for when users select items in the <code>custTableView</code>.
     *
     * @param url the url of the scene's fxml layout
     * @param rb  the currently loaded resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resourceBundle = rb;
        custTableView.getColumns().get(0).setText(rb.getString("ID"));
        custTableView.getColumns().get(1).setText(rb.getString("Name"));
        custTableView.getColumns().get(2).setText(rb.getString("Address"));
        custTableView.getColumns().get(3).setText(rb.getString("Postal Code"));
        custTableView.getColumns().get(4).setText(rb.getString("Division"));
        custTableView.getColumns().get(5).setText(rb.getString("Country"));
        custTableView.getColumns().get(6).setText(rb.getString("Phone Number"));
        custIdField.setPromptText(rb.getString("Auto-Generated"));
        custIdLbl.setText(rb.getString("ID") + ":");
        custNameLbl.setText(rb.getString("Name") + ":");
        custAddressLbl.setText(rb.getString("Address") + ":");
        custPostalCodeLbl.setText(rb.getString("Postal Code") + ":");
        custPhoneLbl.setText(rb.getString("Phone") + ":");
        addCustomerBtn.getTooltip().setText(rb.getString("Add New Customer"));
        editCustomerBtn.getTooltip().setText(rb.getString("Edit Selected Customer"));
        deleteCustomerBtn.getTooltip().setText(rb.getString("Remove Selected Customer"));
        addCustAppBtn.getTooltip().setText(rb.getString("Schedule Appointment with Selected Customer"));
        custRefreshBtn.getTooltip().setText(rb.getString("Refresh"));
        custConfirmBtn.getTooltip().setText(rb.getString("Confirm Submission"));
        setupTable();
        custTableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        deleteCustomerBtn.setDisable(false);
                        editCustomerBtn.setDisable(false);
                        addCustAppBtn.setDisable(false);
                        if (custToolDrawer.isExpanded()) {
                            if (editCustomerBtn.isSelected()) {
                                custConfirmBtn.setDisable(true);
                            }
                            openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
                        }
                        if (addCustomerBtn.isSelected()) {
                            addCustomerBtn.setSelected(false);
                            setCollapseToolDrawer(true);
                        }
                    }
                });
        String countryTxt = resourceBundle.getString("Country");
        countryComboBox.setPromptText(countryTxt);
        countryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Country item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(countryTxt);
                } else {
                    setText(item.getCountry());
                }
            }
        });
        String stateTxt = resourceBundle.getString("State");
        stateComboBox.setPromptText(stateTxt);
        stateComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Division item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(stateTxt);
                } else {
                    setText(item.getDivision());
                }
            }
        });
    }

    /**
     * Links this controller to it's parent controller.
     *
     * @param mainController the parent controller
     */
    void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Calls populate on both <code>customer</code> table and the <code>countryComboBox</code>.
     */
    void populate() {
        populateTable();
        populateCountryBox();
    }

    /**
     * Links columns of the <code>customer</code> table with corresponding <code>customer</code>
     * object values.
     */
    private void setupTable() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    /**
     * Gets all <code>customer</code> data from the Database class and uses it to populate the table
     * view. A lambda expression is used to implement the <code>Runnable</code> interface and submit a
     * new task to the database to fetch the customer records. Three more <code>Runnable</code>
     * lambdas are nested inside to update GUI components on the JavaFX thread. A notification is
     * displayed if the database request fails for any reason.
     */
    void populateTable() {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                List<Customer> customers = Database.getCustomers();
                custTableView.getItems().clear();
                custTableView.getItems().addAll(customers);
            } catch (SQLException e) {
                Platform.runLater(
                        () -> mainController.notify(
                                resourceBundle.getString("Failed to populate customer table. Check connection."),
                                MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    /**
     * Gets all <code>country</code> data from the Database class and uses it to populate the
     * <code>countryComboBox</code>. A lambda expression is used to implement the
     * <code>Runnable</code> interface and submit a new task to the database to fetch the country
     * records. Two more <code>Runnable</code> lambdas are nested inside to update GUI components on
     * the JavaFX thread. A notification is displayed if the database request fails for any reason.
     */
    private void populateCountryBox() {
        MainController.getDbService().submit(() -> {
            try {
                List<Country> countries = Database.getCountries();
                Platform.runLater(() -> countryComboBox.getItems().addAll(countries));
            } catch (SQLException e) {
                Platform.runLater(
                        () -> mainController.notify(
                                resourceBundle.getString("Failed to populate country box. Check connection."),
                                MainController.NotificationType.ERROR, false));
            }
        });
    }

    /**
     * Gets all <code>division</code> data from the Database class and uses it to populate the
     * <code>stateComboBox</code>. A lambda expression is used to implement the
     * <code>Runnable</code> interface and submit a new task to the database to fetch the division
     * records. Two more <code>Runnable</code> lambdas are nested inside to update GUI components on
     * the JavaFX thread. A notification is displayed if the database request fails for any reason.
     *
     * @param country used to select relevant states
     */
    private void populateStateBox(Country country) {
        stateComboBox.getItems().clear();
        MainController.getDbService().submit(() -> {
            try {
                List<Division> division = Database.getDivisionsByCountry(country);
                Platform.runLater(() -> stateComboBox.getItems().addAll(division));
            } catch (SQLException e) {
                Platform
                        .runLater(() -> mainController
                                .notify(resourceBundle.getString("Failed to populate state box. Check connection."),
                                        MainController.NotificationType.ERROR, false));
            }
        });
    }

    /**
     * Helper method used to open and closes the tool drawer.
     *
     * @param b if the tool drawer should be closed
     */
    void setCollapseToolDrawer(boolean b) {
        if (b) {
            custToolDrawer.setAnimated(true);
        }
        custToolDrawer.setCollapsible(true);
        custToolDrawer.setExpanded(!b);
        custToolDrawer.setCollapsible(false);
        custToolDrawer.setAnimated(false);
    }

    /**
     * Clears all the GUI components in the tool drawer.
     */
    private void clearToolDrawer() {
        custNameField.setText(null);
        custIdField.setText(null);
        custPhoneField.setText(null);
        custAddressField.setText(null);
        custPostalCodeField.setText(null);
        setValHelper(countryComboBox, null);
        setValHelper(stateComboBox, null);
        stateComboBox.setDisable(true);
    }

    /**
     * Resets the tool buttons to their default state.
     */
    void resetToolButtons() {
        editCustomerBtn.setDisable(true);
        deleteCustomerBtn.setDisable(true);
        addCustAppBtn.setDisable(true);
        addCustomerBtn.setSelected(false);
        editCustomerBtn.setSelected(false);
        deleteCustomerBtn.setSelected(false);
    }

    /**
     * Makes GUI input elements in the tool drawer editable or not.
     *
     * @param isEdit if the input elements should be set as editable
     */
    private void setToolDrawerEditable(boolean isEdit) {
        custNameField.setDisable(!isEdit);
        custPhoneField.setDisable(!isEdit);
        custAddressField.setDisable(!isEdit);
        custPostalCodeField.setDisable(!isEdit);
        countryComboBox.setDisable(!isEdit);
        stateComboBox.setDisable(!isEdit);
    }

    /**
     * Opens the tool drawer and populates the GUI elements with data from the provided
     * <code>customer</code>.
     *
     * @param cust the customer record that is to be opened
     */
    private void openToolDrawer(Customer cust) {
        setCollapseToolDrawer(false);
        if (cust == null) {
            clearToolDrawer();
        } else {
            custIdField.setText(Integer.toString(cust.getCustomerId()));
            custNameField.setText(cust.getCustomerName());
            custPhoneField.setText(cust.getPhone());
            custAddressField.setText(cust.getAddress());
            custPostalCodeField.setText(cust.getPostalCode());
            setValHelper(countryComboBox, cust.getDivision().getCountry());
            populateStateBox(cust.getDivision().getCountry());
            setValHelper(stateComboBox, cust.getDivision());
        }
        if (!editCustomerBtn.isSelected()) {
            tryActivateConfirmBtn();
        }
    }

    /**
     * Helper method used to implement a workaround that allows the caller to change the GUI subject's
     * value without having its <code>onAction</code> event fired.
     *
     * @param box the GUI element that will be operated on
     * @param val the new value that is to be applied to the subject
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setValHelper(ComboBoxBase box, Object val) {
        var handler = box.getOnAction();
        box.setOnAction(null);
        box.setValue(val);
        box.setOnAction(handler);
    }

    /**
     * Sends a request to the Database class to <code>INSERT</code> a new customer record. A lambda
     * expression is used to implement the <code>Runnable</code> interface and submit a new task to
     * the database to perform the operation. Three more <code>Runnable</code> lambdas are nested
     * inside to update GUI components on the JavaFX thread. A notification is displayed to report the
     * success or failure of the operation.
     *
     * @param customer the new <code>customer</code> that is to be added
     */
    private void confirmAdd(Customer customer) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                int custId = Database.insertCustomer(customer);
                customer.setCustomerId(custId);
                Platform.runLater(() -> {
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    populateTable();
                    mainController.getAppTabController().populateCustComboBox();
                    mainController.getAppTabController().resetToolButtons();
                    mainController.notify(resourceBundle.getString("Customer Added") + ": " + customer,
                            MainController.NotificationType.ADD, true);
                });
            } catch (SQLException e) {
                Platform.runLater(
                        () -> mainController.notify(
                                resourceBundle.getString("Failed to add customer. Check connection and input."),
                                MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    /**
     * Sends a request to the Database class to <code>DELETE</code> a customer record. A lambda
     * expression is used to implement the <code>Runnable</code> interface and submit a new task to
     * the database to perform the operation. Three more <code>Runnable</code> lambdas are nested
     * inside to update GUI components on the JavaFX thread. A notification is displayed to report the
     * success or failure of the operation.
     *
     * @param customer the <code>customer</code> that is to be deleted
     */
    private void confirmDelete(Customer customer) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                Database.deleteCustomer(customer);
                Platform.runLater(() -> {
                    clearToolDrawer();
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    populateTable();
                    mainController.getAppTabController().populateCustComboBox();
                    mainController.getAppTabController().populateTable();
                    mainController.getAppTabController().resetToolButtons();
                    mainController.getAppTabController().setCollapseToolDrawer(true);
                    mainController.notify(resourceBundle.getString("Customer Removed") + ": " + customer,
                            MainController.NotificationType.DELETE, true);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mainController
                        .notify(resourceBundle.getString("Failed to delete customer. Check connection."),
                                MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    /**
     * Sends a request to the Database class to <code>UPDATE</code> a customer record. A lambda
     * expression is used to implement the <code>Runnable</code> interface and submit a new task to
     * the database to perform the operation. Three more <code>Runnable</code> lambdas are nested
     * inside to update GUI components on the JavaFX thread. A notification is displayed to report the
     * success or failure of the operation.
     *
     * @param newCust  a <code>customer</code> object holding the data that is to be copied unto the
     *                 original
     * @param original the <code>customer</code> that is to be updated
     */
    private void confirmUpdate(Customer newCust, Customer original) {
        mainController.getTableProgress().setVisible(true);
        MainController.getDbService().submit(() -> {
            try {
                Database.updateCustomer(newCust);
                Platform.runLater(() -> {
                    clearToolDrawer();
                    setCollapseToolDrawer(true);
                    resetToolButtons();
                    mainController.getAppTabController().populateCustComboBox();
                    custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
                    mainController.notify(resourceBundle.getString("Customer Updated") + ": " + newCust,
                            MainController.NotificationType.EDIT, true);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> mainController
                        .notify(resourceBundle.getString("Failed to update customer. Check connection."),
                                MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
    }

    /**
     * Activates the <code>custConfirmBtn</code> if all the required values are present in the
     * toolbar's inputs.
     */
    private void tryActivateConfirmBtn() {
        custConfirmBtn
                .setDisable((custNameField.getText() == null || custNameField.getText().equals(""))
                        || (custPhoneField.getText() == null || custPhoneField.getText().equals(""))
                        || (custAddressField.getText() == null || custAddressField.getText().equals(""))
                        || (custPostalCodeField.getText() == null || custPostalCodeField.getText().equals(""))
                        || countryComboBox.getValue() == null || stateComboBox.getValue() == null);
    }

    ///////////////////Event Handlers/////////////////////

    /**
     * Gathers all the input data from the tool drawer elements and calls the relevant confirm method
     * when the <code>custConfirmBtn</code> is pressed.
     */
    @FXML
    private void onActionConfirm() {
        Customer customer = new Customer();
        customer.setCustomerName(custNameField.getText());
        customer.setCustomerId((custIdField.getText() == null ? 0
                : Integer.parseInt(custIdField.getText())));
        customer.setPhone(custPhoneField.getText());
        customer.setAddress(custAddressField.getText());
        customer.setPostalCode(custPostalCodeField.getText());
        customer.setCreatedBy(Database.getConnectedUser());
        customer.setLastUpdatedBy(Database.getConnectedUser());
        customer.setDivision(stateComboBox.getValue());
        if (addCustomerBtn.isSelected()) {
            confirmAdd(customer);
        } else if (deleteCustomerBtn.isSelected()) {
            confirmDelete(custTableView.getSelectionModel().getSelectedItem());
        } else if (editCustomerBtn.isSelected()) {
            confirmUpdate(customer, custTableView.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Open the tool drawer when the <code>addCustomerBtn</code> is pressed.
     */
    @FXML
    private void onActionAddCust() {
        custTableView.getSelectionModel().clearSelection();
        editCustomerBtn.setDisable(true);
        deleteCustomerBtn.setDisable(true);
        addCustAppBtn.setDisable(true);
        if (addCustomerBtn.isSelected()) {
            setToolDrawerEditable(true);
            custConfirmBtnImg.setImage(MainController.getAddImg());
            openToolDrawer(null);
        } else {
            setCollapseToolDrawer(true);
        }
    }

    /**
     * Open the tool drawer when the <code>deleteCustomerBtn</code> is pressed.
     */
    @FXML
    private void onActionDeleteCust() {
        if (deleteCustomerBtn.isSelected()) {
            setToolDrawerEditable(false);
            custConfirmBtnImg.setImage(MainController.getDeleteImg());
            openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true);
        }
    }

    /**
     * Open the tool drawer when the <code>editCustomerBtn</code> is pressed.
     */
    @FXML
    private void onActionEditCust() {
        if (editCustomerBtn.isSelected()) {
            setToolDrawerEditable(true);
            custConfirmBtnImg.setImage(MainController.getEditImg());
            custConfirmBtn.setDisable(true);
            openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true);
        }
    }

    /**
     * Opens the appointment tab with the add new appointment button selected and the tool drawer
     * pre-populated with the currently selected customer.
     */
    @FXML
    private void onActionAddCustApp() {
        setCollapseToolDrawer(true);
        resetToolButtons();
        mainController.getTabPane().getSelectionModel().select(0);
        mainController.getAppTabController().pushAppointment(custTableView.getSelectionModel()
                .getSelectedItem());
        custTableView.getSelectionModel().clearSelection();
    }

    /**
     * When the <code>countryComboBox</code> is selected, populates the <code>stateComboBox</code>
     * with the states corresponding to the selected country.
     */
    @FXML
    private void onActionCountryComboBox() {
        setValHelper(stateComboBox, null);
        stateComboBox.setDisable(false);
        if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
            populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
        }
        tryActivateConfirmBtn();
    }

    /**
     * Attempts to activate the <code>custConfirmBtn</code> when a selection is made in the
     * <code>stateComboBox</code>.
     */
    @FXML
    private void onActionStateComboBox() {
        tryActivateConfirmBtn();
    }

    /**
     * Makes a request to the Database class to perform a <code>COMMIT</code> on the database. This
     * deselects any selected items and closes the tool drawer. A lambda expression is used to
     * implement the <code>Runnable</code> interface and submit a new task to the database to perform
     * the <code>COMMIT</code>. Three more <code>Runnable</code> lambdas are nested inside to update
     * GUI components on the JavaFX thread. A notification is displayed to report the success or
     * failure of this request.
     */
    @FXML
    private void onActionRefresh() {
        mainController.getTableProgress().setVisible(true);
        custTableView.getSelectionModel().clearSelection();
        setCollapseToolDrawer(true);
        resetToolButtons();
        MainController.getDbService().submit(() -> {
            try {
                Database.commit();
                Platform.runLater(() -> mainController.notify(resourceBundle.getString("Changes Committed"),
                        MainController.NotificationType.SUCCESS, false)
                );
            } catch (SQLException e) {
                Platform.runLater(() -> mainController
                        .notify(resourceBundle.getString("Failed to refresh database. Check connection."),
                                MainController.NotificationType.ERROR, false));
            } finally {
                Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
            }
        });
        mainController.disableUndo();
    }

    /**
     * Tries to activate the <code>custConfirmBtn</code> whenever a key is typed in any of the toolbar
     * text fields.
     */
    @FXML
    private void onKeyTypedCustField() {
        tryActivateConfirmBtn();
    }

    ///////////////////////GUI Components/////////////////////
    @FXML
    private ImageView custConfirmBtnImg;

    @FXML
    private ToggleButton addCustomerBtn;

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

    @FXML
    private Label custIdLbl;

    @FXML
    private Label custNameLbl;

    @FXML
    private Label custPhoneLbl;

    @FXML
    private Label custPostalCodeLbl;

    @FXML
    private Label custAddressLbl;

}
