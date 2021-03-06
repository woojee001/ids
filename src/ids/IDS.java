package ids;

import net.sourceforge.jpcap.capture.*;
import net.sourceforge.jpcap.net.*;

import java.util.*;
import java.io.*;
import parse.*;
import def.*;

/**
 * IDS.java: Main class for the IDS. Process the command line arguments (make sure
 * there are two of them), parse the rule file, build the PacketCapture object and
 * begin capturing packets.
 */
public class IDS {
    private static final String usage = "Usage: IDS [rule_file] [pcap_file]";
    private static final int WIDTH = 80; //terminal width

    public static void main(String[] argv) {
    	if (argv.length < 2)
	    die(usage);

	String rules = argv[0]; //rule file
	String pfile = argv[1]; //pcap file

    	ThreatDefinition def = loadDefinition(rules);
	IDSListener listener = new IDSListener(new IDSScanner(def));

	packetCapture(listener, pfile);	
    }

    private static void printHeader() {
	System.out.println("(PCKET ID): IPPacket.toColoredString(true)\t\t4 head bytes: [xxxx xxxx xxxx xxxx]");

	for (int i = 0; i < WIDTH; i++)
	    System.out.print("-");

	System.out.println();
    }

    private static void packetCapture(IDSListener listener, String pfile) {
	PacketCapture pc = new PacketCapture();

	try {
	    pc.addPacketListener(listener);
	    pc.openOffline(pfile);

	    System.out.println();
	    printHeader();

	    pc.capture(-1);
	} catch (CaptureFileOpenException e) {
	    die("Could not open file: "+e.getMessage());
	} catch (CapturePacketException e) {
	    System.out.println("Done scanning trace...");
	}
    }

    private static ThreatDefinition loadDefinition(String fname) {
	try {
	    parser p = new parser(new lexer(new FileReader(fname)));
	    Object d = p.parse().value;

	    if (d instanceof ThreatDefinition)
		return (ThreatDefinition) d;
	} catch (FileNotFoundException e) {
	    die("File not found: "+fname);
	} catch (Exception e) {
	    die("Parser exception: "+e.getMessage());
	}

	die("Error loading ThreatDefinition: "+fname);

	return null;
    }

    private static void die(String msg) {
    	System.err.println(msg);
    	System.exit(1);
    }

}
