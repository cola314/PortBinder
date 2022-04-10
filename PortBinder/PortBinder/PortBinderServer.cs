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
                        _listener.ClientConnected();
                        break;
                    case ClientEventType.ClientDisconnected:
                        _listener.ClientDiconnected();
                        break;
                    case ClientEventType.DataTransfer:
                        _listener.ClientDataSend();
                        break;
                }
            }
        });
    }
}
