package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Main extends SimpleApplication {

    int sizeX = 0;
    int sizeY = 0;
    int sizeZ = 0;
    int oldSizeX = 0;
    int oldSizeY = 0;
    int oldSizeZ = 0;
    ColorRGBA world[][][];
    Geometry worldGeometries[][][];
    Server server;
    
    public static void main(String[] args) {        
        Main app = new Main();                
        app.start();
    }

    @Override
    public void simpleInitApp() {        
        //open TCP communication        
        setSize(32);
        world = new ColorRGBA[sizeX][sizeY][sizeZ];
        worldGeometries = new Geometry[sizeX][sizeY][sizeZ];
        cam.setFrustumFar(30);
        flyCam.setMoveSpeed(5);
        initWorld();
        initCube();
        initServer();
    }
    
    public void initServer(){        
        server = new Server(this);
        new Thread(server).start();
    }

    @Override
    public void simpleUpdate(float tpf) {
//        if(oldSizeX != sizeX || oldSizeY != sizeY || oldSizeZ != sizeZ){
//            for(int x = oldSizeX; x < sizeX; x++){
//                for(int y = oldSizeY; y < sizeY; y++){
//                    for(int z = oldSizeZ; z < sizeZ; z++){
//                        worldGeometries[x][y][z].getMaterial().setColor("Color", new ColorRGBA(1,1,1,0));
//                    }
//                }
//            }
//        }
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
    
    public void updateCube(int[][][] world){
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
                            geom.setMaterial(mat);
                            rootNode.attachChild(geom);
                            worldGeometries[x][y][z] = null;
                        }
                    }
                }
            }
        }
        for(int x = 0; x < sizeX; x++){
            for(int y = 0; y < sizeY; y++){
                for(int z = 0; z < sizeZ; z++){
                    //ColorRGBA color = new ColorRGBA(x/(float)sizeX, y/(float)sizeY, z/(float)sizeZ, 0);
                    ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
                    worldGeometries[x][y][z].getMaterial().setColor("Color", color);
                    rootNode.attachChild(worldGeometries[x][y][z]);
                }
            }
        }        
    }
    
    public void initWorld(){
        for(int x = 0; x < sizeX; x++){
                for(int y = 0; y < sizeY; y++){
                    for(int z = 0; z < sizeZ; z++){
                        world[x][y][z] = new ColorRGBA(x/100f+0.15f, y/100f+0.3f, z/100f+0.35f, 0);
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
                    geom.setMaterial(mat);
                    rootNode.attachChild(geom);
                }
            }
        }
    }
}
