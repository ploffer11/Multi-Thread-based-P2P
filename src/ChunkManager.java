import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class ChunkManager
{
    private final char NOT_DOWNLOADED = '0';
    private final char DOWNLOADING = '1';
    private final char DOWNLOADED = '2';

    private int totalChunk;
    private int downloadedChunk;
    private volatile char[] chunkArray;

    public ChunkManager(int totalChunk)
    {
        downloadedChunk = 0;
        this.totalChunk = totalChunk;
        if (totalChunk != Integer.MAX_VALUE)
        {
            chunkArray = new char[totalChunk];
            for (int i = 0; i < totalChunk; i++)
                chunkArray[i] = NOT_DOWNLOADED;
        }
    }

    // 현재 자신의 download 상태를 주는 함수
    public String getBitMap()
    {
        if (totalChunk == Integer.MAX_VALUE)
            return "X";

        String bitMap = "";

        for (char i : chunkArray)
            bitMap += Character.toString(i);

        //System.out.println("bitMap: " + bitMap);

        return bitMap;
    }

    // 다른 peer 의 bitMap 을 확인해 현재 다운 가능한 걸 리턴
    public ArrayList<Integer> getDownloadList(String anoBitMap)
    {
        ArrayList<Integer> canDownloadList = new ArrayList<Integer>();

        for (int i = 0; i < chunkArray.length; i++)
        {
            if (anoBitMap.charAt(i) == DOWNLOADED && isNotDownloaded(i))
            {
                canDownloadList.add(i);

                if (canDownloadList.size() == 3)
                    return canDownloadList;
            }
        }

        return canDownloadList;
    }

    // downloadChunkArray의 접근이 잘못됨을 방지
    // download가 안됬다면 downloading상태로 변경 후 true 리턴
    public synchronized boolean isNotDownloaded(int chunkIndex)
    {
        if (chunkArray[chunkIndex] == NOT_DOWNLOADED)
        {
            chunkArray[chunkIndex] = DOWNLOADING;
            return true;
        }
        else
            return false;
    }

    public boolean isFinish()
    {
        System.out.println(downloadedChunk + " / " + totalChunk);
        return (downloadedChunk == totalChunk);
    }

    public synchronized void downloadComplete(int chunkIndex)
    {
        downloadedChunk++;
        chunkArray[chunkIndex] = DOWNLOADED;
    }

    public void setTotalChunk(int totalChunk)
    {
        this.totalChunk = totalChunk;
        chunkArray = new char[totalChunk];
        for (int i = 0; i < totalChunk; i++)
            chunkArray[i] = NOT_DOWNLOADED;
    }
}
