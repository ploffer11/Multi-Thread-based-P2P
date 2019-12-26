import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class Upload extends Thread
{
    private int port;
    private String host;
    private String bitMap;
    private ServerSocket serverSocket;
    private ChunkManager chunkManager;
    private FileManager fileManager;

    public Upload(String host, int port, ChunkManager chunkManager, FileManager fileManager) throws Exception
    {
        serverSocket = new ServerSocket(port);
        this.host = host;
        this.port = port;
        this.chunkManager = chunkManager;
        this.fileManager = fileManager;
    }

    public void run()
    {
        try
        {
            while (true)
            {
                Socket welcomeSocket = serverSocket.accept();
                BufferedReader receiveRequestStream = new BufferedReader(
                        new InputStreamReader(welcomeSocket.getInputStream()));
                DataOutputStream outputStream = new DataOutputStream(welcomeSocket.getOutputStream());

                // 자신의 bitMap 상황을 보내준다.
                while (true)
                {
                    long start = System.currentTimeMillis();

                    outputStream.writeBytes(chunkManager.getBitMap() + "\n");
                    int totalRequest = Integer.parseInt(receiveRequestStream.readLine().trim());

                    if (System.currentTimeMillis() - start > 5000.0)
                        continue;

                    if (totalRequest == 0)
                        break;

                    for (int i = 0; i < totalRequest; i++)
                    {
                        int chunkIndex = Integer.parseInt(receiveRequestStream.readLine().trim());
                        fileManager.uploadChunk(chunkIndex, outputStream);
                    }

                    if (totalRequest > -1)
                        break;
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
