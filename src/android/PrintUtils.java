package com.megster.cordova.ble.central;

import android.annotation.SuppressLint;
import java.nio.charset.Charset;

/**
 * Created by 周旭 on 2017/3/1.
 * 打印格式工具类
 */

public class PrintUtils {

    /**
     * 检测型号
     */
    public static final byte[] CMD_CHECK_TYPE=new byte[]{0x1B,0x2B};
    /**
     * 水平制表
     */
    public static final byte[] CMD_HORIZONTAL_TAB=new byte[]{0x09};
    /**
     * 换行
     */
    public static final byte[] CMD_NEWLINE=new byte[]{0x0A};
    /**
     * 打印当前存储内容
     */
    public static final byte[] CMD_PRINT_CURRENT_CONTEXT=new byte[]{0x0D};
    /**
     * 初始化打印机
     */
    public static final byte[] CMD_INIT_PRINTER=new byte[]{0x1B,0x40};
    /**
     * 允许下划线打印
     */
    public static final byte[] CMD_UNDERLINE_ON=new byte[]{0x1C,0x2D,0x01};
    /**
     * 禁止下划线打印
     */
    public static final byte[] CMD_UNDERLINE_OFF =new byte[]{0x1C,0x2D,0x00};
    /**
     * 允许粗体打印
     */
    public static final byte[] CMD_Blod_ON=new byte[]{0x1B,0x45,0x01};
    /**
     * 禁止粗体打印
     */
    public static final byte[] CMD_BLOD_OFF=new byte[]{0x1B,0x45,0x00};
    /**
     * 选择字体：ASCII(12*24) 汉字（24*24）
     */
    public static final byte[] CMD_SET_FONT_24x24=new byte[]{0x1B,0x4D,0x00};
    /**
     * 选择字体：ASCII(8*16)  汉字（16*16）
     */
    public static final byte[] CMD_SET_FONT_16x16=new byte[]{0x1B,0x4D,0x01};
    /**
     * 字符正常：  不放大
     */
    public static final byte[] CMD_FONTSIZE_NORMAL=new byte[]{0x1D,0x21,0x00};
    /**
     * 字符2倍高：纵向放大
     */
    public static final byte[] CMD_FONTSIZE_DOUBLE_HIGH=new byte[]{0x1D,0x21,0x01};
    /**
     * 字符2倍宽：横向放大
     */
    public static final byte[] CMD_FONTSIZE_DOUBLE_WIDTH=new byte[]{0x1D,0x21,0x10};
    /**
     * 字符2倍整体放大
     */
    public static final byte[] CMD_FONTSIZE_DOUBLE=new byte[]{0x1D,0x21,0x11};
    /**
     * 左对齐
     */
    public static final byte[] CMD_ALIGN_LEFT=new byte[]{0x1B,0x61,0x00};
    /**
     * 居中对齐
     */
    public static final byte[] CMD_ALIGN_MIDDLE=new byte[]{0x1B,0x61,0x01};
    /**
     * 居右对齐
     */
    public static final byte[] CMD_ALIGN_RIGHT=new byte[]{0x1B,0x61,0x02};
    /**
     * 页进纸/黑标定位
     */
    public static final byte[] CMD_BLACK_LOCATION=new byte[]{0x0C};


    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    /**
     * 打印三列时，中间一列的中心线距离打印纸左侧的距离
     */
    private static final int LEFT_LENGTH = 16;

    /**
     * 打印三列时，中间一列的中心线距离打印纸右侧的距离
     */
    private static final int RIGHT_LENGTH = 16;

    /**
     * 打印三列时，第一列汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 5;


    /**
     * 获取数据长度
     *
     * @param msg
     * @return
     */
    @SuppressLint("NewApi")
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }


    /**
     * 打印三列
     *
     * @param leftText   左侧文字
     * @param middleText 中间文字
     * @param rightText  右侧文字
     * @return
     */

    public static String printThreeData(String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 计算左侧文字和中间文字的空格长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - leftTextLength - middleTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }

        // 打印的时候发现，最右边的文字总是偏右一个字符，所以需要删除一个空格
        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

    /**
     * 打印四列
     * @param leftText
     * @param middleLeftText
     * @param middleRightText
     * @param rightText
     * @return
     */
    public static String printFourData(String leftText, String middleLeftText, String middleRightText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > 6) {
            leftText = leftText.substring(0, 6) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleLeftTextLength = getBytesLength(middleLeftText);
        int middleRightTextLength = getBytesLength(middleRightText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 1
        int marginBetweenLeftAndMiddle = 12 - leftTextLength - middleLeftTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(middleLeftText);

        // 2
        int marginBetweenMiddleAndRight = 8 - middleLeftTextLength / 2 - middleRightTextLength/2;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }

        sb.append(middleRightText);
        // 3
        int marginBetweenMiddleAndRightLast = 12 - middleRightTextLength / 2 - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRightLast; i++) {
            sb.append(" ");
        }


        // 打印的时候发现，最右边的文字总是偏右一个字符，所以需要删除一个空格
        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

    /**
     * 打印-号
     * 例如     ----------------海鲜类-------------
     *
     * @param middleText 中间的文字
     * @return
     */
    public static String printStar(String middleText) {
        StringBuilder sb = new StringBuilder();

        int middleTextLength = getBytesLength(middleText);

        // 计算左侧和中间文字的长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - middleTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append("-");
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2;

        for (int i = 0; i < marginBetweenMiddleAndRight - 2; i++) {
            sb.append("-");
        }
        return sb.toString();
    }

    /**
     * 打印之间的标题
     * <p>
     *
     * @param title 居中的标题
     * @return
     */
    public static String printTitle(String title) {
        StringBuilder sb = new StringBuilder();

        int middleTextLength = getBytesLength(title);

        // 计算左侧和中间文字的长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - middleTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(title);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2;

        for (int i = 0; i < marginBetweenMiddleAndRight - 2; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }


    //打印空白的一行
    public static String print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LINE_BYTE_SIZE; i++) {
            sb.append("-");
        }
        return sb.toString();
    }

    //打印--的一行
    public static String prints() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LINE_BYTE_SIZE; i++) {
            sb.append("");
        }
        return sb.toString();
    }




}
