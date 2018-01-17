package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */

    public void run(final ServerSocket socket, final PCDPFilesystem fs, final int ncores)
            throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {

            // TODO Delete this once you start working on your solution.
//            throw new UnsupportedOperationException();

            // TODO 1) Use socket.accept to get a Socket object
            Socket connection = socket.accept();

            Thread thread = new Thread(() -> {
                try {

                    InputStream input = connection.getInputStream();
                    OutputStream output = connection.getOutputStream();

                    /*
                     * TODO 2) Using Socket.getInputStream(), parse the received HTTP
                     * packet. In particular, we are interested in confirming this
                     * message is a GET and parsing out the path to the file we are
                     * GETing. Recall that for GET HTTP packets, the first line of the
                     * received packet will look something like:
                     *
                     *     GET /path/to/file HTTP/1.1
                     */
                    String filename = parseFileName(input);
                    if (filename == null) {
                        replyHttp404(output);
                    } else {
                        serveFilename(filename, fs, output);
                    }

                    /*
                     * TODO 3) Using the parsed path to the target file, construct an
                     * HTTP reply and write it to Socket.getOutputStream(). If the file
                     * exists, the HTTP reply should be formatted as follows:
                     *
                     *   HTTP/1.0 200 OK\r\n
                     *   Server: FileServer\r\n
                     *   \r\n
                     *   FILE CONTENTS HERE\r\n
                     *
                     * If the specified file does not exist, you should return a reply
                     * with an error code 404 Not Found. This reply should be formatted
                     * as:
                     *
                     *   HTTP/1.0 404 Not Found\r\n
                     *   Server: FileServer\r\n
                     *   \r\n
                     *
                     * Don't forget to close the output stream.
                     */
                    output.flush();
                    output.close();
                    connection.close();
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }

            });
            thread.start();

        }
    }

    private void serveFilename(String filename, final PCDPFilesystem fs, OutputStream output) {
        final String contents = fs.readFile(new PCDPPath(filename));
        if (null == contents) {
            replyHttp404(output);
        } else {
            final PrintStream printer = new PrintStream(output);

            printer.print("HTTP/1.0 200 OK\r\n");
            printer.print("Server: FileServer\r\n");
            printer.print("Content-Length: " + contents.length() + "\r\n");
            printer.print("\r\n");
            printer.print(contents);
            printer.print("\r\n");

            printer.flush();
            printer.close();
        }
    }

    private void replyHttp404(OutputStream output) {
        final PrintStream printer =  new PrintStream(output);

        printer.print("HTTP/1.0 404 Not Found\r\n");
        printer.print("Server: FileServer\r\n");
        printer.print("\r\n");

        printer.close();
    }

    private String parseFileName(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter("\\r\\n");
        String line = scanner.next();

        Pattern pattern = Pattern.compile("GET (.+) HTTP/\\d.\\d");
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

}
