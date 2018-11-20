package com.uroad.zhgs.common

import com.uroad.zhgs.model.MutilItem

/**
 *Created by MFB on 2018/8/14.
 */
class CarNoType {
    /**
     * @"京",@"津",@"渝",@"沪",@"冀",@"晋",@"辽",@"吉",@"黑",@"苏",@"浙",
     * @"皖",@"闽",@"赣",@"鲁",@"豫",@"鄂",@"湘",@"粤",@"琼",@"川",@"贵",@"云",
     * @"陕",@"甘",@"青",@"蒙",@"桂",@"宁",@"新",@"藏",@"使",@"领",@"警",@"学",@"港",@"澳"
     */

    class TextType(val text: String) : MutilItem {
        override fun getItemType(): Int = 1
    }

    class Option(val text: String) : MutilItem {
        override fun getItemType(): Int = 2
    }

    companion object {

        fun getCarNoList(): ArrayList<String> {
            val arr = arrayOf("渝", "京", "津", "浙", "沪", "冀", "晋", "辽", "吉", "黑", "苏",
                    "皖", "闽", "赣", "鲁", "豫", "鄂", "湘", "粤", "琼", "川", "贵", "云",
                    "陕", "甘", "青", "蒙", "桂", "宁", "新", "藏", "使", "领", "警", "学", "港", "澳")
            return ArrayList<String>().apply { for (item in arr) add(item) }
        }

        fun getCarMulti(): ArrayList<MutilItem> {
            return ArrayList<MutilItem>().apply {
                add(TextType("京"))
                add(TextType("津"))
                add(TextType("渝"))
                add(TextType("沪"))
                add(TextType("冀"))
                add(TextType("晋"))
                add(TextType("辽"))
                add(TextType("吉"))
                add(TextType("黑"))
                add(TextType("苏"))  //10
                add(TextType("浙"))
                add(TextType("皖"))
                add(TextType("闽"))
                add(TextType("赣"))
                add(TextType("鲁"))
                add(TextType("豫"))
                add(TextType("鄂"))
                add(TextType("湘"))
                add(TextType("粤"))
                add(TextType("琼")) //20
                add(TextType("川"))
                add(TextType("贵"))
                add(TextType("云"))
                add(TextType("陕"))
                add(TextType("甘"))
                add(TextType("青"))
                add(TextType("蒙"))
                add(TextType("桂"))
                add(TextType("宁"))
                add(TextType("新")) //30
                add(Option("取消"))
                add(TextType("藏"))
                add(TextType("使"))
                add(TextType("领"))
                add(TextType("警"))
                add(TextType("学"))
                add(TextType("港"))
                add(TextType("澳"))
                add(Option("完成"))
            }
        }
    }
}