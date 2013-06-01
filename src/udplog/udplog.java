/**
 * Takes UDP packets at a given port, 
 * with text of type "N:1.23,0,111.2,96.5"
 * 
 * @author msto
 *
 */
package udplog;

import java.io.File;

/*
 *  Basic program flow: it starts, opens a window, and
 *  you interact with that window. As soon as packets
 *  start to come in, a new file per head code is created.
 *  When the #NEW\n directive is sent, the current file is closed
 *  and a new one is made. 
 *  
 *  On the graphical side, there is a stop button - when called,
 *  it cuts the current file stream. Further packets are ignored until
 *  the button (now labeled "start") is pressed, at which point a 
 *  new set of file streams can be opened
 * 
 */
public class udplog {
    public static final int DEFAULT_PORT = 1140;

    public static void main(String[] args) {
        boolean faked = false;
        boolean fast = false;
        for (String s : args) {
            if ("--fakestream".equals(s)) {
                faked = true;
            } else if ("--fast".equals(s)) {
                fast = true;
            } else if ("--slow".equals(s)) {
                fast = false;
            } else if ("--help".equals(s)) {
                System.out
                        .println("Udplogger. args: There is one optional arg: --fakestream, which fakes incoming packets.");
                return;
            } else {
                System.out
                        .println("Udplogger. There is one optional arg: --fakestream, which fakes incoming packets.");
                return;
            }
        }
        System.out.format("I am here! Faking stream: %b\n", faked);

        File targetDirectory = new File("robotlogs/");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
            System.out.format("Created directory: %s\n",
                    targetDirectory.getAbsolutePath());
        }

        // setup
        PacketHandler p = new PacketHandler(targetDirectory.getAbsolutePath());
        final Receiver r = new Receiver(DEFAULT_PORT, p, faked, (fast ? 5 : 50));
        PortStreamChanger psc = new PortStreamChanger(r);
        GUI g = new GUI(r, psc);
        p.setGUI(g);

        // starts receiving
        r.start();
        // enables user interaction
        g.show();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                r.close();
                System.out.println("Killed");
            }
        });
    }
}
