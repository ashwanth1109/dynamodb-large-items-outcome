package com.hackathon.deploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OsTools {

    /**
     * Execute a command and gets its console output
     *
     * @param inheritIo Set this to true if you don't want console output in return value. In this case, it will print
     *                  output of the command into the java console
     * @return Output of the console captured to a string if inheritIo is false
     */
    public String executeCommandAndGetOutput(String[] commandAndArguments, boolean inheritIo)
            throws InterruptedException, IOException {
        List<String> cmdWithArgs = new ArrayList<>();
        if (isWindows()) {
            cmdWithArgs.add("cmd.exe");
            cmdWithArgs.add("/C");
        }
        cmdWithArgs.addAll(Arrays.asList(commandAndArguments));
        ProcessBuilder pb = new ProcessBuilder(cmdWithArgs);
        if (inheritIo) {
            pb.inheritIO();
        }
        Process p = pb.start();
        p.waitFor();

        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = bri.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    /**
     * Find if we are currently running on a windows box
     */
    public boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

}