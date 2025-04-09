import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class rsync_new {

    String address_active;

    String address_passive;

    String my_address;

    int a, b, c, d, e, f, g, h;

    static InetAddress ipin = null;

    public int rsyncRun() {
        try {
            System.out.println("HELLO");
            String capitalizedSentence;
            DatagramSocket serverSocket = new DatagramSocket(9876);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            try {
                InetAddress addr = InetAddress.getLocalHost();
                my_address = addr.getHostAddress();
                System.out.println(my_address);
                String my_new = my_address.replace(".", ",");
                String[] my_final_addr = my_new.split(",");
                System.out.println(my_final_addr.length);
                e = Integer.parseInt(my_final_addr[0]);
                f = Integer.parseInt(my_final_addr[1]);
                g = Integer.parseInt(my_final_addr[2]);
                h = Integer.parseInt(my_final_addr[3]);
                System.out.println(e + "." + f + "." + g + "." + h);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (true) {
                System.out.println("WAITING FOR NEXT CHANGE");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println(sentence);
                String final_ip = sentence.trim();
                String final_ip_comma = final_ip.replace(".", ",");
                String[] primary_addr = final_ip_comma.split(",");
                a = Integer.parseInt(primary_addr[0]);
                b = Integer.parseInt(primary_addr[1]);
                c = Integer.parseInt(primary_addr[2]);
                d = Integer.parseInt(primary_addr[3]);
                byte[] prim_addr = new byte[4];
                prim_addr[0] = (byte) a;
                prim_addr[1] = (byte) b;
                prim_addr[2] = (byte) c;
                prim_addr[3] = (byte) d;
                ipin = InetAddress.getByAddress(prim_addr);
                String rsync_string = "rsync -avz -e ssh myserver@" + final_ip;
                String later = ":/home/myserver /home/myserver/Desktop/backup250";
                String cmd = "ping 192.168.3.254";
                int loop = 1;
                System.out.println("REACHED HERE");
                while (loop == 1) {
                    if (!(a == e && b == f && c == g && d == h)) {
                        System.out.println("INSIDE LOOP");
                        try {
                            Process p = Runtime.getRuntime().exec(cmd);
                            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String line = null;
                            while ((line = input.readLine()) != null) {
                                System.out.println(line);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        boolean check1 = ipin.isReachable(3000);
                        if (!check1) {
                            boolean check2 = ipin.isReachable(3000);
                            if (!check2) {
                                loop = 0;
                            }
                        }
                    } else {
                        loop = 0;
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return 0;
    }

    public static void main(String args[]) {
        rsync_new r = new rsync_new();
        r.rsyncRun();
    }
}
