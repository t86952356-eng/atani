import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import net.minecraft.client.main.Main;

public class Start
{
    public static void main(String[] args)
    {
        // LWJGL natives are no longer shipped in this repository (26MB of dev-only files).
        // If a local "natives" folder exists it will be used, otherwise LWJGL falls back
        // to java.library.path. Override the location with -Dnatives.path=...
        final Path natives = Path.of(System.getProperty("natives.path", "../natives/")).resolve(System.getProperty("os.name").startsWith("Windows") ? "windows" : "linux");

        if (Files.isDirectory(natives))
        {
            System.setProperty("org.lwjgl.librarypath", natives.toAbsolutePath().toString());
        }

        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
