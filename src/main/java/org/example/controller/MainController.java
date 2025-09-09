package org.example.controller;

import com.fazecast.jSerialComm.SerialPort;
import org.example.SerialCommunicator;
import org.example.util.ByteConverter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.Timer;
import java.util.TimerTask;

public class MainController {
    @FXML private ComboBox<String> portSelector;
    @FXML private ComboBox<Integer> baudRateSelector;
    @FXML private ComboBox<Integer> dataBitsSelector;
    @FXML private ComboBox<String> stopBitsSelector;
    @FXML private ComboBox<String> paritySelector;
    @FXML private Button openCloseButton;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private TextArea logArea;
    @FXML private CheckBox autoScrollCheck;
    @FXML private CheckBox hexDisplayCheck;
    @FXML private CheckBox binaryDisplayCheck;

    private SerialCommunicator communicator;
    private Timer receiveTimer;
    private boolean receiving = false;

    private final ObservableList<Integer> baudRates = FXCollections.observableArrayList(
            110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000
    );

    private final ObservableList<Integer> dataBitsList = FXCollections.observableArrayList(
            5, 6, 7, 8
    );

    private final ObservableList<String> stopBitsList = FXCollections.observableArrayList(
            "1", "1.5", "2"
    );

    private final ObservableList<String> parityList = FXCollections.observableArrayList(
            "None", "Even", "Odd", "Mark", "Space"
    );

    @FXML
    public void initialize() {
        baudRateSelector.setItems(baudRates);
        baudRateSelector.getSelectionModel().select(6);

        dataBitsSelector.setItems(dataBitsList);
        dataBitsSelector.getSelectionModel().select(3);

        stopBitsSelector.setItems(stopBitsList);
        stopBitsSelector.getSelectionModel().select(0);

        paritySelector.setItems(parityList);
        paritySelector.getSelectionModel().select(0);

        refreshPortList();

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendByte();
            }
        });

        communicator = new SerialCommunicator();
    }

    private void refreshPortList() {
        String[] ports = SerialCommunicator.getAvailablePorts();
        portSelector.getItems().setAll(ports);

        if (ports.length > 0) {
            portSelector.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleOpenClose() {
        if (communicator.isPortOpen()) {
            closePort();
        } else {
            openPort();
        }
    }

    private void openPort() {
        String port = portSelector.getValue();
        if (port == null || port.isEmpty()) {
            log("Please select a port");
            return;
        }

        int baudRate = baudRateSelector.getValue();
        int dataBits = dataBitsSelector.getValue();

        int stopBits;
        switch (stopBitsSelector.getValue()) {
            case "1.5": stopBits = SerialPort.ONE_POINT_FIVE_STOP_BITS; break;
            case "2": stopBits = SerialPort.TWO_STOP_BITS; break;
            default: stopBits = SerialPort.ONE_STOP_BIT; break;
        }

        int parity;
        switch (paritySelector.getValue()) {
            case "Even": parity = SerialPort.EVEN_PARITY; break;
            case "Odd": parity = SerialPort.ODD_PARITY; break;
            case "Mark": parity = SerialPort.MARK_PARITY; break;
            case "Space": parity = SerialPort.SPACE_PARITY; break;
            default: parity = SerialPort.NO_PARITY; break;
        }

        communicator.setPortParameters(baudRate, dataBits, stopBits, parity);

        if (communicator.openPort(port)) {
            log("Port " + port + " opened successfully");
            openCloseButton.setText("Close Port");
            startReceiving();

            portSelector.setDisable(true);
            baudRateSelector.setDisable(true);
            dataBitsSelector.setDisable(true);
            stopBitsSelector.setDisable(true);
            paritySelector.setDisable(true);
        } else {
            log("Failed to open port " + port);
        }
    }

    private void closePort() {
        stopReceiving();
        communicator.closePort();
        log("Port closed");
        openCloseButton.setText("Open Port");

        portSelector.setDisable(false);
        baudRateSelector.setDisable(false);
        dataBitsSelector.setDisable(false);
        stopBitsSelector.setDisable(false);
        paritySelector.setDisable(false);
    }

    private void startReceiving() {
        if (receiving) return;

        receiving = true;
        receiveTimer = new Timer(true);
        receiveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Byte receivedByte = communicator.receiveByte();
                if (receivedByte != null) {
                    Platform.runLater(() -> handleReceivedByte(receivedByte));
                }
            }
        }, 0, 10);
    }

    private void stopReceiving() {
        receiving = false;
        if (receiveTimer != null) {
            receiveTimer.cancel();
            receiveTimer = null;
        }
    }

    private void handleReceivedByte(byte data) {
        String display;

        if (binaryDisplayCheck.isSelected()) {
            display = "BIN: " + ByteConverter.byteToBinaryString(data);
        } else if (hexDisplayCheck.isSelected()) {
            display = "HEX: " + ByteConverter.byteToHexString(data);
        } else {
            display = "DEC: " + ByteConverter.byteToString(data) +
                    " (CHAR: '" + (char) data + "')";
        }

        log("Received: " + display);
    }

    @FXML
    private void sendByte() {
        if (!communicator.isPortOpen()) {
            log("Port is not open");
            return;
        }

        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            log("Please enter a byte to send");
            return;
        }

        try {
            byte data = ByteConverter.stringToByte(input);
            if (communicator.sendByte(data)) {
                String display;

                if (binaryDisplayCheck.isSelected()) {
                    display = "BIN: " + ByteConverter.byteToBinaryString(data);
                } else if (hexDisplayCheck.isSelected()) {
                    display = "HEX: " + ByteConverter.byteToHexString(data);
                } else {
                    display = "DEC: " + ByteConverter.byteToString(data) +
                            " (CHAR: '" + (char) data + "')";
                }

                log("Sent: " + display);
                inputField.clear();
            } else {
                log("Failed to send byte");
            }
        } catch (NumberFormatException e) {
            log("Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void refreshPorts() {
        refreshPortList();
        log("Port list refreshed");
    }

    private void log(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");

        if (autoScrollCheck.isSelected()) {
            logArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    @FXML
    private void clearLog() {
        logArea.clear();
    }

    public void shutdown() {
        if (communicator != null && communicator.isPortOpen()) {
            communicator.closePort();
        }
        stopReceiving();
    }
}