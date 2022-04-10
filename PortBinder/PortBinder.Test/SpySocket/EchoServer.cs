using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;

namespace PortBinder.Test.SpySocket;

public class EchoServer
{
    private int ASSERT_TIMEOUT = 2000;
    private ManualResetEvent manual = new ManualResetEvent(false);
    private CancellationTokenSource tokenSource = new CancellationTokenSource();
    private TcpListener listener;

    public async Task RunAsync(int port)
    {
        listener = new TcpListener(IPAddress.Loopback, port);
        listener.Start();

        while (!tokenSource.IsCancellationRequested)
        {
            var client = await listener.AcceptTcpClientAsync().ConfigureAwait(false);
            ClientConnected(client);
        }
    }

    private void ClientConnected(TcpClient client)
    {
        _ = Task.Run(async () =>
        {
            using var stream = client.GetStream();
            using var reader = new StreamReader(stream);
            using var writer = new StreamWriter(stream)
            {
                AutoFlush = true
            };

            while (client.Connected && !tokenSource.IsCancellationRequested)
            {
                var line = await reader.ReadLineAsync();
                await writer.WriteLineAsync(line);
            }
        });
    }

    public void Stop()
    {
        tokenSource.Cancel();
        listener.Stop();
    }
}
