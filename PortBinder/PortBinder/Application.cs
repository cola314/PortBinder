using PortBinder.AgentSocket;
using PortBinder.Utils;
using Xunit;

namespace PortBinder;

public class Application : IAgentSocketEventListener
{
    private ClientEventListener _clientEventListener;
    private PortBinderServer _server;

    public Application(int localServerPort)
    {
        var socketManager = new AgentSocketManager(this, localServerPort);
        _clientEventListener = new ClientEventListener(socketManager);
        _server = new PortBinderServer(_clientEventListener);
    }

    public void ConnectToServer(string serverAddress)
    {
        _server.ConnectToServer(serverAddress);
    }

    public void Close()
    {
        _server.Close();
    }

    public void RegisterPort(int port)
    {
        _server.RegisterPort(port);
    }

    public void ClientConnected()
    {
        Assert.True(TestTool.Polling(() => _clientEventListener.IsClientConnected).Result);
    }

    public void ClientDisconnected()
    {
        Assert.True(TestTool.Polling(() => _clientEventListener.IsClientDisconnected).Result);
    }

    public void ClientSendData()
    {
        Assert.True(TestTool.Polling(() => _clientEventListener.IsClientSendData).Result);
    }

    public void OnConnected(string clientId)
    {
    }

    public void OnReceiveData(string clientId, byte[] data)
    {
        _server.SendDataAsync(clientId, data).Wait();
    }

    public void OnDisconnected(string clientId)
    {
    }

    private class ClientEventListener : IClientEventListener
    {
        private AgentSocketManager _manager;

        public ClientEventListener(AgentSocketManager manager)
        {
            this._manager = manager;
        }

        public bool IsClientConnected { get; private set; }
        public bool IsClientDisconnected { get; private set; }
        public bool IsClientSendData { get; private set; }

        public void ClientConnected(string clientId)
        {
            IsClientConnected = true;
            _manager.CreateNewClient(clientId);
        }

        public void ClientDiconnected(string clientId)
        {
            IsClientDisconnected = true;
            _manager.DisconnectClient(clientId);
        }

        public void ClientDataSend(string clientId, byte[] data)
        {
            IsClientSendData = true;
            _manager.SendData(clientId, data);
        }
    }
}
