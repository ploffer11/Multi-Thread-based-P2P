public class PeerManager
{
    static boolean[] connectPeer = new boolean[5];
    public static synchronized boolean changePeer(int i, boolean diff)
    {
        if (connectPeer[i] == diff)
            return false;
        else
        {
            connectPeer[i] = diff;
            return true;
        }
    }
}
