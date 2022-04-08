using Grpc.Core;
using GrpcProtocol;
using System;
using Xunit;

namespace PortBinder;

public class FakePortBinderServer : IDisposable
{
    private class PortBinderService : GrpcProtocol.PortBinder.PortBinderBase
    {
        public int? Port { get; private set; }

        public override Task<RegisterAgentResponse> RegisterAgent(RegisterAgentRequest request, ServerCallContext context)
        {
            Port = request.Port;
            return Task.FromResult(new RegisterAgentResponse() { Success = true });
        }
    }

    private Server server;
    private PortBinderService portBinderService = new();

    public void Dispose()
    {
        server.ShutdownAsync().Wait();
    }

    public void Start(int port)
    {
        server = new Server
        {
            Services = { GrpcProtocol.PortBinder.BindService(portBinderService) },
            Ports = { new ServerPort("localhost", port, ServerCredentials.Insecure) }
        };
        server.Start();

        Console.WriteLine("Greeter server listening on port " + port);
    }

    public void AgentPortRegisterd(int port)
    {
        Assert.Equal(port, portBinderService.Port);
    }
}
