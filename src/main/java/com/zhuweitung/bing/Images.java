package com.zhuweitung.bing;

import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Objects;

/**
 * @author niujinpeng
 * @date 2021/02/08
 * @link https://github.com/niumoo
 */
@Data
public class Images {

    /**
     * 图片保存格式：4k
     */
    public transient static final String IMAGE_FORMAT = "%s.jpg";
    /**
     * 图片保存格式：1080
     */
    public transient static final String IMAGE_FORMAT_1080 = "%s_1080.jpg";
    /**
     * 图片保存格式：small
     */
    public transient static final String IMAGE_FORMAT_SMALL = "%s_sm.jpg";

    /**
     * 图片原始地址参数：1080
     */
    public transient static final String IMAGE_URL_PARAMS_1080 = "&pid=hp&w=1920&h=1080&rs=1&c=4";
    /**
     * 图片原始地址参数：small
     */
    public transient static final String IMAGE_URL_PARAMS_SMALL = "&pid=hp&w=384&h=216&rs=1&c=4";

    /**
     * 壁纸本地保存根目录
     */
    public static final String WALLPAPER_URL = "wallpaper";
    /**
     * 壁纸本地保存目录绝对路径
     */
    public static final String WALLPAPER_SAVE_ROOT = System.getProperty("user.dir") + File.separator + WALLPAPER_URL;

    /**
     * 固定地址图片数
     */
    public static final Integer FIX_URL_IMAGE_NUM = 7;
    /**
     * 固定地址图片名称前缀
     */
    public static final String FIX_IMAGE_PREFIX = "day";

    private String desc;
    private String date;
    private String url;

    @Override
    public String toString() {
        String smallUrl = getRelativeUrl(IMAGE_FORMAT_SMALL);
        return String.format("![](%s)%s [download 4k](%s)", smallUrl, date, url);
    }

    public String formatMarkdown() {
        return String.format("%s | [%s](%s) ", date, desc, url);
    }

    public String toLarge() {
        return String.format("![](%s)Today: [%s](%s)", url, desc, url);
    }

    /**
     * 获取图片相对路径
     * @param format {@link #IMAGE_FORMAT,#IMAGE_FORMAT_1080 ,#IMAGE_SMALL_FORMAT}
     * @return java.lang.String
     * @author zhuweitung
     * @date 2021/10/31
     */
    public String getRelativeUrl(String format) {
        if (StringUtils.isNotBlank(date) && StringUtils.isNotBlank(format)) {
            return FilenameUtils.separatorsToUnix("./" + WALLPAPER_URL + File.separator + String.format(format, date));
        }
        return "";
    }

    /**
     * 获取图片绝对路径
     * @param format {@link #IMAGE_FORMAT,#IMAGE_FORMAT_1080 ,#IMAGE_SMALL_FORMAT}
     * @return java.lang.String
     * @author zhuweitung
     * @date 2021/10/31
     */
    public String getAbsoluteUrl(String format) {
        if (StringUtils.isNotBlank(date) && StringUtils.isNotBlank(format)) {
            return Images.WALLPAPER_SAVE_ROOT + File.separator + String.format(format, date);
        }
        return "";
    }

    public Images() {
    }

    public Images(String desc, String date) {
        this.desc = desc;
        this.date = date;
        this.url = getRelativeUrl(IMAGE_FORMAT);
    }

    public Images(String desc, String date, String url) {
        this.desc = desc;
        this.date = date;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Images images = (Images) o;
        return Objects.equals(desc, images.desc) && Objects.equals(date, images.date)
                && Objects.equals(url, images.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, date, url);
    }
}
