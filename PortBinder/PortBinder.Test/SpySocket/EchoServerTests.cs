using System;
using System.IO;
using System.Net.Sockets;
using Xunit;

namespace PortBinder.Test.SpySocket;

public class EchoServerTests : IDisposable
{
    private EchoServer server;
    private int PORT = 1616;

    public EchoServerTests()
    {
        server = new EchoServer();
        _ = server.RunAsync(PORT);
    }

    public void Dispose()
    {
        server.Stop();
    }

    [Fact]
    public void SendHelloAndReceiveHello()
    {
        using var client = new TcpClient();
        client.Connect("localhost", PORT);

        using var stream = client.GetStream();
        using var writer = new StreamWriter(stream) { AutoFlush = true };
        writer.WriteLine("Hello");

        using var reader = new StreamReader(stream);
        var result = reader.ReadLine();

        Assert.Equal("Hello", result);
    }
}
