package com.zhq;

public class XmlInfo {
    public int stringTrunkSize;
    public int targetAttributeOffset;
    public int newStringOffset;
    public int stringCount;
    public int styleCount;
    public int stringPoolOffset;
    public int stylePoolOffset;
    public int stringPoolAlignBytesCount;       // StringPool中为了实现4字节对齐而额外补的字节数（0或2）
}
