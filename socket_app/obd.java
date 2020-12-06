import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class obd {
    // Arrays for each data type needed to record
    ArrayList<Integer> engineRunTimes = new ArrayList<Integer>();
    ArrayList<Integer> intakeAirTemperatures = new ArrayList<Integer>();
    ArrayList<Integer> throttlePositions = new ArrayList<Integer>();
    ArrayList<Integer> engineRPM = new ArrayList<Integer>();
    ArrayList<Integer> vehicleSpeeds = new ArrayList<Integer>();
    ArrayList<Integer> coolantTemperatures = new ArrayList<Integer>();

    void obdf(int clientPort, int serverPort, String oc, int rt, byte[] hostIP) throws IOException {
        InetAddress hostAddress = InetAddress.getByAddress(hostIP);

        String[] codes = { "OBD=01 1F", "OBD=01 0F", "OBD=01 11", "OBD=01 0C", "OBD=01 0D", "OBD=01 05" }; //All the codes needed
        String packetInfo = "";
        byte[] txbuffer;
        byte[] rxbuffer = new byte[11]; // Ithaki sends 11 characters each time

        // // start of datagram
        DatagramSocket s = new DatagramSocket();
        DatagramPacket host_message;
        DatagramSocket r = new DatagramSocket(clientPort);
        DatagramPacket server_message = new DatagramPacket(rxbuffer, rxbuffer.length);
        try {
            s.connect(hostAddress, serverPort);
            r.setSoTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error connecting sockets to ithaki: " + e);
        }
        // end of datagram

        String response = "";
        int runtime = 0;
        boolean run = true;
        do {
            for (int i = 0; i < 6; i++) {
                // Which code
                packetInfo = oc + codes[i];
                txbuffer = packetInfo.getBytes();
                host_message = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
                try {
                    s.send(host_message);
                    r.receive(server_message);
                } catch (Exception p) {
                    System.out.println("Error receiving: " + p);
                    continue;
                }
                response = new String(rxbuffer, 0, server_message.getLength());
                System.out.println(response);
                run_store(i, response);
            }
            runtime = engineRunTimes.get(engineRunTimes.size()-1)-engineRunTimes.get(0);
            System.out.println("Runtime is: " + runtime);
            if (runtime >= (rt * 60))
                run = false;
        } while (run);
        s.close();
        r.close();

        System.out.println("Runtime: " + runtime);
        
        // Exporting data:
        BufferedWriter writer0 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\engine_runtimes.txt"));
        BufferedWriter writer1 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\intake_air_temperatures.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\throttle_positions.txt"));
        BufferedWriter writer3 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\engine_RPMs.txt"));
        BufferedWriter writer4 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\vehicle_speeds.txt"));
        BufferedWriter writer5 = new BufferedWriter(new FileWriter("C:\\Users\\giann\\Desktop\\Networks\\session2\\obd\\coolant_temperatures.txt"));

        for (int i = 0; i < engineRunTimes.size(); i++)
            writer0.write(engineRunTimes.get(i) + "\n");

        for (int i = 0; i < intakeAirTemperatures.size(); i++)
            writer1.write(intakeAirTemperatures.get(i) + "\n");

        for (int i = 0; i < throttlePositions.size(); i++)
            writer2.write(throttlePositions.get(i) + "\n");

        for (int i = 0; i < engineRPM.size(); i++)
            writer3.write(engineRPM.get(i) + "\n");

        for (int i = 0; i < vehicleSpeeds.size(); i++)
            writer4.write(vehicleSpeeds.get(i) + "\n");

        for (int i = 0; i < coolantTemperatures.size(); i++)
            writer5.write(coolantTemperatures.get(i) + "\n");

        writer0.close();
        writer1.close();
        writer2.close();
        writer3.close();
        writer4.close();
        writer5.close();
        System.out.println("Done with OBD!");

    }

    void run_store(int i, String response) {
        switch (i) {
            case 0:
                engineRunTimes.add(calcRunTime(response));
                break;
            case 1:
                intakeAirTemperatures.add(calcIntakeTemp(response));
                break;
            case 2:
                throttlePositions.add(calcPos(response));
                break;
            case 3:
                engineRPM.add(calcRPM(response));
                break;
            case 4:
                vehicleSpeeds.add(calcSpeed(response));
                break;
            case 5:
                coolantTemperatures.add(calcCoolTemp(response));
                break;
        }
    }

    public int calcRunTime(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        char y1 = response.charAt(9);
        char y2 = response.charAt(10);
        String hexXX = "" + x1 + x2;
        String hexYY = "" + y1 + y2;
        int XX = Integer.parseInt(hexXX, 16);
        int YY = Integer.parseInt(hexYY, 16);
        int runtime = 256 * XX + YY;
        return runtime;
    }

    public int calcIntakeTemp(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        int XX = Integer.parseInt("" + x1 + x2, 16);
        int inAirTemp = XX - 40;
        return inAirTemp;
    }

    public int calcPos(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        int XX = Integer.parseInt("" + x1 + x2, 16);
        int pos = XX * 100 / 255;
        return pos;
    }

    public int calcRPM(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        char y1 = response.charAt(9);
        char y2 = response.charAt(10);
        int XX = Integer.parseInt("" + x1 + x2, 16);
        int YY = Integer.parseInt("" + y1 + y2, 16);
        int rpm = ((XX * 256) + YY) / 4;
        return rpm;
    }

    public int calcSpeed(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        int XX = Integer.parseInt("" + x1 + x2, 16);
        int speed = XX;
        return speed;
    }

    public int calcCoolTemp(String response) {
        char x1 = response.charAt(6);
        char x2 = response.charAt(7);
        int XX = Integer.parseInt("" + x1 + x2, 16);
        int coolTemp = XX - 40;
        return coolTemp;
    }

}
