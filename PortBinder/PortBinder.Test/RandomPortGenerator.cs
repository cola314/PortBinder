using System.Threading;

namespace PortBinder.Test;

/// <summary>
/// 여러 테스트에 사용되는 포트들을 겹치지 않게 하기 위해서 임의의 포트를 반환하는 객체
/// Thread Safe가 보장됨
/// </summary>
internal class RandomPortGenerator
{
    public const int START_PORT = 12222;

    private static int _randomPort = START_PORT;

    /// <summary>
    /// START_PORT 부터 1씩 늘어나는 포트를 반환
    /// </summary>
    public static int GetNextPort() => Interlocked.Increment(ref _randomPort);
}
