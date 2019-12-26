import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.util.stream.Collectors;

public class FileManager
{
    private int myIndex;
    private int totalChunk;
    private String myPath;
    private boolean flag;
    private int divideSize = 10240;

    public FileManager(int myIndex, int totalChunk)
    {
        this.myIndex = myIndex;
        this.totalChunk = totalChunk;
        this.myPath = "./peer" + myIndex + "/";
        this.flag = false;

        File folder = new File(myPath);
        if (!folder.exists())
            folder.mkdir();
    }

    private String makeChunkPath(int chunkIndex)
    {
        return (myPath + "chunk" + chunkIndex + ".chunk");
    }


    public void uploadChunk(int chunkIndex, DataOutputStream outputStream) throws Exception
    {
        File file = new File(makeChunkPath(chunkIndex));
        FileInputStream file_reader = new FileInputStream(file);
        byte[] buf = new byte[divideSize + 5];
        int sz = file_reader.read(buf);

        System.out.println("upload: " + chunkIndex + " sz: " + sz);
        //outputStream.writeBytes(sz + "\r\n");
        outputStream.write(buf, 0, sz);

        file_reader.close();

    }

    public boolean downloadChunk(int chunkIndex, DataInputStream inputStream, DataOutputStream requestStream) throws Exception
    {
        requestStream.writeBytes(chunkIndex + "\n");
        OutputStream out = new FileOutputStream(makeChunkPath(chunkIndex));

        byte[] buf = new byte[divideSize + 5];

        int sz = inputStream.read(buf, 0, divideSize + 5);
        int cnt = 0;
        while (sz == 0)
        {
            if (cnt == 10)
                return false;
            Thread.sleep(1000);
            sz = inputStream.read(buf, 0, divideSize + 5);
            cnt++;
        }
        System.out.println("download: " + chunkIndex + " sz: " + sz);
        out.write(buf, 0, sz);
        out.flush();
        out.close();

        return true;
    }

    public synchronized void mergeChunk(String originalFileName) throws Exception
    {
        if (flag)
            return;

        flag = true;

        LogManager.writeLog("request : " + myIndex + " finish all downloading and request merging chunks");
        OutputStream out = new FileOutputStream(myPath + "merged_" + originalFileName);

        //System.out.println("totalChunk: " + totalChunk);
        for (int chunkIndex = 0; chunkIndex < totalChunk; chunkIndex++)
        {
            File file = new File(makeChunkPath(chunkIndex));
            FileInputStream file_reader = new FileInputStream(file);

            byte[] buf = new byte[divideSize + 5];
            int sz;

            sz = file_reader.read(buf, 0, divideSize + 5);
            out.write(buf, 0, sz);
            file_reader.close();
        }
        out.flush();
        out.close();

        System.out.println("Download Complete!");
    }

    public void divideFile(String fileName, int divideSize)
    {
        try
        {
            File file = new File(fileName);
            FileInputStream in = new FileInputStream(file);

            int readSize = 0;
            int totalSize = 0;
            int chunkIndex = 0;

            BufferedInputStream bin = new BufferedInputStream(in);
            byte[] byteArray = new byte[1024];

            File chunkFile = new File(makeChunkPath(0));
            FileOutputStream out = new FileOutputStream(chunkFile);

            while (true)
            {
                readSize = bin.read(byteArray);

                if (readSize == -1)
                    break;

                out.write(byteArray, 0, readSize);
                totalSize += readSize;

                if (totalSize % divideSize == 0)
                {
                    out.flush();
                    out.close();
                    File newChunkFile = new File(makeChunkPath(++chunkIndex));
                    out = new FileOutputStream(newChunkFile);
                }
            }
            in.close();
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    void setTotalChunk(int totalChunk)
    {
        this.totalChunk = totalChunk;
    }
}
