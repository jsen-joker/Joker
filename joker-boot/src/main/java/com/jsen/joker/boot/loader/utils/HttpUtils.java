package com.jsen.joker.boot.loader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>
 *     发送stop命令到joker-core-plugin-manager
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class HttpUtils {
    /**
     * 发送http get请求
     *
     * @param getUrl
     * @return
     */
    public static String sendGetRequest(String getUrl) throws IOException {
        try {
            URL url = new URL(getUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream())) ) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IOException e) {
                throw e;
            }
        } catch (IOException e) {
            throw e;
        }
    }

}
