using PortBinder.Utils;
using Xunit;

namespace PortBinder;

public class Application
{
    private ClientEventListener _clientEventListener;
    private PortBinderServer _server;

    public Application()
    {
        _clientEventListener = new ClientEventListener();
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

    private class ClientEventListener : IClientEventListener
    {
        public bool IsClientConnected { get; private set; }
        public bool IsClientDisconnected { get; private set; }
        public bool IsClientSendData { get; private set; }

        public void ClientConnected()
        {
            IsClientConnected = true;
        }

        public void ClientDiconnected()
        {
            IsClientDisconnected = true;
        }

        public void ClientDataSend()
        {
            IsClientSendData = true;
        }
    }
}
