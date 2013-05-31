package udplog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

// synchronicity is needed;
class PacketHandler {
    public PacketHandler(String dir) {
        gui = null;
        directory = dir;
        writers = new HashMap<String, FileWriter>();
        discards = null;
    }

    public void setGUI(GUI g) {
        gui = g;
    }

    private GUI gui;
    private final HashMap<String, FileWriter> writers;
    private final String directory;
    private FileWriter discards;

    public synchronized void receive(String input) {
        if (gui != null) {
            gui.showNewPacket(input);
        }

        // reset the writers
        if ("#NEW\n".equals(input)) {
            cut();
            return;
        }

        String[] s = input.split(":");
        if (s.length != 2) {
            if (discards == null) {
                try {
                    discards = new FileWriter(directory + File.separator
                            + "log_junk-" + Util.timestamp() + ".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Creating a new log file failed.");
                    return;
                }
            }
            try {
                discards.append(input).flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Writing to filestream failed.");
            }
            gui.showNewPair("::", input);
            return;
        }
        String prefix = s[0];
        String values = s[1];

        FileWriter n = writers.get(prefix);
        if (n == null) {
            try {
                n = new FileWriter(directory + File.separator + "log-" + prefix
                        + "-" + Util.timestamp() + ".txt");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Creating a new log file failed.");
                return;
            }
            writers.put(prefix, n);
        }

        try {
            n.append(values).flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Writing to filestream failed.");
        }

        if (gui != null) {
            gui.showNewPair(prefix, values);
        }
    }

    // empty our HashMap;
    public synchronized void cut() {
        // close all the writers
        Collection<FileWriter> c = writers.values();
        for (FileWriter f : c) {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // and get rid of them
        writers.clear();
        if (discards != null) {
            try {
                discards.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            discards = null;
        }
    }
}