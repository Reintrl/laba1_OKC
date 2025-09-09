package org.example;

import com.fazecast.jSerialComm.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialCommunicator {
    private SerialPort serialPort;
    private boolean portOpen = false;
    private ConcurrentLinkedQueue<Byte> sendQueue = new ConcurrentLinkedQueue<>();
    private ExecutorService sendExecutor;
    private AtomicBoolean sending = new AtomicBoolean(false);

    private int baudRate = 9600;
    private int dataBits = 8;
    private int stopBits = SerialPort.ONE_STOP_BIT;
    private int parity = SerialPort.NO_PARITY;

    public boolean openPort(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        portOpen = serialPort.openPort();
        if (portOpen) {
            sendExecutor = Executors.newSingleThreadExecutor();
        }
        return portOpen;
    }

    public void closePort() {
        if (serialPort != null && portOpen) {
            if (sendExecutor != null) {
                sendExecutor.shutdown();
                sendExecutor = null;
            }
            serialPort.closePort();
            portOpen = false;
            sendQueue.clear();
            sending.set(false);
        }
    }

    public boolean sendByte(byte data) {
        if (!portOpen) return false;
        sendQueue.offer(data);
        startAsyncSend();
        return true;
    }

    private void startAsyncSend() {
        if (sending.compareAndSet(false, true)) {
            sendExecutor.execute(this::processSendQueue);
        }
    }

    private void processSendQueue() {
        try {
            while (!sendQueue.isEmpty() && portOpen) {
                Byte data = sendQueue.poll();
                if (data != null) {
                    byte[] buffer = new byte[]{data};
                    serialPort.writeBytes(buffer, 1);
                }
            }
        } finally {
            sending.set(false);
            if (!sendQueue.isEmpty()) {
                startAsyncSend();
            }
        }
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

    public boolean isPortOpen() { return portOpen; }

    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];

        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }

        return portNames;
    }
}