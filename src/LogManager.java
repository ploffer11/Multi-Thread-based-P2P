import java.io.FileOutputStream;
import java.io.OutputStream;

public class LogManager
{
    public static synchronized void writeLog(String s)
    {
        try
        {
            OutputStream out = new FileOutputStream("./log.txt", true);
            out.write((s + "\n").getBytes());
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
