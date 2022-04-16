using System.Net.Sockets;

namespace PortBinder.AgentSocket;

public class AgentSocketManager
{
    private object _lock = new object();
    private Dictionary<string, Client> _clients = new();
    private readonly int LOCAL_SERVER_PORT;
    private IAgentSocketEventListener? _listener;

    public AgentSocketManager(IAgentSocketEventListener? listener, int localServerPort)
    {
        this._listener = listener;
        this.LOCAL_SERVER_PORT = localServerPort;
    }

    public void CreateNewClient(string clientId)
    {
        Client client;
        lock (_lock)
        {
            client = new(clientId, LOCAL_SERVER_PORT);
            _clients.Add(clientId, client);
        }

        client.Connected += (_, _) =>
        {
            _listener?.OnConnected(clientId);
        };
        client.Disconnected += (_, _) =>
        {
            _listener?.OnDisconnected(clientId);
        };
        client.DataReceived += (_, data) =>
        {
            _listener?.OnReceiveData(clientId, data);
        };

        client.Connect();
    }

    public void SendData(string clientId, byte[] data)
    {
        Client client;
        lock (_lock)
        {
            client = _clients[clientId];
        }
        client.SendData(data);
    }

    public void DisconnectClient(string clientId)
    {
        Client client;
        lock (_lock)
        {
            client = _clients[clientId];
            _clients.Remove(clientId);
        }
        client.Disconnect();
    }

    private class Client
    {
        private TcpClient _tcpClient;
        private byte[] buffer = new byte[1024];

        public string ClientId { get; }
        public int ServerPort { get; }

        public Client(string clientId, int serverPort)
        {
            ClientId = clientId;
            ServerPort = serverPort;
        }

        public event EventHandler Connected;
        public event EventHandler Disconnected;
        public event EventHandler<byte[]> DataReceived;

        public void Connect()
        {
            try
            {
                _tcpClient = new TcpClient("localhost", ServerPort);
            }
            catch (Exception ex)
            {
                Disconnected?.Invoke(this, EventArgs.Empty);
                return;
            }
            Connected?.Invoke(this, EventArgs.Empty);

            _ = Task.Run(async () =>
            {
                var stream = _tcpClient.GetStream();
                while (_tcpClient.Connected)
                {
                    var readCount = await stream.ReadAsync(buffer, 0, buffer.Length);
                    if (readCount > 0)
                    {
                        var data = new byte[readCount];
                        Array.Copy(buffer, 0, data, 0, readCount);
                        DataReceived?.Invoke(this, data);
                    }
                }
            });
        }

        public void Disconnect()
        {
            _tcpClient.Close();
            Disconnected?.Invoke(this, EventArgs.Empty);
        }

        public void SendData(byte[] data)
        {
            var stream = _tcpClient.GetStream();
            stream.Write(data, 0, data.Length);
        }
    }
}
