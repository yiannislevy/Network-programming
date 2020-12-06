import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

public class userApp {

    public static void main(String[] args) throws IOException, LineUnavailableException {
        byte[] hostIP = { (byte) 155, (byte) 207, (byte) 18, (byte) 208 }; // standard

        int clientPort = 48007;
        int serverPort = 38007;

        String echo_code = "E4222 T00"; //LANGUAGE SENSITIVE 
        int image_size = 1024;
        String image_code = "M1147" + "UDP=" + Integer.toString(image_size); // ΜΧΧΧΧCAM=PTZDIR=X for cam2 // (add 128/256/512/1024)
        String sound_code = "A1479AQ"; // Add here AQ if wanted
        String obd_code = "V0724";

        echo e = new echo();
        image i = new image();
        sound s = new sound();
        copter c = new copter();
        obd o = new obd();

        System.out.println("Welcome to socket Application!");
        System.out.println("Please insert your choice: \n");
        System.out.println("1) Run Echo");
        System.out.println("2) Run Image"); 
        System.out.println("3) Run Sound");
        System.out.println("4) Run Copter");
        System.out.println("5) Run OBD");
        System.out.println("6) Stop the loop");

        boolean run = true;
        int input = 1;
        System.out.print("Choice: ");
        Scanner choice = new Scanner(System.in);

        do {
            input = Integer.parseInt(choice.nextLine());
            System.out.println();
            switch (input) {
                case 1:
                    System.out.println("Running Echo...\n");
                    e.echof(clientPort, serverPort, echo_code, 300, hostIP); //ran in ubuntu
                    break;
                case 2:
                    System.out.println("Running Image...\n");
                    i.imagef(clientPort, serverPort, image_code, image_size, hostIP); //run in windows
                    break;
                case 3:
                    System.out.println("Running Sound...\n");
                    s.soundf(clientPort, serverPort, sound_code, "F", "600", hostIP); //F for songs || T for frequency generator || 000-999 for number of packets // run in windows
                    break;
                case 4:
                    System.out.println("Running Copter...\n"); //open jar needed //ran in ubuntu
                    c.copterf(hostIP);
                    break;
                case 5:
                    System.out.println("Running OBD...\n");
                    o.obdf(clientPort, serverPort, obd_code, 4, hostIP); //where 4 is the number of (vehicle) minutes to run //ran in ubuntu
                    break;
                case 6:
                    run = false;
                    System.out.println("Ok bye!");
                    break;
            }
            if(run == false) 
                break;
            System.out.println("Whats next?\n\n");
            System.out.print("Choice: ");
        } while (run);
        choice.close();
    }

}
