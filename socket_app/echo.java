
//computer resources
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//network resources
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class echo {
    ArrayList<Double> BPS = new ArrayList<Double>();

    void echof(int clientPort, int serverPort, String packetInfo, int runtime, byte[] hostIP) throws IOException {

        InetAddress hostAddress = InetAddress.getByAddress(hostIP);

        boolean delay = true; // Variable to store files depending on code EXXXX / E0000
        boolean temp = false; // Do I ask for temperature values?
        int size = 32;

        if (packetInfo.equals("E0000")) {
            delay = false;
        }
        if (packetInfo.length() > 5 && packetInfo.substring(6, 7).equals("T")) {
            temp = true;
            size = 55;
            System.out.println("here");
        }

        byte[] txbuffer = packetInfo.getBytes(); // Sending information
        byte[] rxbuffer = new byte[size]; // Receiving information

        ArrayList<Double> rTimes = new ArrayList<Double>();
        ArrayList<Long> sysTimes = new ArrayList<Long>();
        double send = -1;

        // start of datagram
        DatagramSocket s = new DatagramSocket();
        DatagramPacket host_message = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
        DatagramSocket r = new DatagramSocket(clientPort);
        DatagramPacket server_message = new DatagramPacket(rxbuffer, rxbuffer.length);
        try {
            s.connect(hostAddress, serverPort);
            r.setSoTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error connecting ECHO: " + e);
        }
        // end of datagram

        String response = "";
        long base_time = System.currentTimeMillis();

        // Communicating
        // Checking for temperature
        if (temp) {
            try {
                s.send(host_message);
                r.receive(server_message);
            } catch (Exception e) {
                System.out.println("Error at ECHO (temp) : " + e);
            }
            response = new String(rxbuffer, 0, server_message.getLength());
            System.out.println(response);
            temperature(response,packetInfo);
            s.close();
            r.close();
            return;
        }

        do {
            send = System.currentTimeMillis();
            try {
                s.send(host_message);
                r.receive(server_message);
            } catch (Exception e) {
                System.out.println("Error at ECHO: " + e);
            }
            rTimes.add(System.currentTimeMillis() - send);
            sysTimes.add(System.currentTimeMillis() - base_time);
            response = new String(rxbuffer, 0, server_message.getLength());
            System.out.println(response);
        } while (System.currentTimeMillis() - base_time < runtime * 1000); // for #runtime seconds

        s.close();
        r.close();

        // Calculating throughput
        throughput(sysTimes);

        // Exporting data
        BufferedWriter writer1 = null;
        BufferedWriter writer2 = null;
        BufferedWriter writer3 = null;
        if (delay) {
            RTO(rTimes); // It only has meaning with delay
            writer1 = new BufferedWriter(new FileWriter("" + packetInfo + "") ); // Add here file destination 
            writer2 = new BufferedWriter(new FileWriter("") ); // Add here file destination
            writer3 = new BufferedWriter(new FileWriter("") ); // Add here file destination
        } else { // Without delay
            writer1 = new BufferedWriter(new FileWriter(""+ packetInfo + "")); // Add here file destination
            writer2 = new BufferedWriter(new FileWriter("") ); // Add here file destination
            writer3 = new BufferedWriter(new FileWriter("") ); // Add here file destination
        }
        for (int i = 0; i < rTimes.size(); i++)
            writer1.write(rTimes.get(i) + "\n");
        for (int k = 0; k < sysTimes.size(); k++)
            writer2.write(sysTimes.get(k) + "\n");
        for (int l = 0; l < BPS.size(); l++)
            writer3.write(BPS.get(l) + "\n");

        writer1.close();
        writer2.close();
        writer3.close();
        System.out.println("Done with Echo!");
    }

    // FUNCTIONS

    void throughput(ArrayList<Long> s) {
        double time = 0;
        int index = 0;
        int packets = 0;
        double bps = 0;
        for (int t = 8000; t < s.get(s.size() - 1); t += 1000) { // t = 16000 or 32000
            while (time < 8000) {
                if (t - s.get(index) < 8000) {
                    if (index == 0) {
                        time += s.get(index);
                        packets++;
                        continue;
                    }
                    time += s.get(index) - s.get(index - 1);
                    packets++;
                }
                index++;
            }
            bps = ((32 * 8) * (double) packets) / 8; // Each packet contains 32 bytes, which translates to 32 * 8 bits per second
            BPS.add(bps);
            packets = 0;
            time = 0;
            index = 0;
        }
    }

    void RTO(ArrayList<Double> r) throws IOException {
        PrintWriter writer4 = new PrintWriter(""); // Add here file destination
        PrintWriter writer5 = new PrintWriter(""); // Add here file destination
        PrintWriter writer6 = new PrintWriter(""); // Add here file destination

        double a = 0.4, b = 0.7, c = 1.1;
        double SRTT = 0;
        double sigma = 0;
        double RTO = 0;
        for (int i = 0; i < r.size(); i++) {
            SRTT = a * SRTT + (1 - a) * r.get(i);
            sigma = b * sigma + (1 - b) * Math.abs(SRTT - r.get(i));
            RTO = SRTT + c * sigma;
            writer4.println(SRTT);
            writer5.println(sigma);
            writer6.println(RTO);
        }
        writer4.close();
        writer5.close();
        writer6.close();
    }

    void temperature(String response, String packetInfo) throws FileNotFoundException {
        // 43-45
        PrintWriter t = new PrintWriter("" + packetInfo + "temp.txt"); // Add here file destination
        String temperature = response.substring(43, 46) + "°C";
        t.println(temperature);
        System.out.println("The temperature is... " + temperature);
        t.close();
    }
}
