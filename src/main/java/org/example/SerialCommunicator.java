package org.example;

import com.fazecast.jSerialComm.*;

public class SerialCommunicator {
    private SerialPort serialPort;
    private boolean portOpen = false;

    private int baudRate = 9600;
    private int dataBits = 8;
    private int stopBits = SerialPort.ONE_STOP_BIT;
    private int parity = SerialPort.NO_PARITY;

    public SerialCommunicator() {}

    public SerialCommunicator(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public boolean openPort(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        portOpen = serialPort.openPort();
        return portOpen;
    }

    public void closePort() {
        if (serialPort != null && portOpen) {
            serialPort.closePort();
            portOpen = false;
        }
    }

    public boolean sendByte(byte data) {
        if (!portOpen) return false;

        byte[] buffer = new byte[]{data};
        int bytesWritten = serialPort.writeBytes(buffer, 1);
        return bytesWritten == 1;
    }

    public Byte receiveByte() {
        if (!portOpen) return null;

        byte[] buffer = new byte[1];
        int bytesRead = serialPort.readBytes(buffer, 1);

        if (bytesRead == 1) {
            return buffer[0];
        }
        return null;
    }

    public void setPortParameters(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;

        if (portOpen) {
            serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        }
    }

    public int getBaudRate() { return baudRate; }
    public int getDataBits() { return dataBits; }
    public int getStopBits() { return stopBits; }
    public int getParity() { return parity; }
    public boolean isPortOpen() { return portOpen; }
    public String getPortName() {
        return serialPort != null ? serialPort.getSystemPortName() : "";
    }

    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];

        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }

        return portNames;
    }
}