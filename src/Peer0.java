import java.io.*;

public class Peer0
{
    // peer's index
    static int myIndex = 0;

    static String[] hostArray;
    static int[] portArray;
    static int divideSize = 10240;

    public static void main(String[] args)
    {
        try
        {
            /*
            configuration file(configuration.txt) form is below
            host0 port0
            host1 port1
            host2 port2
            host3 port3
            host4 port4
            /path/filename.file
            */

            hostArray = new String[5];
            portArray = new int[5];

            File file = new File("./configuration.txt");
            BufferedReader in = new BufferedReader(new FileReader(file));

            for (int i = 0; i < 5; i++)
            {
                String temp = in.readLine().trim();
                String[] tempArray = temp.split(" ");

                hostArray[i] = tempArray[0];
                portArray[i] = Integer.parseInt(tempArray[1]);
            }

            String filePath = in.readLine().trim();
            String[] tempStringArray = filePath.split("/");
            String originalFileName = tempStringArray[tempStringArray.length - 1];

            file = new File(filePath);

            long size = file.length();
            int totalChunk = (int) Math.ceil((double) size / divideSize);

            FileManager fileManager = new FileManager(myIndex, totalChunk);
            ChunkManager chunkManager = new ChunkManager(totalChunk);

            // Peer 0 은 모든 파일을 가지고 있다.
            if (myIndex == 0)
            {
                fileManager.divideFile(filePath, divideSize);
                for (int i = 0; i < totalChunk; i++)
                    chunkManager.downloadComplete(i);
            }

            Upload upload = new Upload(hostArray[myIndex], portArray[myIndex], chunkManager, fileManager);
            Download[] downloadArray = new Download[3];
            for (int i = 0; i < 3; i++)
            {
                downloadArray[i] = new Download(myIndex, totalChunk, portArray, hostArray,
                        chunkManager, fileManager, originalFileName);
            }

            // Thread start
            upload.start();
            for (int i=0; i<3; i++)
                downloadArray[i].start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
