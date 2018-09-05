package com.blackstone.dailyresearchweb.websocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.websocket.Session;

public class ProcessHelper {
    private BufferedReader reader;
    private BufferedWriter writer;
    private Process process;
    private CallBackWriteThread callBackWriteThread;


    public ProcessHelper(String cmd, Session session) {
        try {
            init(cmd, session);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init(String cmd, Session session) throws UnsupportedEncodingException {
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
        process = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();
        reader = new BufferedReader(new InputStreamReader(stdout, "GBK"));
        writer = new BufferedWriter(new OutputStreamWriter(stdin));

        callBackWriteThread = new CallBackWriteThread(reader, session);
        callBackWriteThread.start();
    }

    public void sendMsg(String msg) throws IOException {
        writer.write(msg + "\n");
        writer.flush();
    }

    public void close() {
        if (process != null) {
            callBackWriteThread.setRunning(false);
            process.destroy();
        }
    }

    public void sendMessage(Session session, String message) throws IOException {
        System.out.println("发送消息:[" + Thread.currentThread().getId() + "]+[session:" + session.getId() + "]" + message);
        session.getBasicRemote().sendText(message);
    }

    class CallBackWriteThread extends Thread {
        private boolean running = true;
        private BufferedReader reader;
        private Session session;

        public CallBackWriteThread(BufferedReader reader, Session session) {
            this.session = session;
            this.reader = reader;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            String line;
            try {
                while (running && (line = reader.readLine()) != null) {
                    if (line.length() > 0) {
                        System.out.println("ServerStdout: " + line);
                        sendMessage(session, line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
