package cn.theodore.jvm;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: theodore
 */
public class Crawler {

    static ExecutorService cachedThreadPool = new ThreadPoolExecutor(100, 150, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    static Pattern pattern = Pattern.compile("https?://.*(?=\")");

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    protected Queue<String> urlQueue = new LinkedBlockingQueue<String>();
    protected Queue<String> visitedQueue = new LinkedBlockingQueue<String>();

    public void initialize() throws IOException {

        String netease = "http://www.163.com/";
        final List<String> urls = obtainInternetData(netease);
        urlQueue.addAll(urls);
    }

    protected List<String> obtainInternetData(String url) throws IOException {
        List<String> urls = new ArrayList<>();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String content = EntityUtils.toString(entity);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    final String uri = matcher.group(0);
                    urls.add(uri);
                    System.out.println("新增待访问uri -> " + uri);
                }
            }
            // ensure it is fully consumed
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        return urls;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        final Crawler crawler = new Crawler();
        crawler.initialize();
        final Queue<String> urlQueue = crawler.urlQueue;
        final Queue<String> visitedQueue = crawler.visitedQueue;
        while (true) {
            if (urlQueue.size() == 0) {
                Thread.sleep(1000);
            }

            String url = urlQueue.poll();
            if (!visitedQueue.contains(url)) {

                cachedThreadPool.execute(() -> {
                    List<String> internetData = null;
                    try {
                        internetData = crawler.obtainInternetData(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    urlQueue.addAll(internetData);
                    visitedQueue.offer(url);

                    System.out.println(Thread.currentThread().getName()
                            + " Priority:" + Thread.currentThread().getPriority() + " uri:[" + url + "]");

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

        }


    }

    static class Monekey implements Runnable {

        @Override
        public void run() {
            final Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            System.out.println(threadName + "开始执行...");
            for (int i = 0; true; i++) {
                try {
                    // threadName += "O";
                    if (i == 9) {
                        System.out.println("EndlessLoopThread:" + threadName);
                        i = 0;
                    }
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
