import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class Download extends Thread
{
    private ChunkManager chunkManager;
    private FileManager fileManager;

    private int[] portArray;
    private String[] hostArray;
    private String originalFileName;

    private int connectedPeer;
    private int myIndex;

    private boolean flag;

    public Download(int myIndex, int totalChunk, int[] portArray, String[] hostArray,
                    ChunkManager chunkManager, FileManager fileManager, String originalFileName)
    {
        this.originalFileName = originalFileName;
        this.myIndex = myIndex;
        this.connectedPeer = -1;

        this.portArray = portArray;
        this.hostArray = hostArray;

        this.chunkManager = chunkManager;
        this.fileManager = fileManager;

        flag = true;
    }

    public void run()
    {
        try
        {
            // 다운로드가 안받아 졌다면
            while (!chunkManager.isFinish())
            {
                // 다른 peer 와 연결
                Socket anoPeerSocket = connectPeer();

                BufferedReader inputStream = new BufferedReader(new InputStreamReader(anoPeerSocket.getInputStream()));
                DataInputStream inputStream1 = new DataInputStream(anoPeerSocket.getInputStream());
                DataOutputStream requestStream = new DataOutputStream(anoPeerSocket.getOutputStream());

                // 연결된 peer 의 bitMap 과 비교해 없는 것 가져오기
                // serverSocket 은 연결되자 마자 자신이 가지고 있는 bitMap 을 보내줌.
                LogManager.writeLog("request : " + myIndex + " request bitmap to " + connectedPeer);
                String bitMap = inputStream.readLine().trim();
                LogManager.writeLog("response: " + connectedPeer + " response bitmap to " + myIndex);

                if (bitMap.equals("X"))
                {
                    requestStream.writeBytes("0\n");
                    PeerManager.changePeer(connectedPeer, false);
                    continue;
                }

                synchronized (this)
                {
                    if (chunkManager.getBitMap().equals("X"))
                    {
                        chunkManager.setTotalChunk(bitMap.length());
                        fileManager.setTotalChunk(bitMap.length());
                    }
                }

                ArrayList<Integer> downloadList = chunkManager.getDownloadList(bitMap);

                // 연결된 peer 와 다른 peer 에게 다운로드 요청
                // 몇 개를 요청할지 먼저 보냄
                requestStream.writeBytes(downloadList.size() + "\n");
                for (int chunkIndex : downloadList)
                {
                    LogManager.writeLog("request : " + myIndex + " request chunk" + chunkIndex + " to " + connectedPeer);
                    if (fileManager.downloadChunk(chunkIndex, inputStream1, requestStream))
                    {
                        LogManager.writeLog("response: " + connectedPeer + " response chunk" + chunkIndex + " to " + myIndex);
                        chunkManager.downloadComplete(chunkIndex);
                    }
                    else
                    {
                        LogManager.writeLog("response: " + connectedPeer + " fail sending chunk" + chunkIndex + " to " + myIndex);
                        downloadList.clear();
                        break;
                    }
                }
                //if (downloadList.size() != 0)
                //    requestStream.writeBytes("0\n");
                PeerManager.changePeer(connectedPeer, false);
            }
            fileManager.mergeChunk(originalFileName);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Socket connectPeer()
    {
        Random rnd = new Random();
        while (true)
        {
            int i = rnd.nextInt(5);
            try
            {
                if (i != myIndex && PeerManager.changePeer(i, true))
                {
                    LogManager.writeLog("request : " + myIndex + " request connecting to " + i);
                    Socket downloadSocket = new Socket(hostArray[i], portArray[i]);
                    LogManager.writeLog("response: " + i + " response connecting to " + myIndex);
                    connectedPeer = i;
                    return downloadSocket;
                }
            }
            catch (Exception ignored)
            {
                LogManager.writeLog("response: " + myIndex + " failed connecting to " + i);
                PeerManager.changePeer(i, false);
            }
        }
    }
}