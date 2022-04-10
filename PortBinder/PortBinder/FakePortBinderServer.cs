using Google.Protobuf;
using Grpc.Core;
using GrpcProtocol;
using PortBinder.Utils;
using System;
using Xunit;

namespace PortBinder;

public class FakePortBinderServer : IDisposable
{
    private class PortBinderService : GrpcProtocol.PortBinder.PortBinderBase
    {
        public int? Port { get; private set; }
        public byte[]? Data { get; private set; }

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
                    switch (message.EventType)
                    {
                        case ClientEventType.DataTransfer:
                            Data = message.Data.ToArray();
                            break;
                    }
                }
            });
        }

        internal void NotifiesClientDisconnected(string clientId)
        {
            var command = new ClientEvent()
            {
                ClientId = clientId,
                EventType = ClientEventType.ClientDisconnected,
            };
            _responseStream.WriteAsync(command).Wait();
        }

        internal void NotifiesClientConnected(string clientId)
        {
            manual.WaitOne();
            var command = new ClientEvent()
            {
                ClientId = clientId,
                EventType = ClientEventType.ClientConnected,
            };
            _responseStream.WriteAsync(command).Wait();
        }

        internal void NotifiesClientSendData(string clientId, byte[] data)
        {
            var command = new ClientEvent()
            {
                ClientId = clientId,
                EventType = ClientEventType.DataTransfer,
                Data = ByteString.CopyFrom(data),
            };
            _responseStream.WriteAsync(command).Wait();
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

    public void NotifiesClientDisconnected(string clientId)
    {
        portBinderService.NotifiesClientDisconnected(clientId);
    }

    public void NotifiesClientConnected(string clientId)
    {
        portBinderService.NotifiesClientConnected(clientId);
    }

    public void NotifiesClientSendData(string clientId, byte[] data)
    {
        portBinderService.NotifiesClientSendData(clientId, data);
    }

    public void AgentPortRegisterd(int port)
    {
        Assert.Equal(port, portBinderService.Port);
    }

    public void ReceiveData(byte[] bytes)
    {
        Assert.True(TestTool.Polling(() => portBinderService.Data?.SequenceEqual(bytes) ?? false).Result);
    }
}
