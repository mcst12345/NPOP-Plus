package miku.npop.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("The fuck? Pid of target process isn't provided?");
        }
        String pid = args[0];
        String file = args[1];

        System.out.println("Target:" + pid);
        System.out.println("Agent file:" + file);

        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(file);
    }
}
