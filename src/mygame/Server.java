/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author evoker
 */
public class Server implements Runnable{
    
    Main main;
    boolean running = false;
    ServerSocket srvr;
    
    private final byte INFO = 0; 
    private final byte CHANGED_DATA = 7;
    private final int SOCKET = 1234;
    
    public Server(Main main) {
        this.main = main;
    }
    
    public void run() {
        try{
            srvr = new ServerSocket(SOCKET);            
            running = true;
            System.out.println("Server Started");
            listen();
        }
        catch(Exception e){
            System.err.println("Server startup crashed" + e);
        }
    }
    
    public void listen(){
        while(running){
            try{
                System.out.println("Listening on " + SOCKET + "...");
                Socket skt = srvr.accept();                                
                proccessPacket(skt.getInputStream());
                skt.close();
            }catch(Exception e){
                System.err.println("Server Listening Error " + e);
            }
        }
    }
    
    public void proccessPacket(InputStream in){
        try{
            int id = in.read();            
            ByteBuffer bb;
            byte[] buffer;
            if(id != -1){
                switch(id){
                    case INFO:
                        buffer = new byte[4];
                        in.read(buffer);                        
                        bb = ByteBuffer.wrap(buffer);
                        int cubeSize = bb.getInt();
                        System.out.println("Received Packet INFO with size " + cubeSize);
                        main.setSize(cubeSize);
                        System.out.println("setSize called");
                        bb.clear();
                        break;
                    case CHANGED_DATA:
                        System.out.println("Received Packet CHANGED_DATA");
                        buffer = new byte[8];
                        //read ID
                        in.read(buffer);
                        //read generation
                        in.read(buffer);
                        //read contents
                        //4bytes per cube cell
                        int size = main.getSize();
                        int[][][] world = new int[size][size][size];
                        for(int x = 0; x < size; x++){
                            for(int y = 0; y < size; y++){
                                for(int z = 0; z < size; z++){
                                    in.read(buffer, 0, 4);
                                    bb = ByteBuffer.wrap(buffer, 0, 4);
                                    world[x][y][z] = bb.getInt();
                                }
                            }
                        }
                        System.out.println("sending world : ");
                        for(int x = 0; x < size; x++){
                            for(int y = 0; y < size; y++){
                                for(int z = 0; z < size; z++){
                                    System.out.println(x + " " + y + " " + " " + z + " is " + world[x][y][z]);
                                }
                            }
                        }
                        main.updateCube(world);
                        break;
                    default:
                        //unknown packet
                        System.out.println("Unknown packet id:" + id);                        
                        break;
                        
                }
            }
        }catch(Exception e){
            System.err.println("Error processing packet" + e);
        }
        
    }
    
    public void stop(){
        try{
            srvr.close();
        }catch (Exception e){
            System.err.println("Error shutting down server " + e);
        }
        running = false;
    }
}
