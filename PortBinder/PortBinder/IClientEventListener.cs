namespace PortBinder;

public interface IClientEventListener
{
    void ClientConnected(string clientId);
    void ClientDiconnected(string clientId);
    void ClientDataSend(string clientId, byte[] data);
}
