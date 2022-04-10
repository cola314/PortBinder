namespace PortBinder.Utils;

public class TestTool
{
    public static async Task<bool> Polling(Func<bool> predicate, int millis = 1000)
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
}
