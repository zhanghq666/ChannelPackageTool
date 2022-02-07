/*
 * Copyright 2008 Android4ME
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.util;

/**
 * @author Dmitry Skiba
 *
 */
public class TypedValue {

    public int type;
    public CharSequence string;
    public int data;
    public int assetCookie;
    public int resourceId;
    public int changingConfigurations;
	
    public static final int 
    	TYPE_NULL				=0x00,
    	TYPE_REFERENCE			=0x01,
    	TYPE_ATTRIBUTE			=0x02,
    	TYPE_STRING				=0x03,
    	TYPE_FLOAT				=0x04,
    	TYPE_DIMENSION			=0x05,
    	TYPE_FRACTION			=0x06,
    	TYPE_FIRST_INT			=0x10,
    	TYPE_INT_DEC			=0x10,
    	TYPE_INT_HEX			=0x11,
    	TYPE_INT_BOOLEAN		=0x12,
    	TYPE_FIRST_COLOR_INT	=0x1C,
    	TYPE_INT_COLOR_ARGB8	=0x1C,
    	TYPE_INT_COLOR_RGB8		=0x1D,
    	TYPE_INT_COLOR_ARGB4	=0x1E,
    	TYPE_INT_COLOR_RGB4		=0x1F,
    	TYPE_LAST_COLOR_INT		=0x1F,
    	TYPE_LAST_INT			=0x1F;
    
    public static final int
	    COMPLEX_UNIT_PX			=0,
	    COMPLEX_UNIT_DIP		=1,
	    COMPLEX_UNIT_SP			=2,
	    COMPLEX_UNIT_PT			=3,
	    COMPLEX_UNIT_IN			=4,
	    COMPLEX_UNIT_MM			=5,
    	COMPLEX_UNIT_SHIFT		=0,
	    COMPLEX_UNIT_MASK		=15,
	    COMPLEX_UNIT_FRACTION	=0,
	    COMPLEX_UNIT_FRACTION_PARENT=1,
	    COMPLEX_RADIX_23p0		=0,
	    COMPLEX_RADIX_16p7		=1,
	    COMPLEX_RADIX_8p15		=2,
	    COMPLEX_RADIX_0p23		=3,
	    COMPLEX_RADIX_SHIFT		=4,
	    COMPLEX_RADIX_MASK		=3,
	    COMPLEX_MANTISSA_SHIFT	=8,
	    COMPLEX_MANTISSA_MASK	=0xFFFFFF;
	
}
