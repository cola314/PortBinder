using System;
using Xunit;

namespace PortBinder.Test;

public class PortBinderEndToEndTest : IDisposable
{
    readonly int SERVER_PORT = RandomPortGenerator.GetNextPort();
    string SERVER_ADDRESS => $"localhost:{SERVER_PORT}";
    FakePortBinderServer server = new();
    Application app = new();

    public PortBinderEndToEndTest()
    {
        server.Start(SERVER_PORT);
        app.ConnectToServer(SERVER_ADDRESS);
    }

    public void Dispose()
    {
        app.Close();
        server.Dispose();
    }

    [Fact]
    public void RegisterPortAndNoClientConnected()
    {
        app.RegisterPort(1234);
        server.AgentPortRegisterd(1234);
    }

    [Fact]
    public void ClientConnectedAndimmediatelyDisconnect()
    {
        app.RegisterPort(1234);
        server.AgentPortRegisterd(1234);

        server.NotifiesClientConnected();
        app.ClientConnected();
        server.NotifiesClientDisconnected();
        app.ClientDisconnected();
    }

    [Fact]
    public void ClientConnectedAndDisconnectedAfterSendData()
    {
        app.RegisterPort(1234);
        server.AgentPortRegisterd(1234);

        server.NotifiesClientConnected();
        app.ClientConnected();

        server.NotifiesClientSendData();
        app.ClientSendData();

        server.NotifiesClientDisconnected();
        app.ClientDisconnected();
    }
}
