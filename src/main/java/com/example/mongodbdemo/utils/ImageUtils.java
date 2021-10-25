package com.example.mongodbdemo.utils;

import com.example.mongodbdemo.content.CommonContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片处理相关的工具类
 *
 * @author Minghao Wang <wangmh@situdata.com>
 */
@Slf4j
public class ImageUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static String sysdate;

    /**
     * convert bufferedImage to opencv mat
     *
     * @param image bufferedImage
     * @return opencv mat
     */
    public static Mat bufferedImage2Mat(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        Mat src = new Mat(new Size(width, height), CvType.CV_8UC3);
        byte[] rgbdata = new byte[width * height * 3];
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, image.getWidth());
        int index = 0, c = 0;
        int r = 0, g = 0, b = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                c = pixels[index];
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = c & 0xff;

                index = row * width * 3 + col * 3;
                rgbdata[index] = (byte) b;
                rgbdata[index + 1] = (byte) g;
                rgbdata[index + 2] = (byte) r;
            }
        }
        src.put(0, 0, rgbdata);
        return src;
    }

    /**
     * Mat转换成BufferedImage
     *
     * @param mat           要转换的Mat
     * @param fileExtension 格式为 ".jpg", ".png", etc
     * @return
     */
    public static BufferedImage mat2BufferedImage(Mat mat, String fileExtension) {
        MatOfByte mob = new MatOfByte();
        Highgui.imencode(fileExtension, mat, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            log.info("[ImageUtils] mat2BufferedImage failed.");
        }
        return bufImage;
    }


    /**
     * base 64 image string to base64string
     *
     * @param base64string base 64 encoded image string
     * @return bufferedImage
     */
    public static BufferedImage base64String2BufferedImage(String base64string) {
        BufferedImage image = null;
        try {
            InputStream stream = BaseToInputStream(base64string);
            image = ImageIO.read(stream);
        } catch (IOException e) {
            log.info("[ImageUtils] GetBufferedImage failed.");
        }
        return image;
    }

    /**
     * base 64 image string to mat
     *
     * @param base64string base 64 encoded image string
     * @return mat
     */
    public static Mat base64String2Mat(String base64string) {
        BufferedImage image = base64String2BufferedImage(base64string);
        return bufferedImage2Mat(image);
    }

    public static InputStream BaseToInputStream(String base64string) {
        ByteArrayInputStream stream = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
            log.info("[ImageUtils] BaseToInputStream failed.");
        }
        return stream;
    }

    /**
     * resize
     *
     * @param image     src image
     * @param maxWidth  target width
     * @param maxHeight target height
     * @return result image
     */
    public static BufferedImage resize(BufferedImage image, int maxWidth, int maxHeight) {
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();
        double scaleRate = Math.min(1.0 * maxWidth / srcWidth, 1.0 * maxHeight / srcHeight);
        scaleRate = Math.min(1.0, scaleRate);
        Integer targetWidth = (int) (srcWidth * scaleRate);
        Integer targetHeight = (int) (srcHeight * scaleRate);
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return scaledImage;
    }

    /**
     * resize mat
     *
     * @param image     src image
     * @param maxWidth  target width
     * @param maxHeight target height
     * @return result image
     */
    public static Mat resize(Mat image, int maxWidth, int maxHeight) {
        int srcWidth = image.width();
        int srcHeight = image.height();
        int dstWidth;
        int dstHeight;
        if (srcWidth > srcHeight) {
            dstWidth = maxWidth;
            dstHeight = (int) Math.round((double) srcHeight / srcWidth * maxWidth);
        } else {
            dstHeight = maxHeight;
            dstWidth = (int) Math.round((double) srcWidth / srcHeight * maxHeight);
        }
        Imgproc.resize(image, image, new Size(dstWidth, dstHeight));
        return image;
    }


    public static String imageToBase64(File file) {
        return imageToBase64(file.getAbsolutePath());
    }

    public static String imageToBase64(MultipartFile file) {
        byte[] data = null;
        try {
            InputStream in = file.getInputStream();
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    public static String imageToBase64(String path) {
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            log.info("[ImageUtils] imageToBase64 failed.");
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    public static String imageContentToBase64(String imageString) {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(imageString.getBytes());
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n|");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * Convert input stream to buffered image
     */
    private static BufferedImage getBufferedImage(InputStream in) {
        byte[] data;
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(in);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            log.info(e.toString());
        }
        return bufferedImage;
    }


    /**
     * Convert input stream image to base64 image string
     *
     * @param inputStream input stream
     * @return base64 image string
     */
    public static String imageToBase64(InputStream inputStream) {
        byte[] data;
        try {
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            log.info(e.toString());
            return "";
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }


    public static BufferedImage resizeImage(BufferedImage originalImage, double times) {
        int width = (int) (originalImage.getWidth() * times);
        int height = (int) (originalImage.getHeight() * times);

        int tType = originalImage.getType();
        if (0 == tType) {
            tType = 5;
        }
        BufferedImage newImage = new BufferedImage(width, height, tType);
        Graphics g = newImage.getGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return newImage;
    }

    /**
     * Cut out pictures method
     *
     * @param bufferedImage 图像源
     * @param startX        裁剪开始x坐标
     * @param startY        裁剪开始y坐标
     * @param endX          裁剪结束x坐标
     * @param endY          裁剪结束y坐标
     * @return
     */
    public static BufferedImage cropImage(BufferedImage bufferedImage, int startX, int startY, int endX, int endY) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (startX == -1) {
            startX = 0;
        }
        if (startY == -1) {
            startY = 0;
        }
        if (endX == -1) {
            endX = width - 1;
        }
        if (endY == -1) {
            endY = height - 1;
        }
        BufferedImage result = new BufferedImage(endX - startX, endY - startY, 4);
        for (int x = startX; x < endX; ++x) {
            for (int y = startY; y < endY; ++y) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x - startX, y - startY, rgb);
            }
        }
        return result;
    }

    /**
     * 带部信息的base64，获取文件格式: jpg/git/png
     *
     * @param base64
     * @return
     */
    public static String getBase64FormalSuffix(String base64) {
        if (!base64.startsWith(CommonContent.IMAGE_BASE64_MOST_PREFIX)) {
            return StringUtils.EMPTY;
        }
        int index = base64.indexOf(CommonContent.BASE64);
        return base64.substring(CommonContent.IMAGE_BASE64_MOST_PREFIX.length(), index);

    }

}

