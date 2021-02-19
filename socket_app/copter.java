import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
// import java.net.InetAddress;

public class copter {

    void copterf(byte[] hostIP) throws IOException {
        int clientPort = 48078; //Always this port!

        int packetSize = 114; //Number of characters sent by Ithaki

        byte[] rxbuffer = new byte[packetSize]; // Storing temporarily received information

        String response = "";
        int size = 70;
        int[] motor = new int[size]; //Random number of packets
        int[] alt = new int[size];

        // start of datagram
        DatagramSocket r = null;
        DatagramPacket server_message = new DatagramPacket(rxbuffer, rxbuffer.length);
        try {
            r = new DatagramSocket(clientPort);
            r.setSoTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error in connecting ITHAKICOPTER: " + e);
        }
        // end of datagram

        // receiving packets
        for (int i = 0; i < size; i++) {
            try {
                r.receive(server_message);
            } catch (IOException e) {
                System.out.println("Error receiving packet : " + e);
            }
            response = new String(rxbuffer, 0, server_message.getLength());
            System.out.println(response);
            motor[i] = motorVal(response);
            alt[i] = altitude(response);
        }
        r.close();
        
        // Exporting data
        BufferedWriter out1 = new BufferedWriter(new FileWriter("")); // Add here file destination
        BufferedWriter out2 = new BufferedWriter(new FileWriter("")); // Add here file destination 

        for(int i = 0; i < motor.length; i++)
            out1.write(motor[i] + "\n");
        
        for(int i = 0; i < alt.length; i++)
            out2.write(alt[i] + "\n");    

        out1.close();
        out2.close();
        System.out.println("Done with Copter!");

    }

    int motorVal(String m) {
        int a = Character.getNumericValue(m.charAt(40)) * 100;
        int b = Character.getNumericValue(m.charAt(41)) * 10;
        int c = Character.getNumericValue(m.charAt(42)) * 1;
        int val = a + b + c;
        return val;
    }

    int altitude(String m) {
        int a = Character.getNumericValue(m.charAt(64)) * 100;
        int b = Character.getNumericValue(m.charAt(65)) * 10;
        int c = Character.getNumericValue(m.charAt(66)) * 1;
        int val = a + b + c;
        return val;
    }

}
