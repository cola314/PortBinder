namespace PortBinder;

public interface IClientEventListener
{
    void ClientConnected();
    void ClientDiconnected();
    void ClientDataSend();
}
