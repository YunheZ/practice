package cn.theodore.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ChatClient {

    private Selector selector;
    private SocketChannel socketChannel;
    private final String IP = "127.0.0.1";
    private final int PORT = 6666;
    private final InetSocketAddress inetSocketAddress = new InetSocketAddress(IP, PORT);

    public ChatClient() {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.bind(inetSocketAddress);
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws IOException, InterruptedException {
        if (!socketChannel.connect(inetSocketAddress)) {
            int retry = 5;
            while (!socketChannel.finishConnect() && retry > 0) {
                Thread.sleep(1000);
                System.out.println("connecting...");
                retry--;
            }
            if (retry < 5) {
                System.out.println("connecting failed.");
            }
        }
    }

    public void send(String message) throws IOException {
        byte[] content = message.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length);
        socketChannel.write(byteBuffer);
    }

    public void close() throws IOException{
        if (socketChannel != null) {
            socketChannel.close();
        }
        if (selector != null) {
            selector.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient chatClient = new ChatClient();
        chatClient.connect();
        chatClient.send("你好，维尼玛希~");
        chatClient.close();
    }


}
