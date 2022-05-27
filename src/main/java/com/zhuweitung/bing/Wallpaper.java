package com.zhuweitung.bing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tinify.Tinify;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.imageio.ImageIO;

/**
 * @author niujinpeng
 * @date 2021/02/08
 * @link https://github.com/niumoo
 */
@Log4j2
public class Wallpaper {

    // BING API
    private static final String BING_API = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=10&nc=1612409408851&pid=hp&FORMBEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160";

    private static final String BING_URL = "https://cn.bing.com";

    public static void main(String[] args) throws IOException, ParseException {

        String tinifyApiKey = null;
        if (args.length > 0) {
            tinifyApiKey = args[0];
        }

        String httpContent = HttpUtls.getHttpContent(BING_API);
        JSONObject jsonObject = JSON.parseObject(httpContent);
        JSONArray jsonArray = jsonObject.getJSONArray("images");

        jsonObject = (JSONObject) jsonArray.get(0);
        // 图片地址
        String url = BING_URL + (String) jsonObject.get("url");
        url = url.substring(0, url.indexOf("&"));

        // 图片时间
        String enddate = (String) jsonObject.get("enddate");
        LocalDate localDate = LocalDate.parse(enddate, DateTimeFormatter.BASIC_ISO_DATE);
        Date today = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        enddate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("enddate:{}, today:{}", enddate, today.toString());

        // 图片版权
        String copyright = (String) jsonObject.get("copyright");

        // 保存图片到本地
        String localFileName = String.format(Images.IMAGE_FORMAT, enddate);
        downloadImageFromUrl(url, localFileName);
        // 保存1080图片到本地
        String local1080FileName = String.format(Images.IMAGE_FORMAT_1080, enddate);
        downloadImageFromUrl(url + Images.IMAGE_URL_PARAMS_1080, local1080FileName);
        // 保存small图片到本地
        String localSmallFileName = String.format(Images.IMAGE_FORMAT_SMALL, enddate);
        downloadImageFromUrl(url + Images.IMAGE_URL_PARAMS_SMALL, localSmallFileName);

        List<Images> imagesList = FileUtils.readBing();
        Map<String, Images> imagesMap = imagesList.stream().collect(Collectors.toMap(Images::getDate, image -> image));
        if (imagesMap.get(enddate) == null) {
            imagesList.add(0, new Images(copyright, enddate));
            imagesMap = imagesList.stream().collect(Collectors.toMap(Images::getDate, image -> image));
        }

        /*// 将4k图片路径改为本地路径
        for (Images image : imagesList) {
            if (StringUtils.isNotBlank(image.getUrl()) && StringUtils.isNotBlank(image.getDate())) {
                image.setUrl(image.getRelativeUrl(Images.IMAGE_FORMAT));
                log.info("本地图片路径：{}", image.getUrl());
            }
        }*/

        // 删除30天前的壁纸
        List<String> imageFormats = Arrays.asList(Images.IMAGE_FORMAT, Images.IMAGE_FORMAT_1080, Images.IMAGE_FORMAT_SMALL);
        Calendar cld = Calendar.getInstance();
        cld.setTime(today);
        cld.add(Calendar.DAY_OF_MONTH, -30);
        Iterator<Images> imagesIterator = imagesList.iterator();
        while (imagesIterator.hasNext()) {
            Images image = imagesIterator.next();
            if (StringUtils.isBlank(image.getDate())) {
                continue;
            }
            Date date = DateUtils.parseDate(image.getDate(), "yyyy-MM-dd");
            if (DateUtils.truncatedCompareTo(cld.getTime(), date, Calendar.DAY_OF_MONTH) > 0) {
                // 删除本地图片
                for (String imageFormat : imageFormats) {
                    org.apache.commons.io.FileUtils.delete(new File(image.getAbsoluteUrl(imageFormat)));
                }
                log.info("删除{}的本地图片", image.getDate());
                imagesIterator.remove();
            }
        }

        // 压缩新增图片
        if (StringUtils.isNotBlank(tinifyApiKey) && imagesMap.get(enddate) != null) {
            Tinify.setKey(tinifyApiKey);
            Images image = imagesMap.get(enddate);
            for (String imageFormat : imageFormats) {
                try {
                    long compressStartTime = System.currentTimeMillis();
                    Tinify.fromFile(image.getAbsoluteUrl(imageFormat)).toFile(image.getAbsoluteUrl(imageFormat));
                    log.info("压缩图片{}, 耗时: {}ms", image.getAbsoluteUrl(imageFormat), System.currentTimeMillis() - compressStartTime);
                } catch (Exception e) {
                    log.error("图片压缩失败，路径={}", image.getAbsoluteUrl(imageFormat), e.getMessage());
                }
            }
        }

        // 替换固定地址图片
        cld.setTime(today);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < Images.FIX_URL_IMAGE_NUM; i++) {
            String date = dateFormat.format(cld.getTime());
            Images image = imagesMap.get(date);
            if (image != null) {
                for (String imageFormat : imageFormats) {
                    File srcFile = new File(image.getAbsoluteUrl(imageFormat));
                    File destFile = new File(Images.WALLPAPER_SAVE_ROOT + File.separator + String.format(imageFormat, Images.FIX_IMAGE_PREFIX + i));
                    org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
                    log.info("将 {} 复制到 {}", srcFile.getAbsoluteFile(), destFile.getAbsoluteFile());
                }
            }
            cld.add(Calendar.DAY_OF_MONTH, -1);
        }

        imagesList = imagesList.stream().distinct().collect(Collectors.toList());
        FileUtils.writeBing(imagesList);
        FileUtils.writeReadme(imagesList);

    }

    /**
     * 下载远程图片
     * @param originalUrl 图片地址
     * @param fileName 保存图片文件名
     * @return java.awt.image.BufferedImage
     * @author zhuweitung
     * @date 2021/10/31
     */
    public static boolean downloadImageFromUrl(String originalUrl, String fileName) {
        // 图片本地路径
        String localUrl = Images.WALLPAPER_SAVE_ROOT + File.separator + fileName;

        File file = new File(localUrl);
        // 判断是否已下载
        if (file.exists()) {
            return true;
        }
        HttpURLConnection conn = null;
        try {
            URL url = new URL(originalUrl);
            conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == 200) {
                BufferedImage image = ImageIO.read(conn.getInputStream());
                ImageIO.write(image, "jpg", file);
                log.info("download {} as {}", originalUrl, localUrl);
                return true;
            }
        } catch (Exception e) {
            log.error("获取网络图片出错, url={}, ", originalUrl, e.getMessage());
        } finally {
            conn.disconnect();
        }
        return false;
    }

}
