using Moq;
using PortBinder.AgentSocket;
using PortBinder.Test.SpySocket;
using System;
using System.Text;
using System.Threading;
using Xunit;

namespace PortBinder.Test.AgentSocket;

public class AgentSocketManagerTests : IDisposable
{
    readonly string CLIENT_ID = Guid.NewGuid().ToString();

    readonly int ECHO_SERVER_PORT = RandomPortGenerator.GetNextPort();
    readonly int NOT_USED_PORT = RandomPortGenerator.GetNextPort();
    EchoServer server = new();

    public AgentSocketManagerTests()
    {
        _ = server.RunAsync(ECHO_SERVER_PORT);
    }

    public void Dispose()
    {
        server.Stop();
    }

    [Fact]
    public void ConnectToEchoServerAndSendHelloThenReceiveHello()
    {
        var data = "Hello\r\n";
        var bytes = Encoding.UTF8.GetBytes(data);
        var mock = new Mock<IAgentSocketEventListener>();
        AgentSocketManager manager = new(mock.Object, ECHO_SERVER_PORT);

        manager.CreateNewClient(CLIENT_ID);
        manager.SendData(CLIENT_ID, bytes);

        Thread.Sleep(1000); // 데이터를 받기까지 시간이 걸리므로 대기
        manager.DisconnectClient(CLIENT_ID);

        mock.Verify(x => x.OnConnected(CLIENT_ID), Times.Once);
        mock.Verify(x => x.OnReceiveData(CLIENT_ID,
            It.Is<byte[]>(o => data == Encoding.UTF8.GetString(o))), Times.Once);
        mock.Verify(x => x.OnDisconnected(CLIENT_ID), Times.Once);
    }

    [Fact]
    public void ConnectFailThenDisconnectedEventOccurs()
    {
        var mock = new Mock<IAgentSocketEventListener>();
        var manager = new AgentSocketManager(mock.Object, NOT_USED_PORT);

        manager.CreateNewClient(CLIENT_ID);
        Thread.Sleep(1000);
        
        mock.Verify(x => x.OnDisconnected(CLIENT_ID), Times.Once);
    }
}
