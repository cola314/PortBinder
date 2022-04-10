using Google.Protobuf;
using Grpc.Core;
using GrpcProtocol;
using static GrpcProtocol.PortBinder;

namespace PortBinder;

public class PortBinderServer
{
    private Channel channel;
    private PortBinderClient client;
    private AsyncDuplexStreamingCall<ClientEvent, ClientEvent> _call;
    private IClientEventListener _listener;

    public PortBinderServer(IClientEventListener listener)
    {
        _listener = listener;
    }

    public void ConnectToServer(string serverAddress)
    {
        channel = new Channel(serverAddress, ChannelCredentials.Insecure);

        client = new GrpcProtocol.PortBinder.PortBinderClient(channel);
    }

    public void Close()
    {
        _call.RequestStream.CompleteAsync().Wait();
        _call.Dispose();
        channel.ShutdownAsync().Wait();
    }

    public void RegisterPort(int port)
    {
        var result = client.RegisterAgent(new RegisterAgentRequest() { Port = port });

        _call = client.StreamingClientEvent();
        var readTask = Task.Run(async () =>
        {
            await foreach (var response in _call.ResponseStream.ReadAllAsync())
            {
                switch (response.EventType)
                {
                    case ClientEventType.ClientConnected:
                        _listener.ClientConnected(response.ClientId);
                        break;
                    case ClientEventType.ClientDisconnected:
                        _listener.ClientDiconnected(response.ClientId);
                        break;
                    case ClientEventType.DataTransfer:
                        _listener.ClientDataSend(response.ClientId, response.Data.ToArray());
                        break;
                }
            }
        });
    }

    public Task SendDataAsync(string clientId, byte[] data)
    {
        var command = new ClientEvent()
        {
            ClientId = clientId,
            EventType = ClientEventType.DataTransfer,
            Data = ByteString.CopyFrom(data)
        };
        return _call.RequestStream.WriteAsync(command);
    }
}
