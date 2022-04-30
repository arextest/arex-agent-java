package io.arex.attacher;

import com.sun.tools.attach.VirtualMachine;

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

        try {
            VirtualMachine virtualMachine = VirtualMachine.attach(pid);
            virtualMachine.loadAgent(agentPathAndOptions, args[2]);
            virtualMachine.detach();
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                System.out.println("It seems to use the lower version of JDK to attach " +
                        "the higher version of JDK, but attach may still success");
            } else {
                e.printStackTrace();
            }
        }
    }
}