package com.blackstone.dailyresearchweb.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class ProcessDemo {
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("请输入命令:");
        String cmd = scan.nextLine();
        new ShellRunner(cmd).start();
    }

    static class ShellRunner extends Thread {
        private String cmd;

        public ShellRunner(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void run() {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            Process process = null;
            try {
                process = builder.start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            final OutputStream stdin = process.getOutputStream();
            final InputStream stdout = process.getInputStream();

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stdout, "GBK"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            OutputThread outputThread = new OutputThread(reader);
            outputThread.start();
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }

                Scanner scan = new Scanner(System.in);
                System.out.print("<<: ");
                String input = scan.nextLine();
                try {
                    writer.write(input + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if ("exit".equals(input)) {
                    outputThread.setRunning(false);
                    break;
                }
            }
        }
    }

    static class OutputThread extends Thread {
        private boolean running = true;
        private BufferedReader reader;

        public OutputThread(BufferedReader reader) {
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
                    System.out.println("Stdout: " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
