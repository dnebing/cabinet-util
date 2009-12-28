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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * cab Folder section
 * @author allenhooo at gmail.com
 * 2009-12-24
 */
class CabFolder {
	static final int COMP_MASK_TYPE         = 0x000F;  // Mask for compression type
	static final int COMP_TYPE_NONE         = 0x0000;  // No compression
	static final int COMP_TYPE_MSZIP        = 0x0001;  // MSZIP
	static final int COMP_TYPE_QUANTUM      = 0x0002;  // Quantum
	static final int COMP_TYPE_LZX          = 0x0003;  // LZX
	static final int COMP_BAD               = 0x000F;  // Unspecified compression type

	
	/** offset of the first CFDATA block in this folder, 4bytes */
	int coffCabStart;
	/** number of CFDATA blocks in this folder, 2bytes*/
	int cCFData;
	/** compression type indicator , 2bytes*/
	int typeCompress;
	/** (optional) per-folder reserved area , 1*n byte*/
	short abReserve[];

	/**
	 * data
	 */
	CabData[] cabData;
	
	byte[] getUnCompressedData(int blockIndex) throws IOException{
		if(typeCompress == COMP_TYPE_NONE){
			return cabData[blockIndex].getDataBlock();
		}else if(typeCompress == COMP_TYPE_MSZIP){
			byte[] compressed = cabData[blockIndex].getDataBlock();
			
			Inflater decompresser = new Inflater(true);
		    decompresser.setInput(compressed, 2, compressed.length-2);
		    byte[] unCompressed = new byte[cabData[blockIndex].cbUncomp];
		    try {
				int resultLength = decompresser.inflate(unCompressed);
				decompresser.end();
				if(cabData[blockIndex].cbUncomp != resultLength){
					throw new IOException("文件解压缩错误");
				}
				return unCompressed;
			} catch (DataFormatException e) {
				String s = e.getMessage();
				throw new IOException(s==null?"Bad file format":s, e);
			}
		}else{
			throw new UnsupportedOperationException("不支持的压缩格式:"+typeCompress);
		}
	}
}