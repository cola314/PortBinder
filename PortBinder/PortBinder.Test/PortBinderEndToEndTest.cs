using PortBinder.Test.SpySocket;
using System;
using System.Text;
using Xunit;

namespace PortBinder.Test;

public class PortBinderEndToEndTest : IDisposable
{
    string CLIENT_ID = Guid.NewGuid().ToString();
    readonly int SERVER_PORT = RandomPortGenerator.GetNextPort();
    readonly int LOCAL_SERVER_PORT = RandomPortGenerator.GetNextPort();
    string SERVER_ADDRESS => $"localhost:{SERVER_PORT}";
    FakePortBinderServer server = new();
    Application app;
    EchoServer localServer = new();

    public PortBinderEndToEndTest()
    {
        localServer = new();
        _ = localServer.RunAsync(LOCAL_SERVER_PORT);
        app = new(LOCAL_SERVER_PORT);
        server.Start(SERVER_PORT);
        app.ConnectToServer(SERVER_ADDRESS);
    }

    public void Dispose()
    {
        localServer.Stop();
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

        server.NotifiesClientConnected(CLIENT_ID);
        app.ClientConnected();
        server.NotifiesClientDisconnected(CLIENT_ID);
        app.ClientDisconnected();
    }

    [Fact]
    public void ClientSendHelloAndReceiveHelloFromLocalEchoServer()
    {
        var bytes = Encoding.UTF8.GetBytes("Hello\r\n");

        app.RegisterPort(1234);
        server.AgentPortRegisterd(1234);

        server.NotifiesClientConnected(CLIENT_ID);
        app.ClientConnected();

        server.NotifiesClientSendData(CLIENT_ID, bytes);
        app.ClientSendData();

        server.ReceiveData(bytes);

        server.NotifiesClientDisconnected(CLIENT_ID);
        app.ClientDisconnected();
    }
}
