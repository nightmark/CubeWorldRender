/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evoker
 */
public class Server implements Runnable{
    
    private Main main;
    private boolean running = false;
    private ServerSocket srvr;
    
    private final byte INFO = 0; 
    private final byte MANAGE_CUBE = 1;
    private final byte CHANGED_DATA = 7;    
    private final int PORT = 1234;
    
    public Server(Main main) {
        this.main = main;
    }
    
    public void run() {
        try{
            srvr = new ServerSocket(PORT);            
            running = true;
            System.out.println("Server Started");
            listen();
        }
        catch(Exception e){
            System.err.println("Server startup crashed" + e);
        }
    }
    
    public void listen(){
        Socket skt = null;
        try{
            skt = srvr.accept();
            InputStream input = skt.getInputStream();
            while(running){
                    //System.out.println("Listening on " + PORT + "...");
                    proccessPacket(input);
            }
        }catch(IOException ex){
                    System.err.println("Server Listening Error " + ex);
        }finally{
            try {
                skt.close();
            } catch (IOException ex) {
                System.err.println("Error closing socket " + ex);
            }
        }
        System.out.println("Everything falls apart");
    }
    
    static final String HEXES = "0123456789ABCDEF";
    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
          return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
          hex.append(HEXES.charAt((b & 0xF0) >> 4))
             .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }    
    
    public void proccessPacket(InputStream in){
        try{
//            byte[] buffer = new byte[4];
//            in.read();
//            ByteBuffer bb;
//            int line = 0;
//            while ((in.read(buffer)) != -1){                
//                bb = ByteBuffer.wrap(buffer);
//                bb.order(ByteOrder.LITTLE_ENDIAN);
//                System.out.println(line+" :"+getHex(buffer)+" = "+bb.getInt());
//                line++;
//            }
//            System.out.println();
//            return;
            int id = in.read();
            System.out.println("Remaining1 " + in.available());            
            ByteBuffer bb;
            byte[] buffer;
            double cubeId;
            if(id != -1){
                switch(id){
                    case INFO:
                        buffer = new byte[4];
                        in.read(buffer);                        
                        bb = ByteBuffer.wrap(buffer);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        int cubeSize = bb.getInt();
                        System.out.println("Received Packet INFO with size " + cubeSize);
                        main.setSize(cubeSize);
                        System.out.println("setSize called");
                        bb.clear();
                        System.out.println("Remaining " + in.available());
                        break;
                    case MANAGE_CUBE:
                        buffer = new byte[8];
                        in.read(buffer);                        
                        bb = ByteBuffer.wrap(buffer);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        cubeId = bb.getDouble();
                        in.read();
                        System.out.println("Received Packet MANAGE_CUBE with id " + cubeId);
                        main.setId(cubeId);                        
                        bb.clear();
                        System.out.println("Remaining " + in.available());
                        break;
                    case CHANGED_DATA:
                        System.out.println("Received Packet CHANGED_DATA");
                        buffer = new byte[8];
                        //read ID
                        in.read(buffer);
                        bb = ByteBuffer.wrap(buffer, 0, 8);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        cubeId = bb.getDouble();
                        //read generation
                        in.read(buffer);
                        //read contents
                        //4bytes per cube cell
                        int size = main.getSize();
                        int[][][] world = new int[size][size][size];
                        System.out.println("Live Cells At:");
                        for(int z = 0; z < size; z++){
                            for(int y = 0; y < size; y++){
                                for(int x = 0; x < size; x++){
                                    int read = in.read(buffer, 0, 4);
                                    while( read != 4){
                                        read += in.read(buffer, read, 4-read);
                                    }
                                    bb = ByteBuffer.wrap(buffer, 0, 4);
                                    bb.order(ByteOrder.LITTLE_ENDIAN);
                                    world[x][y][z] = bb.getInt();
                                    if(world[x][y][z] != 0){
//                                        System.out.println(x + " " + y + " " + z + " is " + world[x][y][z]);
                                        world[x][y][z] = 8;
                                    }                                    
                                    //System.out.println("received coords " + x + " " + y + " " + z );
                                }
                            }
                        }
//                        System.out.println("Remaining " + in.available());
                        System.out.println("Remaining " + in.available());
                        System.out.println("world sent");
                        main.requestUpdate(world, cubeId);
                        break;
                    default:
                        //unknown packet
                        System.out.println("Unknown packet id:" + id);                        
                        break;
                        
                }                
            }
        }catch(Exception e){
            System.err.println("Error processing packet");
            e.printStackTrace(new PrintStream(System.err));
        }
        
    }
    
    public void stop(){
        System.out.println("Stopping server");
        try{
            srvr.close();
        }catch (Exception e){
            System.err.println("Error shutting down server " + e);
        }
        running = false;
    }
}
