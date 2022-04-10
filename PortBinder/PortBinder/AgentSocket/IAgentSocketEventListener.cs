namespace PortBinder.AgentSocket;

public interface IAgentSocketEventListener
{
    void OnConnected(string clientId);
    void OnReceiveData(string clientId, byte[] data);
    void OnDisconnected(string clientId);
}
