
//computer resources
import java.io.FileOutputStream;
import java.io.IOException;
//network resources
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class image {

    void imagef(int clientPort, int serverPort, String packetInfo, int size, byte[] hostIP) throws IOException {

        InetAddress hostAddress = InetAddress.getByAddress(hostIP);
        
        byte[] txbuffer = packetInfo.getBytes(); // Sending information aka image code
        byte[] rxbuffer = new byte[size]; // Storing received information aka image
        ArrayList<Byte> responses = new ArrayList<Byte>();

        // start of datagram
        DatagramSocket s = new DatagramSocket();
        DatagramPacket host_message;
        DatagramSocket r = new DatagramSocket(clientPort);
        DatagramPacket server_message = new DatagramPacket(rxbuffer, rxbuffer.length);
        try {
            host_message = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
            s.connect(hostAddress, serverPort);
            r.setSoTimeout(5000);
            s.send(host_message);
        } catch (Exception e) {
            System.out.println("Error connecting IMAGE :" + e);
        }
        // end of datagram

        // Making the .jpg file
        String place = "" + packetInfo + ".jpg"; // Add here file destination
        FileOutputStream im = new FileOutputStream(place);

        int pcounter = 0; // to check how many packets I receive
        int bcounter = 0; // to check how many bytes I receive

        // Receiving
        boolean run = true;
        do {
            try {
                r.receive(server_message);
            } catch (Exception e) {
                System.out.println("Error receiving IMAGE:" + e);
                continue;
            }
            pcounter++;
            for (int i = 0; i < size; i++) {
                responses.add(rxbuffer[i]);
                // Exporting the bytes live, as they arrive.
                im.write(rxbuffer[i]); // Without allocating any memory, I can print the image. Ofcourse I allocate so that I check the start and end delimeter
                if (bcounter > 0 && responses.get(bcounter - 1) == (byte) 0xFF
                        && responses.get(bcounter) == (byte) 0xD9) { // End delimeter ---> stop fetching data!
                    bcounter++;
                    run = false;
                    break;
                }
                bcounter++;
            }
        } while (run);
        
        s.close();
        r.close();
        
        System.out.println("Received " + pcounter + " packets");
        
        //Determining if all is well with the image that arrived
        if (responses.get(0) == -1 && responses.get(1) == -40 && responses.get(bcounter - 2) == -1
                && responses.get(bcounter - 1) == -39) {
            System.out.println("Received Successfuly");
        } else {
            System.out.println("Error, the Image is not here.");
        }

        im.close();
        System.out.println("Done with Image!");

    }
}
