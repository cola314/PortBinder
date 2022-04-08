using Grpc.Core;
using GrpcProtocol;
using static GrpcProtocol.PortBinder;

namespace PortBinder;

public class Application
{
    private Channel channel;
    private PortBinderClient client;

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
        channel.ShutdownAsync().Wait();
    }

    public void RegisterPort(int port)
    {
        var result = client.RegisterAgent(new RegisterAgentRequest() { Port = port });
    }
}
