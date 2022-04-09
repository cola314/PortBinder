using Grpc.Core;
using GrpcProtocol;
using Xunit;
using static GrpcProtocol.PortBinder;

namespace PortBinder;

public class Application
{
    private Channel channel;
    private PortBinderClient client;
    private AsyncDuplexStreamingCall<ClientEvent, ClientEvent> _call;
    private bool _clientConnected;
    private bool _clientDisconnected;

    public void Run()
    {

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
                        _clientConnected = true;
                        break;
                    case ClientEventType.ClientDisconnected:
                        _clientDisconnected = true;
                        break;
                }
            }
        });
    }

    private async Task<bool> Polling(Func<bool> predicate, int millis = 1000)
    {
        var start = DateTime.Now;
        var timeout = TimeSpan.FromMilliseconds(millis);
        while (DateTime.Now - start <= timeout)
        {
            if (predicate())
                return true;

            await Task.Delay(10);
        }
        return false;
    }

    public void ClientConnected()
    {
        Assert.True(Polling(() => _clientConnected).Result);
    }

    public void ClientDisconnected()
    {
        Assert.True(Polling(() => _clientDisconnected).Result);
    }
}
