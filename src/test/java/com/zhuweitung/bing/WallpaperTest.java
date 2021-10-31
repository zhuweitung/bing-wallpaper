package com.zhuweitung.bing;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author zhuweitung
 * @create 2021/10/31 
 */
@Log4j2
public class WallpaperTest {

    /**
     * 将原先bing-wallpaper.md中的图片都下载到本地（4k）
     * 将md中的图片路径都修改为本地路径
     * @param
     * @return void
     * @author zhuweitung
     * @date 2021/10/31
     */
    @Test
    public void saveOldDataUrl() {

        try {
            List<Images> imagesList = FileUtils.readBing();
            for (Images image : imagesList) {
                if (StringUtils.isNotBlank(image.getUrl()) && StringUtils.isNotBlank(image.getDate())) {
                    String localFileName = String.format(Images.IMAGE_FORMAT, image.getDate());
                    log.info("downloading {} as {}", image.getUrl(), localFileName);
                    Wallpaper.downloadImageFromUrl(image.getUrl(), localFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将原先bing-wallpaper.md中的图片都下载到本地（1080）
     * 将md中的图片路径都修改为本地路径
     * @param
     * @return void
     * @author zhuweitung
     * @date 2021/10/31
     */
    @Test
    public void saveOldData1080Url() {

        try {
            List<Images> imagesList = FileUtils.readBing();
            for (Images image : imagesList) {
                if (StringUtils.isNotBlank(image.getUrl()) && StringUtils.isNotBlank(image.getDate())) {
                    String smallUrl = image.getUrl() + Images.IMAGE_URL_PARAMS_1080;
                    String localFileName = String.format(Images.IMAGE_FORMAT_1080, image.getDate());
                    log.info("downloading {} as {}", smallUrl, localFileName);
                    Wallpaper.downloadImageFromUrl(smallUrl, localFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将原先bing-wallpaper.md中的图片都下载到本地（小）
     * 将md中的图片路径都修改为本地路径
     * @param
     * @return void
     * @author zhuweitung
     * @date 2021/10/31
     */
    @Test
    public void saveOldDataSmallUrl() {

        try {
            List<Images> imagesList = FileUtils.readBing();
            for (Images image : imagesList) {
                if (StringUtils.isNotBlank(image.getUrl()) && StringUtils.isNotBlank(image.getDate())) {
                    String smallUrl = image.getUrl() + Images.IMAGE_URL_PARAMS_SMALL;
                    String localFileName = String.format(Images.IMAGE_FORMAT_SMALL, image.getDate());
                    log.info("downloading {} as {}", smallUrl, localFileName);
                    Wallpaper.downloadImageFromUrl(smallUrl, localFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}