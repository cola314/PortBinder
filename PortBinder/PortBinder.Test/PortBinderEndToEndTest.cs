using System;
using Xunit;

namespace PortBinder.Test;

public class PortBinderEndToEndTest : IDisposable
{
    const int SERVER_PORT = 34567;
    readonly string SERVER_ADDRESS = $"localhost:{SERVER_PORT}";
    FakePortBinderServer server = new();
    Application app = new();

    public void Dispose()
    {
        server.Dispose();
    }

    [Fact]
    public void RegisterPortAndNoClientConnected()
    {
        server.Start(SERVER_PORT);

        app.ConnectToServer(SERVER_ADDRESS);

        app.RegisterPort(1234);
        server.AgentPortRegisterd(1234);

        app.Close();
    }
}
