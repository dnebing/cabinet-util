/*
  Copyright [2009] [allenhooo at gmail dot com]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.google.code.cabinet;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * CAB DATA section
 * @author allenhooo at gmail.com
 * 2009-12-24
 */
class CabData {
	private RandomAccessFile file;
	private boolean speed_first = false;
	
	CabData(RandomAccessFile file){
		this(file, false);
	}
	CabData(RandomAccessFile file, boolean speed_first){
		this.file = file;
		this.speed_first = speed_first;
	}
	/** checksum of this CFDATA entry , 4bytes*/
	int csum;
	/** number of compressed bytes in this block , 2bytes*/
	int cbData;
	/** number of uncompressed bytes in this block , 2bytes*/
	int cbUncomp;
	/** (optional) per-datablock reserved area , 1*n bytes*/
	short abReserve[];
	/** compressed data bytes (length=cbData) , 1*cbData bytes*/
	private byte ab[];
	/** compressed offset in the cab file*/
	int offset;
	
	/**读取数据块。*/
	byte[] getDataBlock() throws IOException{
		if(ab == null){
			file.seek(offset);
			byte[] x = new byte[cbData];
			file.read(x);
			
			if(speed_first){
				ab = x;
			}
			return x;
		}else{
			return ab;
		}
	}
	
	void fill2byte(byte[] b, int offset, int num){
		b[offset++] = (byte)(num&0x000000FF);
		b[offset++] = (byte)((num&0x0000FF00)>>8);
	}
};