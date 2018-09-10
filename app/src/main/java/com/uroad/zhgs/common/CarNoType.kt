package com.uroad.zhgs.common

/**
 *Created by MFB on 2018/8/14.
 */
class CarNoType {
    /**
     * @"京",@"津",@"渝",@"沪",@"冀",@"晋",@"辽",@"吉",@"黑",@"苏",@"浙",
     * @"皖",@"闽",@"赣",@"鲁",@"豫",@"鄂",@"湘",@"粤",@"琼",@"川",@"贵",@"云",
     * @"陕",@"甘",@"青",@"蒙",@"桂",@"宁",@"新",@"藏",@"使",@"领",@"警",@"学",@"港",@"澳"
     */

    companion object {

        fun getCarNoList(): ArrayList<String> {
            val arr = arrayOf("渝", "京", "津", "浙", "沪", "冀", "晋", "辽", "吉", "黑", "苏",
                    "皖", "闽", "赣", "鲁", "豫", "鄂", "湘", "粤", "琼", "川", "贵", "云",
                    "陕", "甘", "青", "蒙", "桂", "宁", "新", "藏", "使", "领", "警", "学", "港", "澳")
            return ArrayList<String>().apply { for (item in arr) add(item) }
        }
    }
}