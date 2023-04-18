package io.arex.attacher;

import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * agent attacher
 * @date 2022/4/29
 */
public class AgentAttacher {
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("agent attach arguments is null.");
            return;
        }

        if (args.length < 2) {
            System.out.printf("agent attach arguments length is %d, need length: 2%n", args.length);
            return;
        }

        String pid = args[0];
        String agentPathAndOptions = args[1];
        String additionalParam = "";
        if (args.length > 2) {
            additionalParam = args[2];
        }

        try {
            VirtualMachine virtualMachine = VirtualMachine.attach(pid);
            try {
                virtualMachine.loadAgent(agentPathAndOptions, additionalParam);
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                    System.out.println("It seems to use the lower version of JDK to attach " +
                            "the higher version of JDK, but attach may still success");
                } else {
                    throw e;
                }
            } catch (AgentLoadException e) {
                if ("0".equals(e.getMessage())) {
                    // https://stackoverflow.com/a/54454418
                    System.out.println("It seems to use the higher version of JDK to attach " +
                            "the lower version of JDK, but attach may still success");
                } else {
                    throw e;
                }
            }
            virtualMachine.detach();
        } catch (Throwable e) {
            // expected behavior, it will be returned as error stream to the caller, if any
            e.printStackTrace();
        }
    }
}