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

        private IServerStreamWriter<ClientEvent> _responseStream;
        private ManualResetEvent manual = new ManualResetEvent(false);

        public override Task<RegisterAgentResponse> RegisterAgent(RegisterAgentRequest request, ServerCallContext context)
        {
            Port = request.Port;
            return Task.FromResult(new RegisterAgentResponse() { Success = true });
        }

        public override Task StreamingClientEvent(IAsyncStreamReader<ClientEvent> requestStream, IServerStreamWriter<ClientEvent> responseStream, ServerCallContext context)
        {
            manual.Set();
            _responseStream = responseStream;

            return Task.Run(async () =>
            {
                await foreach(var message in requestStream.ReadAllAsync())
                {

                }
            });
        }

        internal void NotifiesClientDisconnected()
        {
            _responseStream.WriteAsync(new ClientEvent() { EventType = ClientEventType.ClientDisconnected }).Wait();
        }

        internal void NotifiesClientConnected()
        {
            manual.WaitOne();
            _responseStream.WriteAsync(new ClientEvent() { EventType = ClientEventType.ClientConnected }).Wait();
        }

        internal void NotifiesClientSendData()
        {
            _responseStream.WriteAsync(new ClientEvent() { EventType = ClientEventType.DataTransfer }).Wait();
        }
    }

    private Server server;
    private PortBinderService portBinderService = new();

    public void Dispose()
    {
        server.KillAsync().Wait();
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

    public void NotifiesClientDisconnected()
    {
        portBinderService.NotifiesClientDisconnected();
    }

    public void NotifiesClientConnected()
    {
        portBinderService.NotifiesClientConnected();
    }

    public void NotifiesClientSendData()
    {
        portBinderService.NotifiesClientSendData();
    }

    public void AgentPortRegisterd(int port)
    {
        Assert.Equal(port, portBinderService.Port);
    }
}
