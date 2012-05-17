package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends SimpleApplication {

    private int sizeX = 0;
    private int sizeY = 0;
    private int sizeZ = 0;
    private int oldSizeX = 0;
    private int oldSizeY = 0;
    private int oldSizeZ = 0;
    private final int  DEFAULT_WORLD_SIZE = 32;
    private ColorRGBA world[][][];
    private Geometry worldGeometries[][][];
    private Server server;
    private AtomicBoolean updatePending = new AtomicBoolean(false);
    private int[][][] pendingWorld = null;
    
    public static void main(String[] args) {        
        Main app = new Main();
        app.start();        
    }
    private double cubeId = 0d;

    @Override
    public void simpleInitApp() {        
        //open TCP communication        
        setSize(DEFAULT_WORLD_SIZE);
        java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
        world = new ColorRGBA[sizeX][sizeY][sizeZ];
        worldGeometries = new Geometry[sizeX][sizeY][sizeZ];
        cam.setFrustumFar(30);
        flyCam.setMoveSpeed(5);
        initKeys();
        initWorld();
        initCube();
        initServer();        
    }
    
    private void initKeys() {
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("Quit",  new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addListener(actionListener, new String[]{"Quit"});
 
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
          if (name.equals("Quit") && !keyPressed) {
              stopServer();
              stop();
          }
        }
    };
    
    public void initServer(){        
        server = new Server(this);
        new Thread(server).start();
    }
    
    public void stopServer(){        
        server.stop();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(updatePending.get()){
            updateCube(pendingWorld);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public void setSize(int size){
        oldSizeX = sizeX;
        oldSizeY = sizeY;
        oldSizeZ = sizeZ;
        sizeX = size;
        sizeY = size;
        sizeZ = size;
    }
    
    public int getSize(){
        return sizeX;
    };
    
    public synchronized void requestUpdate(int[][][] world, double id){        
        if(updatePending.get() && cubeId != id){
            System.out.println("Not Updating " + id);
            return;
        }        
        pendingWorld = world;
        updatePending.set(true);
    }
    
    public void updateCube(int[][][] world){
        System.out.println("updating cube");
        synchronized(this){
            rootNode.detachAllChildren();
            if(oldSizeX > sizeX || oldSizeY > sizeY || oldSizeZ > sizeZ){
                for(int x = oldSizeX; x < sizeX; x++){
                    for(int y = oldSizeY; y < sizeY; y++){
                        for(int z = oldSizeZ; z < sizeZ; z++){
                            worldGeometries[x][y][z] = null;
                        }
                    }
                }
            }else{
                if(oldSizeX < sizeX || oldSizeY < sizeY || oldSizeZ < sizeZ){
                    for(int x = oldSizeX; x < sizeX; x++){
                        for(int y = oldSizeY; y < sizeY; y++){
                            for(int z = oldSizeZ; z < sizeZ; z++){
                                Box b = new Box(new Vector3f(x, y, z), (float)0.2, (float)0.2, (float)0.2);
                                Geometry geom = new Geometry("Box "+x+"|"+y+"|"+z, b);
                                worldGeometries[x][y][z] = geom;
                                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                                mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                                geom.setQueueBucket(Bucket.Transparent);
                                geom.setMaterial(mat);
                                rootNode.attachChild(geom);                                                                
                            }
                        }
                    }
                }
            }
            for(int x = 0; x < sizeX; x++){
                for(int y = 0; y < sizeY; y++){
                    for(int z = 0; z < sizeZ; z++){
                        //ColorRGBA color = new ColorRGBA(x/(float)sizeX, y/(float)sizeY, z/(float)sizeZ, 0);
                        ColorRGBA color = new ColorRGBA(world[x][y][z]/10f + 0.2f, world[x][y][z]/10f + 0.2f, world[x][y][z]/10f + 0.2f, 0.6f);
                        //ColorRGBA color = new ColorRGBA(1, 0, 0, 0);                                                
                        worldGeometries[x][y][z].getMaterial().setColor("Color", color);                        
                        rootNode.attachChild(worldGeometries[x][y][z]);
                    }
                }
            }
        }
        System.out.println("cube updated");
        updatePending.set(false);
    }
    
    public void initWorld(){
        for(int x = 0; x < sizeX; x++){
                for(int y = 0; y < sizeY; y++){
                    for(int z = 0; z < sizeZ; z++){
                        world[x][y][z] = new ColorRGBA(x/100f+0.15f, y/100f+0.3f, z/100f+0.35f, 0.8f);                        
                    }
                }
        }
    }
    
    public void initCube(){
        for(int x = 0; x < sizeX; x++){
            for(int y = 0; y < sizeY; y++){
                for(int z = 0; z < sizeZ; z++){
                    Box b = new Box(new Vector3f(x, y, z), (float)0.2, (float)0.2, (float)0.2);
                    Geometry geom = new Geometry("Box "+x+"|"+y+"|"+z, b);
                    worldGeometries[x][y][z] = geom;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", world[x][y][z]);  
                    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                    geom.setMaterial(mat);
                    geom.setQueueBucket(Bucket.Transparent);
                    rootNode.attachChild(geom);
                }
            }
        }
    }

    void setId(double cubeId) {
        this.cubeId = cubeId;
    }
}
