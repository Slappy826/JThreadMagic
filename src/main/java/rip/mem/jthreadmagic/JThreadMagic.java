package rip.mem.jthreadmagic;

import java.io.*;
import java.nio.file.*;

public class JThreadMagic {

    public static final boolean LOADED;
    private static native void stopThread0(Thread thread, Object exception);

    static {
        boolean isLoaded = false;

        try {
            loadNativeLibrary();
            isLoaded = true;
        } catch (Exception ex) {
            System.err.println("Cannot load JThreadMagic:");
            ex.printStackTrace();
        } catch (UnsatisfiedLinkError ex) {
            System.err.println("Cannot load JThreadMagic native library:");
            ex.printStackTrace();
        }

        LOADED = isLoaded;
    }

    private static void loadNativeLibrary() throws IOException {
        String os = getOS();
        String arch = getArch();
        String libName = getLibraryName();
        
        // macOS uses a universal binary (fat binary), others are arch-specific
        String resourcePath = os.equals("macos") 
            ? "/natives/macos/" + libName
            : "/natives/" + os + "-" + arch + "/" + libName;
        
        InputStream in = JThreadMagic.class.getResourceAsStream(resourcePath);
        if (in == null) {
            // Fallback to system library path
            System.loadLibrary("jthreadmagic");
            return;
        }
        
        try {
            // Extract to temp file
            Path tempDir = Files.createTempDirectory("jthreadmagic");
            Path tempLib = tempDir.resolve(libName);
            Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
            
            // Make executable on Unix
            tempLib.toFile().setExecutable(true);
            
            // Mark for deletion on exit
            tempLib.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
            
            System.load(tempLib.toAbsolutePath().toString());
        } finally {
            in.close();
        }
    }

    private static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return "macos";
        } else if (os.contains("linux") || os.contains("nix") || os.contains("nux")) {
            return "linux";
        }
        throw new UnsupportedOperationException("Unsupported operating system: " + os);
    }

    private static String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            return "x64";
        } else if (arch.equals("x86") || arch.equals("i386") || arch.equals("i686")) {
            return "x86";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            return "arm64";
        }
        throw new UnsupportedOperationException("Unsupported architecture: " + arch);
    }

    private static String getLibraryName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "jthreadmagic.dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return "libjthreadmagic.dylib";
        } else {
            return "libjthreadmagic.so";
        }
    }

    public static void stopThread(Thread thread) {
        stopThread0(thread, new ThreadDeath());
    }

    public static void stopThread(Thread thread, Throwable exception) {
        stopThread0(thread, exception);
    }
}
