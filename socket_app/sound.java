
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFileFormat.Type;

public class sound {

    void soundf(int clientPort, int serverPort, String sc, String Y, String XXX, byte[] hostIP)
            throws IOException, LineUnavailableException {

        InetAddress hostAddress = InetAddress.getByAddress(hostIP);

        String packetInfo = sc + Y + XXX;
        int packetNo = Integer.parseInt(XXX);

        // Audio control stuff
        AudioFormat pcm;
        int packetSize;
        if (packetInfo.substring(5, 7).equals("AQ")) {
            System.out.println("AQ-DPCM");
            packetSize = 132;
            pcm = new AudioFormat(8000, 16, 1, true, false);
        } else {
            System.out.println("DPCM");
            packetSize = 128;
            pcm = new AudioFormat(8000, 8, 1, true, false);
        }

        byte[][] buffer = new byte[packetNo][packetSize]; // the array in which Ι'll store the audio packets from ithaki

        byte[] txbuffer = packetInfo.getBytes(); // Sending information
        byte[] rxbuffer = new byte[packetSize]; // Storing temporarily received information

        // Start of datagram
        DatagramSocket s = new DatagramSocket();
        DatagramSocket r = new DatagramSocket(clientPort);
        DatagramPacket host_message = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
        DatagramPacket server_message = new DatagramPacket(rxbuffer, rxbuffer.length);
        try {
            s.connect(hostAddress, serverPort);
            r.setSoTimeout(5000); // How much time to wait in ms
        } catch (Exception e) {
            System.out.println("Error connecting SOUND: " + e);
        }
        // End of datagram

        // Making sure request is sent
        int sendCounter = 0; // How many times do I have to try before I send my request
        while (true) {
            try {
                s.send(host_message);
                sendCounter++;
                System.out.println("I sent my message, try no." + sendCounter);
            } catch (Exception e) {
                System.out.println("Failed to send at try no." + sendCounter);
                System.out.println("Because : " + e);
            }
            break;
        }

        // Receiving reply and storing at buffer[][]
        int receiveCounter = 0; // to check how many packets I receive
        for (int i = 0; i < packetNo; i++) {
            try {
                r.receive(server_message);
                receiveCounter++;
            } catch (Exception e) {
                System.out.println("Error is: " + e);
            }
            for (int j = 0; j < packetSize; j++)
                buffer[i][j] = rxbuffer[j];
        }
        System.out.println("Received " + receiveCounter + " packets");
        s.close();
        r.close();

        // Demodulating
        byte[] bufferOut;
        if (packetInfo.substring(5, 7).equals("AQ")) {
            bufferOut = aq_dpcm(receiveCounter, buffer);
        } else {
            bufferOut = dpcm(receiveCounter, buffer, 1);
        }

        // Playing audio
        try {
            playAudio(bufferOut, pcm);
        } catch (Exception e) {
            System.out.println("Error trying to play audio: " + e);
        }

        // Saving audio
        saveAudio(bufferOut, packetNo, packetSize, packetInfo);

        System.out.println("Done with Sound!");
    }

    byte[] dpcm(int receiveCounter, byte[][] buffer, int volume) throws FileNotFoundException {
        System.out.println("Demodulating...");
        PrintWriter sam = new PrintWriter(""); // Add here file destination
        PrintWriter dif = new PrintWriter(""); // Add here file destination
        byte[] bufferOut = new byte[128 * 2 * receiveCounter];
        int help, nibble1, nibble2;
        int sum = 0;
        int packetSize = buffer[0].length;
        int index = -1;
        for (int i = 0; i < receiveCounter; i++)
            for (int j = 0; j < packetSize; j++) {
                help = (int) buffer[i][j]; // step 1: splitting the nibbles
                nibble1 = (help) & 15; // 15 -> 0000 1111
                nibble2 = (help >>> 4) & 15; // 15 -> 0000 1111

                help = (nibble2 - 8) * volume; // step 2: substracting 8 and step 3: multiplying with wanted volume
                dif.println(help); // storing differences

                sum += help;

                bufferOut[++index] = (byte) sum;
                sam.println(bufferOut[index]); // storing samples

                help = (nibble1 - 8) * volume; // volume aka beta
                dif.println(help); // storing differences

                sum += help;

                bufferOut[++index] = (byte) sum;
                sam.println(bufferOut[index]); //storing samples
                
            }
            sam.close();
            dif.close();
        return bufferOut;
    }

    byte[] aq_dpcm(int receiveCounter, byte[][] buffer) throws FileNotFoundException {
        System.out.println("Demodulating AQ...");
        PrintWriter sa = new PrintWriter(""); // Add here file destination 
        PrintWriter me = new PrintWriter(""); // Add here file destination
        PrintWriter st = new PrintWriter(""); // Add here file destination
        PrintWriter dif = new PrintWriter(""); // Add here file destination

        byte[] bufferOut = new byte[2 * (128 * 2) * receiveCounter];
        int[][] samples = new int[receiveCounter][128 * 2];
        int[] mean = new int[receiveCounter];
        int[] step = new int[receiveCounter];
        int currentByte, nibblesLS, nibbleMS;
        int index = -1;

        for (int i = 0; i < receiveCounter; i++) {
            // step 1: finding and storing mean and step (first 4 bytes of every packet received)
            mean[i] = 256 * buffer[i][1] + buffer[i][0]; // 256*msb+lsb
            me.println(mean[i]); //storing means

            step[i] = 256 * buffer[i][3] + buffer[i][2]; // 256*msb+lsb;
            st.println(step[i]); //storing steps

            index = -1;
            for (int j = 4; j < buffer[i].length; j++) {

                currentByte = (int) buffer[i][j];
                // step 2: separating to nibbles (most and least significant)
                // step 3: subtructing 8
                // step 4: multiplying by step
                
                nibblesLS = ((currentByte & 15) - 8) * step[i]; //1
                nibbleMS = (((currentByte >>> 4) & 15) - 8) * step[i]; //2

                samples[i][++index] = (nibbleMS + mean[i]);
                samples[i][++index] = (nibblesLS + mean[i]);
                
                dif.println((((currentByte >>> 4) & 15) - 8)); //storing difference of MS
                dif.println(((currentByte & 15) - 8)); //storing difference of LS
            }
        }
        index = -1;
        for (int m = 0; m < samples.length; m++)
            for (int n = 0; n < samples[0].length; n++) {
                bufferOut[++index] = (byte) (samples[m][n]);
                sa.println(bufferOut[index]); //storing samples
                bufferOut[++index] = (byte) ((samples[m][n] >>> 8));
                sa.println(bufferOut[index]); //storing samples
            }
        sa.close();
        me.close();
        st.close();
        dif.close();
        return bufferOut;
    }

    public static void playAudio(byte[] bufferOut, AudioFormat pcm) {
        // Playing live music by ithakiFM
        SourceDataLine out = null;
        try {
            out = AudioSystem.getSourceDataLine(pcm);
            out.open(pcm, bufferOut.length + 1000); // to be safe in terms of getting enough packets to listen live
        } catch (LineUnavailableException e) {
            System.out.println("Error playing audio live.. " + e);
        }
        out.start();
        out.write(bufferOut, 0, bufferOut.length);
        out.flush();
        out.stop();
        out.close();
    }

    // Store the audio packets from ithaki
    public static void saveAudio(byte[] bufferOut, int packetNo, int packetSize, String packetInfo) {
        String where = "";
        InputStream in = new ByteArrayInputStream(bufferOut);
        AudioFormat pcm;
        if (packetInfo.substring(5, 7).equals("AQ")) {
            pcm = new AudioFormat(8000, 16, 1, true, false);
            where = "aq_dpcm\\";
        } else {
            pcm = new AudioFormat(8000, 8, 1, true, false);
            where = "dpcm\\";
        }
        AudioInputStream stream = new AudioInputStream(in, pcm, bufferOut.length);
        File file = new File("" + where + packetInfo + ".wav"); // Add here file destination
        try {
            AudioSystem.write(stream, Type.WAVE, file);
        } catch (IOException e) {
            System.out.println("Error saving audio... " + e);
        }
    }
}
